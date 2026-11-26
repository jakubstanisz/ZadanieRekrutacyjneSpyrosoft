package com.jakubstaniszewski.evcharging.backend.infrastructure.adapter.in.web.controller;

import com.jakubstaniszewski.evcharging.backend.application.dto.BookingResult;
import com.jakubstaniszewski.evcharging.backend.application.dto.ChargerBookedSlotResult;
import com.jakubstaniszewski.evcharging.backend.application.exception.ExternalEnergyDataUnavailableException;
import com.jakubstaniszewski.evcharging.backend.application.port.in.CancelBookingUseCase;
import com.jakubstaniszewski.evcharging.backend.application.port.in.CreateBookingUseCase;
import com.jakubstaniszewski.evcharging.backend.application.port.in.FindBestChargingWindowUseCase;
import com.jakubstaniszewski.evcharging.backend.application.port.in.GetChargerScheduleUseCase;
import com.jakubstaniszewski.evcharging.backend.application.port.in.GetChargersUseCase;
import com.jakubstaniszewski.evcharging.backend.application.port.in.GetEnergyMixUseCase;
import com.jakubstaniszewski.evcharging.backend.application.port.in.GetUserBookingsUseCase;
import com.jakubstaniszewski.evcharging.backend.domain.exception.BookingConflictException;
import com.jakubstaniszewski.evcharging.backend.domain.exception.ForbiddenActionException;
import com.jakubstaniszewski.evcharging.backend.infrastructure.adapter.in.web.mapper.WebResponseMapper;
import com.jakubstaniszewski.evcharging.backend.infrastructure.adapter.in.web.security.UserContextResolver;
import com.jakubstaniszewski.evcharging.backend.infrastructure.config.ClockConfig;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Web MVC slice tests verifying REST contracts, validation, headers, and ProblemDetail mappings.
 */
@WebMvcTest(controllers = {
        BookingController.class,
        ChargerController.class,
        EnergyMixController.class,
        ChargingWindowController.class
})
@Import({ClockConfig.class, UserContextResolver.class, WebResponseMapper.class})
class BookingControllerWebMvcTest {

    private static final UUID CHARGER_ID = UUID.fromString("11111111-1111-1111-1111-111111111101");

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private CreateBookingUseCase createBookingUseCase;

    @MockitoBean
    private GetUserBookingsUseCase getUserBookingsUseCase;

    @MockitoBean
    private CancelBookingUseCase cancelBookingUseCase;

    @MockitoBean
    private GetChargersUseCase getChargersUseCase;

    @MockitoBean
    private GetChargerScheduleUseCase getChargerScheduleUseCase;

    @MockitoBean
    private GetEnergyMixUseCase getEnergyMixUseCase;

    @MockitoBean
    private FindBestChargingWindowUseCase findBestChargingWindowUseCase;

    @Test
    void shouldReturnCreatedWhenBookingRequestIsValid() throws Exception {
        UUID bookingId = UUID.randomUUID();
        Instant start = Instant.parse("2026-12-01T12:00:00Z");
        Instant end = Instant.parse("2026-12-01T14:00:00Z");

        when(createBookingUseCase.createBooking(any())).thenReturn(new BookingResult(
                bookingId, CHARGER_ID, "Hub-Alpha", "London", "user-1", start, end, 2, Instant.now()
        ));

        String body = """
                {
                  "chargerId": "%s",
                  "startTime": "2026-12-01T12:00:00Z",
                  "endTime": "2026-12-01T14:00:00Z"
                }
                """.formatted(CHARGER_ID);

        mockMvc.perform(post("/api/bookings")
                        .header("X-User-Id", "user-1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(bookingId.toString()))
                .andExpect(jsonPath("$.durationHours").value(2));
    }

    @Test
    void shouldReturnBadRequestWhenUserIdHeaderIsMissing() throws Exception {
        String body = """
                {
                  "chargerId": "%s",
                  "startTime": "2026-12-01T12:00:00Z",
                  "endTime": "2026-12-01T14:00:00Z"
                }
                """.formatted(CHARGER_ID);

        mockMvc.perform(post("/api/bookings")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.title").value("Validation Error"));
    }

    @Test
    void shouldReturnBadRequestWithFieldErrorsWhenRequestBodyInvalid() throws Exception {
        mockMvc.perform(post("/api/bookings")
                        .header("X-User-Id", "user-1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors.chargerId").exists());
    }

    @Test
    void shouldReturnConflictProblemDetailWhenSlotAlreadyBooked() throws Exception {
        when(createBookingUseCase.createBooking(any()))
                .thenThrow(new BookingConflictException("Charger already booked"));

        String body = """
                {
                  "chargerId": "%s",
                  "startTime": "2026-12-01T12:00:00Z",
                  "endTime": "2026-12-01T14:00:00Z"
                }
                """.formatted(CHARGER_ID);

        mockMvc.perform(post("/api/bookings")
                        .header("X-User-Id", "user-1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.title").value("Booking Conflict"));
    }

    @Test
    void shouldReturnAnonymizedBookedSlotsForChargerCalendar() throws Exception {
        when(getChargerScheduleUseCase.getChargerBookedSlots(CHARGER_ID))
                .thenReturn(List.of(new ChargerBookedSlotResult(
                        Instant.parse("2026-12-01T10:00:00Z"),
                        Instant.parse("2026-12-01T12:00:00Z")
                )));

        mockMvc.perform(get("/api/chargers/{id}/bookings", CHARGER_ID))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].startTime").value("2026-12-01T10:00:00Z"))
                .andExpect(jsonPath("$[0].userId").doesNotExist());
    }

    @Test
    void shouldReturnForbiddenWhenCancellingAnotherUsersBooking() throws Exception {
        UUID bookingId = UUID.randomUUID();
        doThrow(new ForbiddenActionException("Not owner")).when(cancelBookingUseCase).cancelBooking(bookingId, "bob");

        mockMvc.perform(delete("/api/bookings/{id}", bookingId).header("X-User-Id", "bob"))
                .andExpect(status().isForbidden());
    }

    @Test
    void shouldReturnBadRequestWhenChargingWindowHoursOutOfBounds() throws Exception {
        mockMvc.perform(get("/api/charging-window").param("hours", "7"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.title").value("Parameter Validation Failed"));
    }

    @Test
    void shouldReturnServiceUnavailableWhenExternalEnergyApiFails() throws Exception {
        when(getEnergyMixUseCase.getEnergyMix(3))
                .thenThrow(new ExternalEnergyDataUnavailableException("API down"));

        mockMvc.perform(get("/api/energy-mix"))
                .andExpect(status().isServiceUnavailable())
                .andExpect(jsonPath("$.title").value("External Energy Data Unavailable"));
    }
}
