package com.capstone.emergency.pharmacy.rest.controller.item.model.response;

import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;

@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
public record ProductResponse(
        long id,
        String name,
        String instruction,
        String storageCondition,
        String contraindication,
        String composition
) {
}
