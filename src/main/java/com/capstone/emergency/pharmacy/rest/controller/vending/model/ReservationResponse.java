package com.capstone.emergency.pharmacy.rest.controller.vending.model;

import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;

@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
public record ReservationResponse(
        String id,
        String userId,
        String expDate,
        Integer quantity,
        ReservedItemResponse reservedItem
) {

    public record ReservedItemResponse(
            String itemId,
            String vendingMachineId
    ) {

    }
}
