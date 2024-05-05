package com.capstone.emergency.pharmacy.rest.controller.vending.model.model;

import com.capstone.emergency.pharmacy.rest.controller.vending.model.VendingMachineItemResponse;
import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;

import java.util.List;

@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
public record VendingMachineLoadedItemsResponse(
        Long vendingMachineId,
        List<VendingMachineItemResponse> items
) {
}
