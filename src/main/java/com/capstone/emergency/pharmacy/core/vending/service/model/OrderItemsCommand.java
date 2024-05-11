package com.capstone.emergency.pharmacy.core.vending.service.model;

import com.capstone.emergency.pharmacy.core.vending.repository.Orderable;
import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.util.List;

@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
public record OrderItemsCommand(
        @NotNull
        Long vendingMachineId,
        @NotEmpty
        List<@Valid OrderedItem> items
) {

    @JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
    public record OrderedItem(
            @NotNull
            Long itemId,
            @NotNull
            @Positive
            Integer quantity
    ) implements Orderable {
        @Override
        public Long getItemId() {
            return itemId();
        }

        @Override
        public Integer getQuantity() {
            return quantity();
        }
    }
}
