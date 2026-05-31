package com.portly.domain.repository;

import com.portly.domain.entity.DenunciaAgrupada;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface DenunciaAgrupadaRepository extends JpaRepository<DenunciaAgrupada, Long> {

    List<DenunciaAgrupada> findAllByOrderByCreatedAtDesc();

    Optional<DenunciaAgrupada> findByPortafolio_IdPortafolio(UUID portfolioId);
    Optional<DenunciaAgrupada> findByPortafolio_IdPortafolioAndStatus(UUID portfolioId, String status);

    List<DenunciaAgrupada> findAllByOwnerUsuario_IdUsuario(UUID userId);
    List<DenunciaAgrupada> findAllByOwnerUsuario_IdUsuarioAndStatus(UUID userId, String status);
}
