package com.jakubstaniszewski.evcharging.backend.infrastructure.adapter.in.web.mapper;

import com.jakubstaniszewski.evcharging.backend.application.dto.BookingResult;
import com.jakubstaniszewski.evcharging.backend.application.dto.ChargerBookedSlotResult;
import com.jakubstaniszewski.evcharging.backend.application.dto.ChargerResult;
import com.jakubstaniszewski.evcharging.backend.application.dto.ChargingWindowResult;
import com.jakubstaniszewski.evcharging.backend.application.dto.CreateBookingCommand;
import com.jakubstaniszewski.evcharging.backend.application.dto.DailyEnergyMixResult;
import com.jakubstaniszewski.evcharging.backend.application.dto.EnergyMixResult;
import com.jakubstaniszewski.evcharging.backend.domain.model.energy.EnergySource;
import com.jakubstaniszewski.evcharging.backend.infrastructure.adapter.in.web.dto.BookingResponse;
import com.jakubstaniszewski.evcharging.backend.infrastructure.adapter.in.web.dto.ChargerBookedSlotResponse;
import com.jakubstaniszewski.evcharging.backend.infrastructure.adapter.in.web.dto.ChargerResponse;
import com.jakubstaniszewski.evcharging.backend.infrastructure.adapter.in.web.dto.ChargingWindowResponse;
import com.jakubstaniszewski.evcharging.backend.infrastructure.adapter.in.web.dto.CreateBookingRequest;
import com.jakubstaniszewski.evcharging.backend.infrastructure.adapter.in.web.dto.DailyEnergyMixResponse;
import com.jakubstaniszewski.evcharging.backend.infrastructure.adapter.in.web.dto.EnergyMixResponse;
import org.springframework.stereotype.Component;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Maps between HTTP Web DTOs and application-layer commands and results.
 */
@Component
public class WebResponseMapper {

    /**
     * Converts a web request and resolved user identity into an application booking command.
     *
     * @param request HTTP request payload
     * @param userId  resolved user identifier
     * @return application command
     */
    public CreateBookingCommand toCommand(CreateBookingRequest request, String userId) {
        return new CreateBookingCommand(
                request.chargerId(),
                userId,
                request.startTime(),
                request.endTime()
        );
    }

    /**
     * Converts an application {@link EnergyMixResult} into an HTTP response DTO.
     *
     * @param result application result
     * @return HTTP response DTO
     */
    public EnergyMixResponse toResponse(EnergyMixResult result) {
        List<DailyEnergyMixResponse> days = result.days().stream()
                .map(this::toDailyResponse)
                .toList();
        return new EnergyMixResponse(days, result.cleanestDate());
    }

    /**
     * Converts an application {@link ChargingWindowResult} into an HTTP response DTO.
     *
     * @param result application result
     * @return HTTP response DTO
     */
    public ChargingWindowResponse toResponse(ChargingWindowResult result) {
        return new ChargingWindowResponse(
                result.startTime(),
                result.endTime(),
                result.cleanEnergyPercentage(),
                result.partialData()
        );
    }

    /**
     * Converts an application {@link ChargerResult} into an HTTP response DTO.
     *
     * @param result application result
     * @return HTTP response DTO
     */
    public ChargerResponse toResponse(ChargerResult result) {
        return new ChargerResponse(
                result.id(),
                result.name(),
                result.location(),
                result.connectorType(),
                result.powerKw()
        );
    }

    /**
     * Converts an anonymized {@link ChargerBookedSlotResult} into an HTTP response DTO.
     *
     * @param result application slot result
     * @return HTTP response DTO
     */
    public ChargerBookedSlotResponse toResponse(ChargerBookedSlotResult result) {
        return new ChargerBookedSlotResponse(result.startTime(), result.endTime());
    }

    /**
     * Converts an application {@link BookingResult} into an HTTP response DTO.
     *
     * @param result application booking result
     * @return HTTP response DTO
     */
    public BookingResponse toResponse(BookingResult result) {
        return new BookingResponse(
                result.id(),
                result.chargerId(),
                result.chargerName(),
                result.chargerLocation(),
                result.userId(),
                result.startTime(),
                result.endTime(),
                result.durationHours(),
                result.createdAt()
        );
    }

    private DailyEnergyMixResponse toDailyResponse(DailyEnergyMixResult day) {
        Map<String, Double> orderedShares = new LinkedHashMap<>();
        for (EnergySource source : EnergySource.values()) {
            Double share = day.averageFuelShares().getOrDefault(source, 0.0);
            orderedShares.put(source.apiName(), share);
        }
        return new DailyEnergyMixResponse(
                day.date(),
                orderedShares,
                day.cleanEnergyPercentage(),
                day.availableIntervals(),
                day.expectedIntervals(),
                day.completenessStatus()
        );
    }
}
