package com.capstone.emergency.pharmacy.rest.controller.vending;


import com.capstone.emergency.pharmacy.core.error.BadRequestException;
import com.capstone.emergency.pharmacy.core.vending.service.OrderService;
import com.capstone.emergency.pharmacy.core.vending.service.VendingMachineService;
import com.capstone.emergency.pharmacy.core.vending.service.model.OrderItemsCommand;
import com.capstone.emergency.pharmacy.rest.controller.vending.model.OrderResponse;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.gson.JsonSyntaxException;
import com.stripe.exception.SignatureVerificationException;
import com.stripe.exception.StripeException;
import com.stripe.model.Event;
import com.stripe.model.StripeObject;
import com.stripe.model.checkout.Session;
import com.stripe.net.RequestOptions;
import com.stripe.net.Webhook;
import com.stripe.param.checkout.SessionCreateParams;
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
    private final ObjectMapper objectMapper;

    @Value("${stripe.endpoint.secret}")
    private String webHookEndpointSecret;

    @Value("${stripe.api.key}")
    private String apiKey;

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
            @RequestHeader("Signature-header") String signatureHeader
    ) {
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

    @PostMapping("/checkout-session")
    public ResponseEntity<Void> session() {
        SessionCreateParams params =
                SessionCreateParams.builder()
                        .setSuccessUrl("https://example.com/success")
                        .addLineItem(
                                SessionCreateParams.LineItem.builder()
                                        .setPriceData(
                                                SessionCreateParams.LineItem.PriceData.builder()
                                                        .setCurrency("usd")
                                                        .setUnitAmount(1000L)
                                                        .setProductData(
                                                                SessionCreateParams.LineItem.PriceData.ProductData.builder()
                                                                        .setName("Products")
                                                                        .build()
                                                        )
                                                        .build()
                                        )
                                        .setQuantity(2L)
                                        .build()
                        )
                        .setClientReferenceId("id")
                        .setMode(SessionCreateParams.Mode.PAYMENT)
                        .build();
        try {
            Session session = Session.create(params, RequestOptions.builder().setApiKey(apiKey).build() );
            System.out.println(session);

            Thread.sleep( 5000);

            final var retrieved = Session.retrieve(session.getId(), RequestOptions.builder().setApiKey(apiKey).build());
            System.out.println(retrieved);
        } catch (StripeException e) {
            System.out.println(e.getMessage());
            System.out.println("Error creating session");
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        return ResponseEntity.ok().build();
    }
}
