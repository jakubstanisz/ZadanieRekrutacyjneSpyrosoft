package com.jakubstaniszewski.evcharging.backend.infrastructure.adapter.in.web.security;

import com.jakubstaniszewski.evcharging.backend.domain.exception.BookingValidationException;
import org.springframework.stereotype.Component;

/**
 * Resolves the acting user identity from the {@code X-User-Id} HTTP header.
 * Isolated behind a dedicated component so it can be replaced by Spring Security without changing controllers.
 */
@Component
public class UserContextResolver {

    public static final String USER_ID_HEADER = "X-User-Id";

    /**
     * Extracts and validates the mandatory user identifier from the request header.
     *
     * @param rawUserIdHeader raw value of {@code X-User-Id}
     * @return trimmed non-blank user identifier
     * @throws BookingValidationException if the header is missing or blank (mapped to HTTP 400)
     */
    public String resolveRequiredUserId(String rawUserIdHeader) {
        if (rawUserIdHeader == null || rawUserIdHeader.isBlank()) {
            throw new BookingValidationException(
                    "Missing or blank required HTTP header: " + USER_ID_HEADER
            );
        }
        return rawUserIdHeader.trim();
    }
}
