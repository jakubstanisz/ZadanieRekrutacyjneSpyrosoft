package com.jakubstaniszewski.evcharging.backend.infrastructure.adapter.in.web.exception;

import com.jakubstaniszewski.evcharging.backend.application.exception.ExternalEnergyDataUnavailableException;
import com.jakubstaniszewski.evcharging.backend.domain.exception.BookingConflictException;
import com.jakubstaniszewski.evcharging.backend.domain.exception.BookingValidationException;
import com.jakubstaniszewski.evcharging.backend.domain.exception.ForbiddenActionException;
import com.jakubstaniszewski.evcharging.backend.domain.exception.ResourceNotFoundException;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingRequestHeaderException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

import java.net.URI;
import java.time.Clock;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Global REST exception handler translating domain, application, and validation errors
 * into RFC 9457 {@link ProblemDetail} responses.
 */
@RestControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    private final Clock clock;

    /**
     * Creates a new {@link GlobalExceptionHandler}.
     *
     * @param clock UTC clock reference for error timestamps
     */
    public GlobalExceptionHandler(Clock clock) {
        this.clock = clock;
    }

    @ExceptionHandler(BookingValidationException.class)
    public ProblemDetail handleValidationException(BookingValidationException ex) {
        return buildProblemDetail(HttpStatus.BAD_REQUEST, "validation-error", "Validation Error", ex.getMessage());
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ProblemDetail handleMethodArgumentNotValid(MethodArgumentNotValidException ex) {
        Map<String, String> fieldErrors = new LinkedHashMap<>();
        for (FieldError fieldError : ex.getBindingResult().getFieldErrors()) {
            fieldErrors.put(fieldError.getField(), fieldError.getDefaultMessage());
        }
        ProblemDetail problem = buildProblemDetail(
                HttpStatus.BAD_REQUEST,
                "validation-error",
                "Request Validation Failed",
                "One or more request fields failed validation"
        );
        problem.setProperty("errors", fieldErrors);
        return problem;
    }

    @ExceptionHandler(ConstraintViolationException.class)
    public ProblemDetail handleConstraintViolation(ConstraintViolationException ex) {
        Map<String, String> violations = new LinkedHashMap<>();
        for (ConstraintViolation<?> violation : ex.getConstraintViolations()) {
            String path = violation.getPropertyPath().toString();
            violations.put(path, violation.getMessage());
        }
        ProblemDetail problem = buildProblemDetail(
                HttpStatus.BAD_REQUEST,
                "validation-error",
                "Parameter Validation Failed",
                "One or more request parameters failed validation"
        );
        problem.setProperty("errors", violations);
        return problem;
    }

    @ExceptionHandler({
            MissingRequestHeaderException.class,
            MissingServletRequestParameterException.class,
            MethodArgumentTypeMismatchException.class,
            HttpMessageNotReadableException.class
    })
    public ProblemDetail handleMalformedRequest(Exception ex) {
        return buildProblemDetail(
                HttpStatus.BAD_REQUEST,
                "malformed-request",
                "Malformed Request",
                ex.getMessage()
        );
    }

    @ExceptionHandler(ForbiddenActionException.class)
    public ProblemDetail handleForbidden(ForbiddenActionException ex) {
        return buildProblemDetail(HttpStatus.FORBIDDEN, "forbidden", "Forbidden", ex.getMessage());
    }

    @ExceptionHandler(ResourceNotFoundException.class)
    public ProblemDetail handleNotFound(ResourceNotFoundException ex) {
        return buildProblemDetail(HttpStatus.NOT_FOUND, "resource-not-found", "Resource Not Found", ex.getMessage());
    }

    @ExceptionHandler(BookingConflictException.class)
    public ProblemDetail handleConflict(BookingConflictException ex) {
        return buildProblemDetail(HttpStatus.CONFLICT, "booking-conflict", "Booking Conflict", ex.getMessage());
    }

    @ExceptionHandler(ExternalEnergyDataUnavailableException.class)
    public ProblemDetail handleExternalServiceUnavailable(ExternalEnergyDataUnavailableException ex) {
        return buildProblemDetail(
                HttpStatus.SERVICE_UNAVAILABLE,
                "external-api-unavailable",
                "External Energy Data Unavailable",
                ex.getMessage()
        );
    }

    @ExceptionHandler(Exception.class)
    public ProblemDetail handleUnexpected(Exception ex) {
        log.error("Unhandled unexpected exception", ex);
        return buildProblemDetail(
                HttpStatus.INTERNAL_SERVER_ERROR,
                "internal-error",
                "Internal Server Error",
                "An unexpected error occurred while processing your request"
        );
    }

    private ProblemDetail buildProblemDetail(HttpStatus status, String typeSlug, String title, String detail) {
        ProblemDetail problem = ProblemDetail.forStatusAndDetail(status, detail);
        problem.setType(URI.create("urn:ev-charging:problem:" + typeSlug));
        problem.setTitle(title);
        problem.setProperty("timestamp", clock.instant());
        return problem;
    }
}
