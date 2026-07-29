package com.fintrack.config;

import com.fintrack.model.Sector;
import com.fintrack.repository.SectorRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@RequiredArgsConstructor
@Slf4j
public class SectorDataLoader implements CommandLineRunner {

    private final SectorRepository sectorRepository;

    @Override
    public void run(String... args) {
        if (sectorRepository.count() == 0) {
            log.info("Seeding sectors...");
            List<Sector> sectors = List.of(
                    Sector.builder().name("Agriculture").build(),
                    Sector.builder().name("Bakery").build(),
                    Sector.builder().name("Construction").build(),
                    Sector.builder().name("Consulting").build(),
                    Sector.builder().name("Education").build(),
                    Sector.builder().name("Food & Beverage").build(),
                    Sector.builder().name("Healthcare").build(),
                    Sector.builder().name("Retail").build(),
                    Sector.builder().name("Restaurant").build(),
                    Sector.builder().name("Technology").build(),
                    Sector.builder().name("Transport").build(),
                    Sector.builder().name("Wholesale").build(),
                    Sector.builder().name("Other").build()
            );
            sectorRepository.saveAll(sectors);
            log.info("Seeded {} sectors", sectors.size());
        } else {
            log.info("Sectors already seeded, skipping.");
        }
    }
}
