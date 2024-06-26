package com.capstone.emergency.pharmacy.rest.controller.vending.model;

import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;

@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
public record OrderCreatedResponse(
        String orderId,
        Double total,
        StripeData paymentData
) {

    @JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
    public record StripeData(
            String paymentIntentSecret,
            String ephemeralKey,
            String customerId,
            String publishableKey
    ) {

    }
}
