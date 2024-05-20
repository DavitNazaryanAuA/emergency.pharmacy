package com.capstone.emergency.pharmacy.rest.controller.vending;


import com.capstone.emergency.pharmacy.core.email.model.EmailDto;
import com.capstone.emergency.pharmacy.core.email.service.EmailService;
import com.capstone.emergency.pharmacy.core.vending.service.OrderService;
import com.capstone.emergency.pharmacy.core.vending.service.StripeService;
import com.capstone.emergency.pharmacy.core.vending.service.VendingMachineService;
import com.capstone.emergency.pharmacy.core.vending.service.model.OrderItemsCommand;
import com.capstone.emergency.pharmacy.rest.controller.vending.model.OrderCreatedResponse;
import com.capstone.emergency.pharmacy.rest.controller.vending.model.OrderResponse;
import com.stripe.exception.StripeException;
import com.stripe.model.PaymentIntent;
import com.stripe.net.RequestOptions;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.function.Consumer;

@Validated
@RequiredArgsConstructor
@CrossOrigin
@RestController
@RequestMapping("/api/order")
public class OrderController {

    private final OrderService orderService;
    private final VendingMachineService vendingMachineService;
    private final StripeService stripeService;
    private final EmailService emailService;
    final private RequestOptions requestOptions;


    @GetMapping
    public ResponseEntity<List<OrderResponse>> orderHistory(
            @RequestParam("page") @NotNull @Positive Integer page
    ) {
        final var auth = SecurityContextHolder.getContext().getAuthentication();
        final var jwt = (Jwt) auth.getPrincipal();
        final var userId = jwt.getSubject();

        return ResponseEntity.ok(
                orderService.getUserOrders(userId, page)
        );
    }

    @PostMapping
    public ResponseEntity<OrderCreatedResponse> orderItems(
            @RequestBody @Valid OrderItemsCommand command
    ) {
        final var auth = SecurityContextHolder.getContext().getAuthentication();
        final var jwt = (Jwt) auth.getPrincipal();
        final var userId = jwt.getSubject();

        vendingMachineService.validateMachineLock(command.vendingMachineId(), userId);
        final var order = orderService.orderItems(userId, command);
        final var response = new OrderCreatedResponse(
                order.getId(),
                order.getTotal(),
                stripeService.createPaymentFromOrder(order)
        );
        return ResponseEntity.ok(response);
    }

    @PostMapping("/cart")
    public ResponseEntity<OrderCreatedResponse> orderCartItems() {
        final var auth = SecurityContextHolder.getContext().getAuthentication();
        final var jwt = (Jwt) auth.getPrincipal();
        final var userId = jwt.getSubject();

        Consumer<Long> verificationCallback = (Long machineId) -> vendingMachineService.validateMachineLock(machineId, userId);

        final var order = orderService.orderItemsInCart(userId, verificationCallback);
        final var response = new OrderCreatedResponse(
                order.getId(),
                order.getTotal(),
                stripeService.createPaymentFromOrder(order)
        );
        return ResponseEntity.ok(response);
    }

    @PostMapping("/stripe-webhook")
    public ResponseEntity<Void> checkOut(
            @RequestBody String paymentEventJson,
            @RequestHeader("stripe-signature") String signatureHeader
    ) {
        stripeService.handlePaymentIntentEvents(paymentEventJson, signatureHeader);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/stripe/confirm")
    public ResponseEntity<Void> confirm(
            @RequestParam("id") String intentId
    ) throws StripeException {

//        final var intent = PaymentIntent.retrieve(intentId, requestOptions);
////        intent.confirm(
////                PaymentIntentConfirmParams.builder()
////                        .setPaymentMethod("pm_card_visa")
////                        .build(),
////                requestOptions
////        );
//
//        intent.cancel(requestOptions);

        try {
            emailService.sendEmail(EmailDto.builder()
                    .subject("TEST")
                    .text("hello")
                    .emailTo("davnazaryan17@gmail.com")
                    .build());
        }catch (Exception e) {
        }


        return ResponseEntity.ok().build();
    }
}
