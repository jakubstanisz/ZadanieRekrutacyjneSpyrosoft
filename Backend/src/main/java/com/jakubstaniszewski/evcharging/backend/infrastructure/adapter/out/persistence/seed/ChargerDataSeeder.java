package com.jakubstaniszewski.evcharging.backend.infrastructure.adapter.out.persistence.seed;

import com.jakubstaniszewski.evcharging.backend.domain.model.booking.Charger;
import com.jakubstaniszewski.evcharging.backend.domain.port.ChargerRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

/**
 * Seeds initial EV chargers on application startup when the {@code chargers} table is empty.
 */
@Component
public class ChargerDataSeeder implements CommandLineRunner {

    private static final Logger log = LoggerFactory.getLogger(ChargerDataSeeder.class);

    private final ChargerRepository chargerRepository;

    /**
     * Creates a new {@link ChargerDataSeeder}.
     *
     * @param chargerRepository charger persistence port
     */
    public ChargerDataSeeder(ChargerRepository chargerRepository) {
        this.chargerRepository = chargerRepository;
    }

    @Override
    @Transactional
    public void run(String... args) {
        if (chargerRepository.count() > 0) {
            return;
        }

        List<Charger> initialChargers = List.of(
                new Charger(
                        UUID.fromString("11111111-1111-1111-1111-111111111101"),
                        "Hub-Alpha 150kW",
                        "London Paddington Green Hub, Bay 1",
                        "CCS2",
                        150
                ),
                new Charger(
                        UUID.fromString("11111111-1111-1111-1111-111111111102"),
                        "Hub-Beta 22kW",
                        "Manchester Piccadilly Eco Park, Bay 4",
                        "Type 2",
                        22
                ),
                new Charger(
                        UUID.fromString("11111111-1111-1111-1111-111111111103"),
                        "Hub-Gamma 350kW",
                        "Edinburgh Waverley Ultra-Fast Station, Bay 2",
                        "CCS2",
                        350
                ),
                new Charger(
                        UUID.fromString("11111111-1111-1111-1111-111111111104"),
                        "Hub-Delta 50kW",
                        "Bristol Harbourside Solar Canopy, Bay 3",
                        "CHAdeMO / CCS2",
                        50
                )
        );

        chargerRepository.saveAll(initialChargers);
        log.info("Seeded {} initial EV chargers into database", initialChargers.size());
    }
}
