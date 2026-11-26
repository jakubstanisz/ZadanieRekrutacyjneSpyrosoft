package com.jakubstaniszewski.evcharging.backend.infrastructure.adapter.in.web.controller;

import com.jakubstaniszewski.evcharging.backend.application.port.in.GetChargerScheduleUseCase;
import com.jakubstaniszewski.evcharging.backend.application.port.in.GetChargersUseCase;
import com.jakubstaniszewski.evcharging.backend.infrastructure.adapter.in.web.dto.ChargerBookedSlotResponse;
import com.jakubstaniszewski.evcharging.backend.infrastructure.adapter.in.web.dto.ChargerResponse;
import com.jakubstaniszewski.evcharging.backend.infrastructure.adapter.in.web.mapper.WebResponseMapper;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

/**
 * REST controller exposing available EV chargers and their anonymized booked time slots.
 */
@RestController
@RequestMapping("/api/chargers")
public class ChargerController {

    private final GetChargersUseCase getChargersUseCase;
    private final GetChargerScheduleUseCase getChargerScheduleUseCase;
    private final WebResponseMapper mapper;

    /**
     * Creates a new {@link ChargerController}.
     *
     * @param getChargersUseCase        use case listing chargers
     * @param getChargerScheduleUseCase use case listing anonymized charger schedule
     * @param mapper                    web response mapper
     */
    public ChargerController(
            GetChargersUseCase getChargersUseCase,
            GetChargerScheduleUseCase getChargerScheduleUseCase,
            WebResponseMapper mapper
    ) {
        this.getChargersUseCase = getChargersUseCase;
        this.getChargerScheduleUseCase = getChargerScheduleUseCase;
        this.mapper = mapper;
    }

    /**
     * Lists all available EV chargers.
     *
     * @return list of chargers
     */
    @GetMapping
    public ResponseEntity<List<ChargerResponse>> getChargers() {
        List<ChargerResponse> response = getChargersUseCase.getChargers().stream()
                .map(mapper::toResponse)
                .toList();
        return ResponseEntity.ok(response);
    }

    /**
     * Returns anonymized booked time intervals for a specific charger so calendar views can show availability.
     *
     * @param id charger identifier
     * @return list of booked time slots
     */
    @GetMapping("/{id}/bookings")
    public ResponseEntity<List<ChargerBookedSlotResponse>> getChargerBookedSlots(@PathVariable UUID id) {
        List<ChargerBookedSlotResponse> response = getChargerScheduleUseCase.getChargerBookedSlots(id).stream()
                .map(mapper::toResponse)
                .toList();
        return ResponseEntity.ok(response);
    }
}
