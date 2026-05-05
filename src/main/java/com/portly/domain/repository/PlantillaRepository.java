package com.portly.domain.repository;

import com.portly.domain.entity.Plantilla;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface PlantillaRepository extends JpaRepository<Plantilla, String> {
    Optional<Plantilla> findByNombre(String nombre);
}
