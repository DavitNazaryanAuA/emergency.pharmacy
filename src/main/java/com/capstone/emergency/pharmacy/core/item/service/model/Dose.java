package com.capstone.emergency.pharmacy.core.item.service.model;

import com.capstone.emergency.pharmacy.core.item.repository.model.Unit;
import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
public record Dose(
        @NotNull
        Unit unit,
        @Positive
        double amount
) {
}
