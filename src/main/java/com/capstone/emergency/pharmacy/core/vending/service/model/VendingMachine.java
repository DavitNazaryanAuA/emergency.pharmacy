package com.capstone.emergency.pharmacy.core.vending.service.model;

import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;

@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
public record VendingMachine(
        Long id,
        Location location
) {
}
