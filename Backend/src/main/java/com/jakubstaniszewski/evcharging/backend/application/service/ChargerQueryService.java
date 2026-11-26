package com.jakubstaniszewski.evcharging.backend.application.service;

import com.jakubstaniszewski.evcharging.backend.application.dto.ChargerBookedSlotResult;
import com.jakubstaniszewski.evcharging.backend.application.dto.ChargerResult;
import com.jakubstaniszewski.evcharging.backend.application.port.in.GetChargerScheduleUseCase;
import com.jakubstaniszewski.evcharging.backend.application.port.in.GetChargersUseCase;
import com.jakubstaniszewski.evcharging.backend.domain.exception.BookingValidationException;
import com.jakubstaniszewski.evcharging.backend.domain.exception.ResourceNotFoundException;
import com.jakubstaniszewski.evcharging.backend.domain.model.booking.Charger;
import com.jakubstaniszewski.evcharging.backend.domain.port.BookingRepository;
import com.jakubstaniszewski.evcharging.backend.domain.port.ChargerRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

/**
 * Application query service for listing EV chargers and their anonymized booked intervals.
 */
@Service
@Transactional(readOnly = true)
public class ChargerQueryService implements GetChargersUseCase, GetChargerScheduleUseCase {

    private final ChargerRepository chargerRepository;
    private final BookingRepository bookingRepository;

    /**
     * Creates a new {@link ChargerQueryService}.
     *
     * @param chargerRepository repository for charger lookup
     * @param bookingRepository repository for charger schedule lookup
     */
    public ChargerQueryService(ChargerRepository chargerRepository, BookingRepository bookingRepository) {
        this.chargerRepository = chargerRepository;
        this.bookingRepository = bookingRepository;
    }

    @Override
    public List<ChargerResult> getChargers() {
        return chargerRepository.findAll().stream()
                .map(this::toChargerResult)
                .toList();
    }

    @Override
    public List<ChargerBookedSlotResult> getChargerBookedSlots(UUID chargerId) {
        if (chargerId == null) {
            throw new BookingValidationException("Charger id must not be null");
        }
        chargerRepository.findById(chargerId)
                .orElseThrow(() -> new ResourceNotFoundException("Charger not found: " + chargerId));

        return bookingRepository.findByChargerId(chargerId).stream()
                .map(booking -> new ChargerBookedSlotResult(
                        booking.getTimeRange().startTime(),
                        booking.getTimeRange().endTime()
                ))
                .toList();
    }

    private ChargerResult toChargerResult(Charger charger) {
        return new ChargerResult(
                charger.id(),
                charger.name(),
                charger.location(),
                charger.connectorType(),
                charger.powerKw()
        );
    }
}
