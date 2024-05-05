package com.capstone.emergency.pharmacy.core.item.service.model;

import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import jakarta.validation.constraints.NotBlank;

@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
public record AddProductCommand(
        @NotBlank
        String name,
        @NotBlank
        String instruction,
        @NotBlank
        String storageCondition,
        @NotBlank
        String contraindication,
        @NotBlank
        String composition
) {
}
