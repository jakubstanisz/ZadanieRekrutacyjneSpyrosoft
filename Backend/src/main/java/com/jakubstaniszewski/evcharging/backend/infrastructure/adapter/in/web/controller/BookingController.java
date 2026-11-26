package com.jakubstaniszewski.evcharging.backend.infrastructure.adapter.in.web.controller;

import com.jakubstaniszewski.evcharging.backend.application.dto.BookingResult;
import com.jakubstaniszewski.evcharging.backend.application.port.in.CancelBookingUseCase;
import com.jakubstaniszewski.evcharging.backend.application.port.in.CreateBookingUseCase;
import com.jakubstaniszewski.evcharging.backend.application.port.in.GetUserBookingsUseCase;
import com.jakubstaniszewski.evcharging.backend.infrastructure.adapter.in.web.dto.BookingResponse;
import com.jakubstaniszewski.evcharging.backend.infrastructure.adapter.in.web.dto.CreateBookingRequest;
import com.jakubstaniszewski.evcharging.backend.infrastructure.adapter.in.web.mapper.WebResponseMapper;
import com.jakubstaniszewski.evcharging.backend.infrastructure.adapter.in.web.security.UserContextResolver;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

/**
 * REST controller managing creation, listing, and cancellation of EV charger bookings.
 */
@RestController
@RequestMapping("/api/bookings")
public class BookingController {

    private final CreateBookingUseCase createBookingUseCase;
    private final GetUserBookingsUseCase getUserBookingsUseCase;
    private final CancelBookingUseCase cancelBookingUseCase;
    private final UserContextResolver userContextResolver;
    private final WebResponseMapper mapper;

    /**
     * Creates a new {@link BookingController}.
     *
     * @param createBookingUseCase   use case creating bookings
     * @param getUserBookingsUseCase use case listing user bookings
     * @param cancelBookingUseCase   use case cancelling bookings
     * @param userContextResolver    user identity resolver
     * @param mapper                 web DTO mapper
     */
    public BookingController(
            CreateBookingUseCase createBookingUseCase,
            GetUserBookingsUseCase getUserBookingsUseCase,
            CancelBookingUseCase cancelBookingUseCase,
            UserContextResolver userContextResolver,
            WebResponseMapper mapper
    ) {
        this.createBookingUseCase = createBookingUseCase;
        this.getUserBookingsUseCase = getUserBookingsUseCase;
        this.cancelBookingUseCase = cancelBookingUseCase;
        this.userContextResolver = userContextResolver;
        this.mapper = mapper;
    }

    /**
     * Creates a new charger booking for the requesting user.
     *
     * @param userIdHeader value of {@code X-User-Id} header
     * @param request      booking details
     * @return created booking with HTTP 201
     */
    @PostMapping
    public ResponseEntity<BookingResponse> createBooking(
            @RequestHeader(value = UserContextResolver.USER_ID_HEADER, required = false) String userIdHeader,
            @Valid @RequestBody CreateBookingRequest request
    ) {
        String userId = userContextResolver.resolveRequiredUserId(userIdHeader);
        BookingResult created = createBookingUseCase.createBooking(mapper.toCommand(request, userId));
        return ResponseEntity.status(HttpStatus.CREATED).body(mapper.toResponse(created));
    }

    /**
     * Lists all bookings owned by the requesting user.
     *
     * @param userIdHeader value of {@code X-User-Id} header
     * @return user's bookings ordered by start time
     */
    @GetMapping
    public ResponseEntity<List<BookingResponse>> getUserBookings(
            @RequestHeader(value = UserContextResolver.USER_ID_HEADER, required = false) String userIdHeader
    ) {
        String userId = userContextResolver.resolveRequiredUserId(userIdHeader);
        List<BookingResponse> responses = getUserBookingsUseCase.getUserBookings(userId).stream()
                .map(mapper::toResponse)
                .toList();
        return ResponseEntity.ok(responses);
    }

    /**
     * Cancels an existing booking owned by the requesting user.
     *
     * @param userIdHeader value of {@code X-User-Id} header
     * @param id           booking identifier
     * @return HTTP 204 No Content
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> cancelBooking(
            @RequestHeader(value = UserContextResolver.USER_ID_HEADER, required = false) String userIdHeader,
            @PathVariable UUID id
    ) {
        String userId = userContextResolver.resolveRequiredUserId(userIdHeader);
        cancelBookingUseCase.cancelBooking(id, userId);
        return ResponseEntity.noContent().build();
    }
}
