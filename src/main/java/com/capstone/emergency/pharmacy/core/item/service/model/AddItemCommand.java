package com.capstone.emergency.pharmacy.core.item.service.model;

import com.capstone.emergency.pharmacy.core.item.repository.model.Item;
import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
public record AddItemCommand(
        @Positive
        @NotNull
        Double price,
        @Positive
        @NotNull
        Integer packSize,
        @Valid
        @NotNull
        Dose dose,
        @NotNull
        Item.Type type,
        @NotNull
        Long productId
) {
}
