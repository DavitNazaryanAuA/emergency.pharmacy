package com.capstone.emergency.pharmacy.rest.controller.vending.model;

import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;

import java.util.Date;
import java.util.List;

@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
public record OrderResponse(
        String id,
        Double total,
        Date date,
        VMResponse vending_machine_info,
        List<OrderItemResponse> items
) {
    @JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
    public record OrderItemResponse(
            Long itemId,
            String itemName,
            Integer quantity,
            Double price
    ) {

    }

    @JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
    public record VMResponse(
            Long vendingMachineId,
            String address
    ) {
    }
}
