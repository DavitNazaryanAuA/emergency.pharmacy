package com.capstone.emergency.pharmacy.rest.controller.vending;


import com.capstone.emergency.pharmacy.core.vending.service.OrderService;
import com.capstone.emergency.pharmacy.core.vending.service.VendingMachineService;
import com.capstone.emergency.pharmacy.core.vending.service.model.OrderItemsCommand;
import com.capstone.emergency.pharmacy.rest.controller.vending.model.OrderResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.function.Consumer;

@Validated
@RequiredArgsConstructor
@CrossOrigin
@RestController
@RequestMapping("/api/order")
public class OrderController {

    private final OrderService orderService;
    private final VendingMachineService vendingMachineService;

    @PostMapping
    public ResponseEntity<OrderResponse> orderItems(
            @RequestBody @Valid OrderItemsCommand command
    ) {
        final var auth = SecurityContextHolder.getContext().getAuthentication();
        final var jwt = (Jwt) auth.getPrincipal();
        final var userId = jwt.getSubject();

        vendingMachineService.validateMachineLock(command.vendingMachineId(), userId);

        final var order = orderService.orderItems(userId, command);
        final var response = new OrderResponse(order.getId(), order.getTotal());
        return ResponseEntity.ok(response);
    }

    @PostMapping("/cart")
    public ResponseEntity<OrderResponse> orderCartItems() {
        final var auth = SecurityContextHolder.getContext().getAuthentication();
        final var jwt = (Jwt) auth.getPrincipal();
        final var userId = jwt.getSubject();

        Consumer<Long> verificationCallback = (Long machineId) -> vendingMachineService.validateMachineLock(machineId, userId);

        final var order = orderService.orderItemsInCart(userId, verificationCallback);
        final var response = new OrderResponse(order.getId(), order.getTotal());
        return ResponseEntity.ok(response);
    }
}
