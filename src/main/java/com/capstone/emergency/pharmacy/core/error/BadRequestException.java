package com.capstone.emergency.pharmacy.core.error;

import org.springframework.http.HttpStatus;

public class BadRequestException extends ApiException {
    public BadRequestException(String message, Reason reason) {
        this(message, reason, null);
    }

    public BadRequestException(String message) {
        this(message, Reason.BAD_REQUEST, null);
    }

    public BadRequestException(String message, Reason reason, Throwable cause) {
        super(HttpStatus.BAD_REQUEST, message, reason, null);
    }
}
