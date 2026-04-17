package com.portly.domain.repository;

import com.portly.domain.entity.HabilidadCatalogo;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface HabilidadCatalogoRepository extends JpaRepository<HabilidadCatalogo, Long> {

    List<HabilidadCatalogo> findByActivoTrueOrderByNombreAsc();

    List<HabilidadCatalogo> findByNombreContainingIgnoreCaseAndActivoTrue(String nombre);

    boolean existsByNombreIgnoreCase(String nombre);

    Optional<HabilidadCatalogo> findByNombreIgnoreCase(String nombre);
}
