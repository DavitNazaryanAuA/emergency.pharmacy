package com.capstone.emergency.pharmacy.core.vending.service.model;

import com.capstone.emergency.pharmacy.rest.controller.item.model.response.ItemResponse;
import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;

import java.util.List;

@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
public record CartResponse(
        List<CartItemResponse> cartItemResponse
) {

    @JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
    public record CartItemResponse(
            Long vendingMachineId,
            ItemResponse itemResponse,
            Integer quantity
    ) {
    }
}
