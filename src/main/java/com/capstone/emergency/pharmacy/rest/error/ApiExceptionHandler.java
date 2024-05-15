package com.capstone.emergency.pharmacy.rest.error;

import com.capstone.emergency.pharmacy.core.error.ApiException;
import com.capstone.emergency.pharmacy.rest.error.model.ApiFailureDto;
import jakarta.validation.ConstraintViolationException;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

import java.util.concurrent.CompletionException;

@RestControllerAdvice
public class ApiExceptionHandler extends ResponseEntityExceptionHandler {

    public static final String INTERNAL_SERVER_ERROR_REASON = "INTERNAL_SERVER_ERROR";
    public static final String INTERNAL_SERVER_ERROR_MESSAGE = "Something went wrong!";

    @ExceptionHandler
    protected ResponseEntity<ApiFailureDto> handleApiException(ApiException ex) {
        if (ex.getStatus().is4xxClientError()) {
            return ResponseEntity
                    .status(ex.getStatus())
                    .body(new ApiFailureDto(ex.getMessage(), ex.getReason().name()));
        }
        return ResponseEntity
                .internalServerError()
                .body(new ApiFailureDto(INTERNAL_SERVER_ERROR_MESSAGE, INTERNAL_SERVER_ERROR_REASON));
    }

    @ExceptionHandler
    protected ResponseEntity<ApiFailureDto> handleApiCompletionException(CompletionException ex) {
        final var cause = (ApiException) ex.getCause();
        if (cause.getStatus().is4xxClientError()) {
            return ResponseEntity
                    .status(cause.getStatus())
                    .body(new ApiFailureDto(cause.getMessage(), cause.getReason().name()));
        }
        return ResponseEntity
                .internalServerError()
                .body(new ApiFailureDto(INTERNAL_SERVER_ERROR_MESSAGE, INTERNAL_SERVER_ERROR_REASON));
    }

    @ExceptionHandler
    protected ResponseEntity<ApiFailureDto> handleRuntimeException(RuntimeException ex) {
        return ResponseEntity
                .internalServerError()
                .body(new ApiFailureDto(INTERNAL_SERVER_ERROR_MESSAGE, INTERNAL_SERVER_ERROR_REASON));
    }

    @ExceptionHandler
    protected ResponseEntity<ApiFailureDto> handleConstraintViolation(ConstraintViolationException ex) {
        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(new ApiFailureDto(ex.getMessage(), ApiException.Reason.BAD_REQUEST.name()));
    }

    @Override
    protected ResponseEntity<Object> handleMethodArgumentNotValid(MethodArgumentNotValidException ex, HttpHeaders headers, HttpStatusCode status, WebRequest request) {
        final var error = ex.getFieldError();
        final var message = error.getField() + " " + error.getDefaultMessage();
        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(new ApiFailureDto(message, ApiException.Reason.BAD_REQUEST.name()));
    }
}
