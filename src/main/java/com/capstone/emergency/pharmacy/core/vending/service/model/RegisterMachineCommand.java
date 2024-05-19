package com.capstone.emergency.pharmacy.core.vending.service.model;

import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;

@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
public record RegisterMachineCommand(
        @Valid Location location,
        @NotBlank
        String country,
        @NotBlank
        String city,
        @NotBlank
        String address
) {
}
