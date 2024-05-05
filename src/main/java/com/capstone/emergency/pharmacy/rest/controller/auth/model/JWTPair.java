package com.capstone.emergency.pharmacy.rest.controller.auth.model;

import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;

@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
public record JWTPair(
        String accessToken,
        String refreshToken
) {
}
