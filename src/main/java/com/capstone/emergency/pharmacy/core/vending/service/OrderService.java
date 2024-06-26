package com.capstone.emergency.pharmacy.core.vending.service;

import com.capstone.emergency.pharmacy.core.error.BadRequestException;
import com.capstone.emergency.pharmacy.core.error.NotFoundException;
import com.capstone.emergency.pharmacy.core.vending.repository.Orderable;
import com.capstone.emergency.pharmacy.core.vending.repository.VendingMachineItemRepository;
import com.capstone.emergency.pharmacy.core.vending.repository.VendingMachineRepository;
import com.capstone.emergency.pharmacy.core.vending.repository.mongo.CartRepository;
import com.capstone.emergency.pharmacy.core.vending.repository.mongo.OrderRepository;
import com.capstone.emergency.pharmacy.core.vending.repository.mongo.model.Order;
import com.capstone.emergency.pharmacy.core.vending.service.model.OrderItemsCommand;
import com.capstone.emergency.pharmacy.rest.controller.vending.model.OrderResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.function.Consumer;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class OrderService {

    private final OrderRepository orderRepository;
    private final VendingMachineItemRepository vendingMachineItemRepository;
    private final VendingMachineRepository vendingMachineRepository;
    private final CartRepository cartRepository;
    private final CartService cartService;


    public List<OrderResponse> getUserOrders(String userId, Integer page) {
        final var orders = orderRepository.findByUserIdAndStatus(userId, Order.Status.COMPLETE, PageRequest.of(page, 10));
        final var vmIds = orders.stream().map(order -> Long.valueOf(order.getVendingMachineId())).toList();
        final var vms = vendingMachineRepository.getAllByIdIn(vmIds).stream()
                .collect(Collectors.toMap(
                        vm -> vm.getId().toString(),
                        vm -> vm.getAddress().getAddress()
                ));

        return orders.stream().map(order ->
                new OrderResponse(
                        order.getId(),
                        order.getTotal(),
                        order.getDate(),
                        new OrderResponse.VMResponse(
                                Long.valueOf(order.getVendingMachineId()),
                                vms.get(order.getVendingMachineId())
                        ),
                        order.getItems().stream().map(item ->
                                new OrderResponse.OrderItemResponse(
                                        Long.valueOf(item.getItemId()),
                                        item.getItemName(),
                                        item.getQuantity(),
                                        item.getPrice()
                                )
                        ).toList()
                )
        ).toList();
    }

    public void setOrderStatus(String orderId, Order.Status status) {
        final var order = orderRepository.findById(orderId).orElseThrow(() -> new NotFoundException("Order not found"));
        order.setStatus(status);
        orderRepository.save(order);
    }

    public Order orderItems(
            String userId,
            OrderItemsCommand orderItemsCommand
    ) {
        final var machineId = orderItemsCommand.vendingMachineId();
        final var orderedItems = orderItemsCommand.items();
        vendingMachineRepository.findById(machineId)
                .orElseThrow(() -> new NotFoundException("Vending machine with id: " + machineId + " not found"));

        return placeOrder(userId, machineId, orderedItems);
    }

    public Order orderItemsInCart(
            String userId,
            Consumer<Long> machineLockVerification
    ) {
        final var cart = cartRepository.findByUserId(userId)
                .orElseThrow(() -> new NotFoundException("Cart not found"));
        final var cartItems = cart.getCartItems();
        if (cartItems.isEmpty()) {
            throw new NotFoundException("Cart is empty");
        }

        final var machineId = cart.getVendingMachineId();
        machineLockVerification.accept(machineId);

        return placeOrder(userId, machineId, cartItems);
    }

    public void checkOut(String orderId) {
        final var order = orderRepository.findById(orderId)
                .orElseThrow(() -> new NotFoundException("Order: " + orderId + " not found for"));

        final var userId = order.getUserId();

        if (!order.getStatus().isPending()) {
            throw new BadRequestException("Order: " + orderId + " has been completed");
        }

        final var machineId = Long.valueOf(order.getVendingMachineId());

        order.getItems().stream()
                .map(orderItem ->
                        CompletableFuture.<Void>supplyAsync(() -> {
                                    final var itemId = Long.valueOf(orderItem.getItemId());
                                    final var machineItem = vendingMachineItemRepository
                                            .findByVendingMachineIdAndItem_Id(machineId, itemId).get();

                                    if (machineItem.getQuantity() - orderItem.getQuantity() == 0) {
                                        vendingMachineItemRepository.delete(machineItem);
                                    } else {
                                        machineItem.setQuantity(machineItem.getQuantity() - orderItem.getQuantity());
                                        vendingMachineItemRepository.save(machineItem);
                                    }

                                    return null;
                                }
                        )
                )
                .toList()
                .stream()
                .map(CompletableFuture::join)
                .close();

        order.setStatus(Order.Status.COMPLETE);
        orderRepository.save(order);
        cartService.deleteCart(userId);
    }

    private <V extends Orderable> Order placeOrder(
            String userId,
            Long vendingMachineId,
            List<V> orderables
    ) {
        final var items = vendingMachineItemRepository.findAllByVendingMachineId(vendingMachineId);
        if (items.isEmpty()) {
            throw new NotFoundException("No items in machine");
        }

        final var idToItems = items.stream().collect(
                Collectors.toMap(
                        item -> item.getItem().getId(),
                        item -> item
                )
        );

        // check to see if there is enough of each item in the machine
        orderables.forEach(item -> {
            Integer foundQuantity = idToItems.get(item.getItemId()).getQuantity();
            foundQuantity = foundQuantity == null ? 0 : foundQuantity;
            if (foundQuantity < item.getQuantity()) {
                throw new BadRequestException(
                        "Not enough of item: " + item.getItemId() + " in machine." +
                                " Requested: " + item.getQuantity() + ", found: " + foundQuantity
                );
            }
        });

        final var orderItemsPrices = orderables.stream().collect(
                Collectors.toMap(
                        Orderable::getItemId,
                        item -> idToItems.get(item.getItemId()).getItem().getPrice() * item.getQuantity()
                )
        );

        final var order = Order.builder()
                .items(
                        orderables.stream().map(item ->
                                Order.OrderItem.builder()
                                        .itemId(item.getItemId().toString())
                                        .itemName(item.getItemName())
                                        .price(
                                                orderItemsPrices.get(item.getItemId())
                                        )
                                        .quantity(item.getQuantity())
                                        .build()
                        ).toList()
                )
                .total(orderItemsPrices.values().stream().reduce(Double::sum).get())
                .vendingMachineId(vendingMachineId.toString())
                .userId(userId)
                .status(Order.Status.PENDING)
                .date(new Date())
                .build();

        return orderRepository.save(order);
    }
}
