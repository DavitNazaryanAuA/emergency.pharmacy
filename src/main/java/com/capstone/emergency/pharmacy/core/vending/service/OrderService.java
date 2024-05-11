package com.capstone.emergency.pharmacy.core.vending.service;

import com.capstone.emergency.pharmacy.core.error.BadRequestException;
import com.capstone.emergency.pharmacy.core.error.NotFoundException;
import com.capstone.emergency.pharmacy.core.vending.repository.CartItemRepository;
import com.capstone.emergency.pharmacy.core.vending.repository.Orderable;
import com.capstone.emergency.pharmacy.core.vending.repository.VendingMachineItemRepository;
import com.capstone.emergency.pharmacy.core.vending.repository.VendingMachineRepository;
import com.capstone.emergency.pharmacy.core.vending.repository.model.Order;
import com.capstone.emergency.pharmacy.core.vending.repository.mongo.OrderRepository;
import com.capstone.emergency.pharmacy.core.vending.service.model.OrderItemsCommand;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.List;
import java.util.function.Consumer;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class OrderService {

    private final OrderRepository orderRepository;
    private final VendingMachineItemRepository vendingMachineItemRepository;
    private final VendingMachineRepository vendingMachineRepository;
    private final CartItemRepository cartItemRepository;


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
        final var cartItems = cartItemRepository.getCartItems(userId);
        if (cartItems.isEmpty()) {
            throw new NotFoundException("Cart is empty");
        }

        final var machineId = cartItems.get(0).getVendingMachineId();
        machineLockVerification.accept(machineId);

        return placeOrder(userId, machineId, cartItems);
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
                                        .vendingMachineId(vendingMachineId.toString())
                                        .itemId(item.getItemId().toString())
                                        .price(
                                                orderItemsPrices.get(item.getItemId())
                                        )
                                        .quantity(item.getQuantity())
                                        .build()
                        ).toList()
                )
                .total(orderItemsPrices.values().stream().reduce(Double::sum).get())
                .paid(false)
                .userId(userId)
                .paid(false)
                .date(new Date())
                .build();

        return orderRepository.save(order);
    }
}
