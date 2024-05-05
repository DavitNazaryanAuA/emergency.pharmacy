package com.capstone.emergency.pharmacy.rest.controller.vending.model;

import com.capstone.emergency.pharmacy.rest.controller.item.model.response.ItemResponse;
import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;

@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
public record VendingMachineItemResponse(
        Long vendingMachineId,
        ItemResponse item,
        Integer quantity
) {
}
