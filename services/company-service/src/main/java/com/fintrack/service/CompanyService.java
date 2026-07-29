package com.fintrack.service;

import com.fintrack.Kafka.consumer.CompanyRegistrationEvent;
import com.fintrack.model.Company;
import com.fintrack.model.Sector;
import com.fintrack.repository.CompanyRepository;
import com.fintrack.repository.SectorRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class CompanyService {

    private final CompanyRepository companyRepository;
    private final SectorRepository sectorRepository;

    @Transactional
    public void createCompanyFromEvent(CompanyRegistrationEvent event) {

        //Normalize sector
        String normalizedSector = normalizeSector(event.getSector());
        log.info("Creating company: {} in sector: {}", event.getCompanyName(), normalizedSector);

        //Check if sector exists, create if not
        sectorRepository.findByName(normalizedSector)
                .orElseGet(() -> {
                    log.info("New sector detected, adding: {}", normalizedSector);
                    return sectorRepository.save(
                            Sector.builder().name(normalizedSector).build()
                    );
                });

        //Create company
        Company company = Company.builder()
                .name(event.getCompanyName())
                .sector(normalizedSector)
                .ownerId(event.getUserId())
                .build();

        companyRepository.save(company);
        log.info("Company created successfully for owner: {}", event.getUserId());
    }

    public List<String> getAllSectors() {
        return sectorRepository.findAllByOrderByNameAsc()
                .stream()
                .map(Sector::getName)
                .toList();
    }

    //"BAKERY" or "bakery" or "bAkErY" → "Bakery"
    private String normalizeSector(String sector) {
        if (sector == null || sector.isBlank()) return "Other";
        String trimmed = sector.trim().toLowerCase();
        return Character.toUpperCase(trimmed.charAt(0)) + trimmed.substring(1);
    }
}
