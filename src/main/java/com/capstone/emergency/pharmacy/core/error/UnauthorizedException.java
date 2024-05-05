package com.capstone.emergency.pharmacy.core.error;

import org.springframework.http.HttpStatus;

public class UnauthorizedException extends ApiException{
    public UnauthorizedException(String message) {
        this(message, null);
    }

    public UnauthorizedException(String message, Throwable cause) {
        super(HttpStatus.UNAUTHORIZED, message, Reason.UNAUTHORIZED, null);
    }
}
