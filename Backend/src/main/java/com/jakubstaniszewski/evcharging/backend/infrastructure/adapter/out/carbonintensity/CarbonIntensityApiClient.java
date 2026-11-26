package com.jakubstaniszewski.evcharging.backend.infrastructure.adapter.out.carbonintensity;

import com.jakubstaniszewski.evcharging.backend.application.exception.ExternalEnergyDataUnavailableException;
import com.jakubstaniszewski.evcharging.backend.domain.model.energy.EnergySource;
import com.jakubstaniszewski.evcharging.backend.domain.model.energy.GenerationInterval;
import com.jakubstaniszewski.evcharging.backend.domain.port.GenerationMixProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;

import java.time.Instant;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

/**
 * Infrastructure adapter implementing {@link GenerationMixProvider} over the UK Carbon Intensity REST API.
 * Uses Caffeine-backed Spring Cache to reduce external requests and protect against rate limiting.
 */
@Component
public class CarbonIntensityApiClient implements GenerationMixProvider {

    private static final Logger log = LoggerFactory.getLogger(CarbonIntensityApiClient.class);
    private static final DateTimeFormatter API_TIMESTAMP_FORMAT =
            DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm'Z'").withZone(ZoneOffset.UTC);

    private final RestClient restClient;

    /**
     * Creates a new {@link CarbonIntensityApiClient}.
     *
     * @param carbonIntensityRestClient configured {@link RestClient} for the Carbon Intensity API
     */
    public CarbonIntensityApiClient(RestClient carbonIntensityRestClient) {
        this.restClient = carbonIntensityRestClient;
    }

    @Override
    @Cacheable(cacheNames = "generation-mix", key = "#from.toString() + '_' + #to.toString()")
    public List<GenerationInterval> fetchGenerationMix(Instant from, Instant to) {
        String fromParam = API_TIMESTAMP_FORMAT.format(from);
        String toParam = API_TIMESTAMP_FORMAT.format(to);

        CarbonIntensityApiResponse response = executeRequest(fromParam, toParam);
        if (response == null || response.data() == null || response.data().isEmpty()) {
            log.warn("Carbon Intensity API returned empty data for range [{} - {}]", fromParam, toParam);
            return List.of();
        }

        return response.data().stream()
                .map(this::toDomainInterval)
                .flatMap(Optional::stream)
                .toList();
    }

    private CarbonIntensityApiResponse executeRequest(String fromParam, String toParam) {
        try {
            return restClient.get()
                    .uri("/generation/{from}/{to}", fromParam, toParam)
                    .accept(MediaType.APPLICATION_JSON)
                    .retrieve()
                    .body(CarbonIntensityApiResponse.class);
        } catch (RestClientException ex) {
            log.error("Failed to fetch generation mix from Carbon Intensity API for [{} - {}]", fromParam, toParam, ex);
            throw new ExternalEnergyDataUnavailableException(
                    "UK Carbon Intensity API is currently unavailable", ex
            );
        }
    }

    private Optional<GenerationInterval> toDomainInterval(GenerationSlotResponse slot) {
        if (slot == null || slot.from() == null || slot.to() == null) {
            return Optional.empty();
        }
        try {
            Instant from = parseUtcTimestamp(slot.from());
            Instant to = parseUtcTimestamp(slot.to());
            if (!to.isAfter(from)) {
                return Optional.empty();
            }
            Map<EnergySource, Double> fuelShares = mapFuelShares(slot.generationmix());
            return Optional.of(new GenerationInterval(from, to, fuelShares));
        } catch (DateTimeParseException | IllegalArgumentException ex) {
            log.warn("Skipping malformed generation slot from Carbon Intensity API: {}", slot, ex);
            return Optional.empty();
        }
    }

    private Instant parseUtcTimestamp(String rawTimestamp) {
        return OffsetDateTime.parse(rawTimestamp.trim()).toInstant();
    }

    private Map<EnergySource, Double> mapFuelShares(List<GenerationMixEntryResponse> entries) {
        Map<EnergySource, Double> shares = new EnumMap<>(EnergySource.class);
        if (entries == null) {
            return shares;
        }
        entries.stream()
                .filter(Objects::nonNull)
                .forEach(entry -> {
                    EnergySource source = EnergySource.fromApiName(entry.fuel());
                    shares.merge(source, entry.perc(), Double::sum);
                });
        return shares;
    }
}
