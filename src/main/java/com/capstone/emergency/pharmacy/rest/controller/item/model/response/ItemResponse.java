package com.capstone.emergency.pharmacy.rest.controller.item.model.response;

import com.capstone.emergency.pharmacy.core.item.repository.model.Item;
import com.capstone.emergency.pharmacy.core.item.service.model.Dose;
import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;

@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
public record ItemResponse(
        long id,
        double price,
        Dose dose,
        Item.Type type,
        ProductResponse product
) {
}
