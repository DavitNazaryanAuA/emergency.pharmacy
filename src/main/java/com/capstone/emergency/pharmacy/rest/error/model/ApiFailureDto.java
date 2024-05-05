package com.capstone.emergency.pharmacy.rest.error.model;

public record ApiFailureDto(
        String message,
        String reason
) {
}
