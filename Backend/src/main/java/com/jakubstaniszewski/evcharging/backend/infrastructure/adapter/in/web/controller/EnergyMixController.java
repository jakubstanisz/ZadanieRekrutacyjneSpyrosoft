package com.jakubstaniszewski.evcharging.backend.infrastructure.adapter.in.web.controller;

import com.jakubstaniszewski.evcharging.backend.application.port.in.GetEnergyMixUseCase;
import com.jakubstaniszewski.evcharging.backend.infrastructure.adapter.in.web.dto.EnergyMixResponse;
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
 * REST controller exposing UK electricity generation mix summaries.
 */
@RestController
@RequestMapping("/api/energy-mix")
@Validated
public class EnergyMixController {

    private final GetEnergyMixUseCase getEnergyMixUseCase;
    private final WebResponseMapper mapper;

    /**
     * Creates a new {@link EnergyMixController}.
     *
     * @param getEnergyMixUseCase use case retrieving daily energy mix
     * @param mapper              web response mapper
     */
    public EnergyMixController(GetEnergyMixUseCase getEnergyMixUseCase, WebResponseMapper mapper) {
        this.getEnergyMixUseCase = getEnergyMixUseCase;
        this.mapper = mapper;
    }

    /**
     * Returns daily UK generation mix summaries starting from today (UTC).
     *
     * @param days number of days to return (1–3, defaults to 3)
     * @return daily energy mix summaries and cleanest date
     */
    @GetMapping
    public ResponseEntity<EnergyMixResponse> getEnergyMix(
            @RequestParam(defaultValue = "3")
            @Min(value = 1, message = "days must be at least 1")
            @Max(value = 3, message = "days must be at most 3")
            int days
    ) {
        return ResponseEntity.ok(mapper.toResponse(getEnergyMixUseCase.getEnergyMix(days)));
    }
}
