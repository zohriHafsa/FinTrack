package com.fintrack.repository;

import com.fintrack.model.Sector;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface SectorRepository extends JpaRepository<Sector, Integer> {
    Optional<Sector> findByName(String name);
    List<Sector> findAllByOrderByNameAsc();

}
