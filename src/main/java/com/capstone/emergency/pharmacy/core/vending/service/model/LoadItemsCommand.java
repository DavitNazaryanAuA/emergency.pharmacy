package com.capstone.emergency.pharmacy.core.vending.service.model;

import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import org.springframework.validation.annotation.Validated;

import java.util.List;

@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
@Validated
public record LoadItemsCommand(
        @NotEmpty
        List<@Valid LoadedItem> loadedItems
) {

    @JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
    public record LoadedItem(
            @NotNull
            Long itemId,
            @Positive
            Integer quantity
    ) {
    }
}
