package com.portly.domain.repository;

import com.portly.domain.entity.PerfilUsuario;
import com.portly.dto.DashboardStatsResponse;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface PerfilUsuarioRepository extends JpaRepository<PerfilUsuario, Integer> {

    Optional<PerfilUsuario> findByUsuario_IdUsuario(UUID idUsuario);

    @Query("SELECT new com.portly.dto.DashboardStatsResponse$ProfesionStats(pu.titularProfesional, COUNT(pu)) " +
           "FROM PerfilUsuario pu WHERE pu.titularProfesional IS NOT NULL AND pu.titularProfesional <> '' " +
           "GROUP BY pu.titularProfesional ORDER BY COUNT(pu) DESC")
    List<DashboardStatsResponse.ProfesionStats> findTopProfesiones(Pageable pageable);
}
