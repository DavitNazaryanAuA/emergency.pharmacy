package com.capstone.emergency.pharmacy.core.vending.service;

import com.capstone.emergency.pharmacy.core.error.BadRequestException;
import com.capstone.emergency.pharmacy.core.error.NotFoundException;
import com.capstone.emergency.pharmacy.core.user.repository.UserRepository;
import com.capstone.emergency.pharmacy.core.vending.repository.mongo.model.Order;
import com.capstone.emergency.pharmacy.rest.controller.vending.model.OrderCreatedResponse;
import com.google.gson.JsonSyntaxException;
import com.stripe.Stripe;
import com.stripe.exception.SignatureVerificationException;
import com.stripe.exception.StripeException;
import com.stripe.model.Customer;
import com.stripe.model.EphemeralKey;
import com.stripe.model.Event;
import com.stripe.model.PaymentIntent;
import com.stripe.net.RequestOptions;
import com.stripe.net.Webhook;
import com.stripe.param.CustomerCreateParams;
import com.stripe.param.EphemeralKeyCreateParams;
import com.stripe.param.PaymentIntentCreateParams;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@RequiredArgsConstructor
@Service
public class StripeService {

    @Value("${stripe.publishable_key}")
    private String publishableKey;

    @Value("${stripe.endpoint.secret}")
    private String whSecret;

    final private UserRepository userRepository;
    final private RequestOptions requestOptions;
    final private OrderService orderService;

    public void handlePaymentIntentEvents(String paymentEventJson, String signatureHeader) {
        Event event;
        try {
            event = Webhook.constructEvent(paymentEventJson, signatureHeader, whSecret);
        } catch (SignatureVerificationException | JsonSyntaxException ex) {
            throw new BadRequestException(ex.getMessage());
        }

        final var stripeObject = event.getDataObjectDeserializer().getObject().orElseThrow(() -> new NotFoundException("Stripe object not found"));
        final var paymentIntent = (PaymentIntent) stripeObject;
        final var orderId = paymentIntent.getMetadata().get("orderId");

        if ("payment_intent.succeeded".equals(event.getType())) {
            orderService.checkOut(orderId);
        } else if (
                "payment_intent.payment_failed".equals(event.getType()) ||
                        "payment_intent.canceled".equals(event.getType())
        ) {
            orderService.setOrderStatus(orderId, Order.Status.CANCELLED);
        } else {
            throw new IllegalArgumentException("Invalid event type");
        }
    }

    public OrderCreatedResponse.StripeData createPaymentFromOrder(Order order) {

        final var user = userRepository.findById(order.getUserId())
                .orElseThrow(() -> new NotFoundException("User not found"));

        Customer customer = null;
        if (user.getStripeId() != null) {
            try {
                customer = Customer.retrieve(user.getStripeId(), requestOptions);
            } catch (StripeException e) {
                System.out.println(e);
            }
        }

        if (customer == null) {
            try {
                customer = Customer.create(
                        CustomerCreateParams.builder()
                                .setName(user.getName() + " " + user.getLastName())
                                .setEmail(user.getEmail())
                                .build(),
                        requestOptions
                );
            } catch (StripeException e) {
                throw new BadRequestException("Failed to create a stripe customer");
            }
        }

        try {
            final var paymentIntent = PaymentIntent.create(
                    PaymentIntentCreateParams.builder()
                            .addPaymentMethodType("card")
                            .putMetadata("orderId", order.getId())
                            .setCustomer(customer.getId())
                            .setAmount(order.getTotal().longValue())
                            .setCurrency("usd")
                            .build(),
                    requestOptions
            );

            return new OrderCreatedResponse.StripeData(
                    paymentIntent.getClientSecret(),
                    EphemeralKey.create(
                            EphemeralKeyCreateParams.builder()
                                    .setStripeVersion(Stripe.API_VERSION)
                                    .setCustomer(customer.getId())
                                    .build(),
                            requestOptions
                    ).getSecret(),
                    customer.getId(),
                    publishableKey
            );
        } catch (Exception e) {
            System.out.println(e.getMessage());
            throw new BadRequestException("Payment intent failed to create");
        }
    }
}
