package com.portly.domain.repository;

import com.portly.domain.entity.Suspension;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface SuspensionRepository extends JpaRepository<Suspension, Long> {

    Optional<Suspension> findByUsuario_IdUsuarioAndCanceladaFalse(UUID userId);

    List<Suspension> findAllByUsuario_IdUsuarioAndCanceladaFalse(UUID userId);

    List<Suspension> findAllByCanceladaFalse();

    boolean existsByUsuario_IdUsuarioAndCanceladaFalse(UUID userId);
}
