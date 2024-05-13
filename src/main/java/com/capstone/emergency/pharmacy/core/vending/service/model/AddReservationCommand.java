package com.capstone.emergency.pharmacy.core.vending.service.model;

import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
public record AddReservationCommand(
        @NotNull
        Long itemId,
        @NotNull
        @Positive
        Integer quantity
) {
}
