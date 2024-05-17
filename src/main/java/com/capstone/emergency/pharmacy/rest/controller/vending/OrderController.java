package com.capstone.emergency.pharmacy.rest.controller.vending;


import com.capstone.emergency.pharmacy.core.error.BadRequestException;
import com.capstone.emergency.pharmacy.core.vending.service.OrderService;
import com.capstone.emergency.pharmacy.core.vending.service.VendingMachineService;
import com.capstone.emergency.pharmacy.core.vending.service.model.OrderItemsCommand;
import com.capstone.emergency.pharmacy.rest.controller.vending.model.OrderResponse;
import com.google.gson.JsonSyntaxException;
import com.stripe.exception.SignatureVerificationException;
import com.stripe.model.Event;
import com.stripe.model.StripeObject;
import com.stripe.model.checkout.Session;
import com.stripe.net.Webhook;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
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

    @Value("${stripe.endpoint.secret}")
    private final String webHookEndpointSecret;

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

    @PostMapping("/stripe-webhook")
    public ResponseEntity<Void> checkOut(
            @RequestBody String paymentEventJson,
            @RequestHeader("Stripe-Signature") String signatureHeader
    ) {
        System.out.println(paymentEventJson);
        System.out.println(signatureHeader);
        Event event;
        try {
            event = Webhook.constructEvent(paymentEventJson, signatureHeader, webHookEndpointSecret);
        } catch (SignatureVerificationException | JsonSyntaxException ex) {
            throw new BadRequestException(ex.getMessage());
        }
        System.out.println(event);
        StripeObject stripeObject = event.getDataObjectDeserializer().getObject().orElseThrow(() -> new BadRequestException("No Stripe event"));
        System.out.println(stripeObject);
        if ("checkout.session.completed".equals(event.getType())) {
            Session session = (Session) stripeObject;
            System.out.println(session);
            final var orderId = session.getClientReferenceId();
            orderService.checkOut(orderId);
        } else {
            throw new BadRequestException("Unhandled stripe event type");
        }

        return ResponseEntity.ok().build();
    }
}
