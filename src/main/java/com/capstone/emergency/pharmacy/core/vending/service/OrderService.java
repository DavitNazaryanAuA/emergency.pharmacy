package com.capstone.emergency.pharmacy.core.vending.service;

import com.capstone.emergency.pharmacy.core.error.BadRequestException;
import com.capstone.emergency.pharmacy.core.error.NotFoundException;
import com.capstone.emergency.pharmacy.core.vending.repository.mongo.OrderRepository;
import com.capstone.emergency.pharmacy.core.vending.repository.VendingMachineItemRepository;
import com.capstone.emergency.pharmacy.core.vending.repository.VendingMachineRepository;
import com.capstone.emergency.pharmacy.core.vending.repository.model.Order;
import com.capstone.emergency.pharmacy.core.vending.service.model.OrderItemsCommand;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class OrderService {

    private final OrderRepository orderRepository;
    private final VendingMachineItemRepository vendingMachineItemRepository;
    private final VendingMachineRepository vendingMachineRepository;


    public Order orderItems(
            String userId,
            OrderItemsCommand orderItemsCommand
    ) {
        final var machineId = orderItemsCommand.vendingMachineId();
        final var orderedItems = orderItemsCommand.items();
        vendingMachineRepository.findById(machineId)
                .orElseThrow(() -> new NotFoundException("Vending machine with id: " + machineId + " not found"));

        final var items = vendingMachineItemRepository.findAllByVendingMachineId(machineId);
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
        orderedItems.forEach(item -> {
            Integer foundQuantity = idToItems.get(item.itemId()).getQuantity();
            foundQuantity = foundQuantity == null ? 0 : foundQuantity;
            if (foundQuantity < item.quantity()) {
                throw new BadRequestException(
                        "Not enough of item: " + item.itemId() + " in machine." +
                                " Requested: " + item.quantity() + ", found: " + foundQuantity
                );
            }
        });

        final var orderItemsPrices = orderedItems.stream().collect(
                Collectors.toMap(
                        OrderItemsCommand.OrderedItem::itemId,
                        item -> idToItems.get(item.itemId()).getItem().getPrice() * item.quantity()
                )
        );

        final var order = Order.builder()
                .items(
                        orderedItems.stream().map(item ->
                                Order.OrderItem.builder()
                                        .vendingMachineId(machineId.toString())
                                        .itemId(item.itemId().toString())
                                        .price(
                                                orderItemsPrices.get(item.itemId())
                                        )
                                        .quantity(item.quantity())
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
