package com.capstone.emergency.pharmacy.core.error;

import org.springframework.http.HttpStatus;

public class ForbiddenException extends ApiException {
    public ForbiddenException(String message, Reason reason) {
        this(message, reason, null);
    }

    public ForbiddenException(String message) {
        this(message, Reason.FORBIDDEN, null);
    }

    public ForbiddenException(String message, Reason reason, Throwable cause) {
        super(
                HttpStatus.FORBIDDEN,
                message,
                reason,
                null
        );
    }
}