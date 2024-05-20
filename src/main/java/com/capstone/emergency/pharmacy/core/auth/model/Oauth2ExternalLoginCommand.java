package com.capstone.emergency.pharmacy.core.auth.model;

import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;

@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
public record Oauth2ExternalLoginCommand(
        String serviceId,
        String email,
        String firstName,
        String lastName
) {
}
