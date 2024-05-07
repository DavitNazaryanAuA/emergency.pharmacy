package com.capstone.emergency.pharmacy.core.error;

import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
public abstract class ApiException extends RuntimeException {
    private final HttpStatus status;
    private final String message;
    private final Reason reason;

    public ApiException(
            HttpStatus status,
            String message,
            Reason reason,
            Throwable cause
    ) {
        super(message, cause);
        this.status = status;
        this.message = message;
        this.reason = reason;
    }

    public enum Reason {
        NOT_FOUND,
        BAD_REQUEST,
        INTERNAL_SERVER_ERROR,
        UNAUTHORIZED,
        FORBIDDEN,
        NOT_ENOUGH_ITEMS,
        MULTIPLE_MACHINES_TO_CART
    }
}
