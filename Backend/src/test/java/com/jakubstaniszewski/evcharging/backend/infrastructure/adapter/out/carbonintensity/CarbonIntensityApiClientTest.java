package com.jakubstaniszewski.evcharging.backend.infrastructure.adapter.out.carbonintensity;

import com.jakubstaniszewski.evcharging.backend.application.exception.ExternalEnergyDataUnavailableException;
import com.jakubstaniszewski.evcharging.backend.domain.model.energy.EnergySource;
import com.jakubstaniszewski.evcharging.backend.domain.model.energy.GenerationInterval;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.restclient.test.autoconfigure.RestClientTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.http.MediaType;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestClient;

import java.net.SocketTimeoutException;
import java.time.Instant;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withException;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withServerError;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess;

/**
 * Integration slice tests for {@link CarbonIntensityApiClient} using {@link MockRestServiceServer}.
 */
@RestClientTest(CarbonIntensityApiClient.class)
class CarbonIntensityApiClientTest {

    @TestConfiguration
    static class MockedRestClientConfig {
        @Bean
        RestClient carbonIntensityRestClient(RestClient.Builder builder) {
            return builder.baseUrl("https://api.carbonintensity.org.uk").build();
        }
    }

    @Autowired
    private CarbonIntensityApiClient client;

    @Autowired
    private MockRestServiceServer server;

    @Test
    void shouldParseValidCarbonIntensityResponseIncludingShortIsoTimestamps() {
        String payload = """
                {
                  "data": [
                    {
                      "from": "2026-12-01T00:00Z",
                      "to": "2026-12-01T00:30Z",
                      "generationmix": [
                        { "fuel": "wind", "perc": 42.5 },
                        { "fuel": "solar", "perc": 5.5 },
                        { "fuel": "gas", "perc": 52.0 }
                      ]
                    }
                  ]
                }
                """;

        server.expect(requestTo("https://api.carbonintensity.org.uk/generation/2026-12-01T00:00Z/2026-12-02T00:00Z"))
                .andRespond(withSuccess(payload, MediaType.APPLICATION_JSON));

        List<GenerationInterval> intervals = client.fetchGenerationMix(
                Instant.parse("2026-12-01T00:00:00Z"),
                Instant.parse("2026-12-02T00:00:00Z")
        );

        assertThat(intervals).hasSize(1);
        assertThat(intervals.getFirst().from()).isEqualTo(Instant.parse("2026-12-01T00:00:00Z"));
        assertThat(intervals.getFirst().shareOf(EnergySource.WIND)).isEqualTo(42.5);
        assertThat(intervals.getFirst().cleanEnergyPercentage()).isEqualTo(48.0);
        server.verify();
    }

    @Test
    void shouldReturnEmptyListWhenApiReturnsEmptyDataArray() {
        server.expect(requestTo("https://api.carbonintensity.org.uk/generation/2026-12-02T00:00Z/2026-12-03T00:00Z"))
                .andRespond(withSuccess("{\"data\":[]}", MediaType.APPLICATION_JSON));

        List<GenerationInterval> intervals = client.fetchGenerationMix(
                Instant.parse("2026-12-02T00:00:00Z"),
                Instant.parse("2026-12-03T00:00:00Z")
        );

        assertThat(intervals).isEmpty();
        server.verify();
    }

    @Test
    void shouldThrowExternalEnergyDataUnavailableOnServerError() {
        server.expect(requestTo("https://api.carbonintensity.org.uk/generation/2026-12-03T00:00Z/2026-12-04T00:00Z"))
                .andRespond(withServerError());

        assertThatThrownBy(() -> client.fetchGenerationMix(
                Instant.parse("2026-12-03T00:00:00Z"),
                Instant.parse("2026-12-04T00:00:00Z")
        )).isInstanceOf(ExternalEnergyDataUnavailableException.class);
        server.verify();
    }

    @Test
    void shouldThrowExternalEnergyDataUnavailableOnTimeout() {
        server.expect(requestTo("https://api.carbonintensity.org.uk/generation/2026-12-04T00:00Z/2026-12-05T00:00Z"))
                .andRespond(withException(new SocketTimeoutException("Read timed out")));

        assertThatThrownBy(() -> client.fetchGenerationMix(
                Instant.parse("2026-12-04T00:00:00Z"),
                Instant.parse("2026-12-05T00:00:00Z")
        ))
                .isInstanceOf(ExternalEnergyDataUnavailableException.class)
                .hasCauseInstanceOf(ResourceAccessException.class);
        server.verify();
    }
}
