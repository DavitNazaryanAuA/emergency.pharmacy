package com.capstone.emergency.pharmacy.core.vending.service.model;

import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import org.hibernate.validator.constraints.Range;

@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
public record Location(
        @Range(min = -180, max = 180, message = "Longitude must be in a value between -180 and 180")
        double longitude,
        @Range(min = -90, max = 90, message = "Latitude must be in a value between -90 and 90")
        double latitude
) {
}