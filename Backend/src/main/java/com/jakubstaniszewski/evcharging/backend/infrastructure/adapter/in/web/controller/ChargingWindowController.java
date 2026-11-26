package com.jakubstaniszewski.evcharging.backend.infrastructure.adapter.in.web.controller;

import com.jakubstaniszewski.evcharging.backend.application.port.in.FindBestChargingWindowUseCase;
import com.jakubstaniszewski.evcharging.backend.infrastructure.adapter.in.web.dto.ChargingWindowResponse;
import com.jakubstaniszewski.evcharging.backend.infrastructure.adapter.in.web.mapper.WebResponseMapper;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * REST controller exposing the optimal clean-energy charging window endpoint.
 */
@RestController
@RequestMapping("/api/charging-window")
@Validated
public class ChargingWindowController {

    private final FindBestChargingWindowUseCase findBestChargingWindowUseCase;
    private final WebResponseMapper mapper;

    /**
     * Creates a new {@link ChargingWindowController}.
     *
     * @param findBestChargingWindowUseCase use case finding the optimal window
     * @param mapper                        web response mapper
     */
    public ChargingWindowController(
            FindBestChargingWindowUseCase findBestChargingWindowUseCase,
            WebResponseMapper mapper
    ) {
        this.findBestChargingWindowUseCase = findBestChargingWindowUseCase;
        this.mapper = mapper;
    }

    /**
     * Finds the cleanest continuous charging window of {@code hours} hours across the next two days (UTC).
     *
     * @param hours charging duration in whole hours (1–6)
     * @return optimal window start, end, and clean energy share
     */
    @GetMapping
    public ResponseEntity<ChargingWindowResponse> getBestChargingWindow(
            @RequestParam
            @Min(value = 1, message = "hours must be at least 1")
            @Max(value = 6, message = "hours must be at most 6")
            int hours
    ) {
        return ResponseEntity.ok(mapper.toResponse(findBestChargingWindowUseCase.findBestWindow(hours)));
    }
}
