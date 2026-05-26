package com.portly.domain.repository;

import com.portly.domain.entity.HabilidadBlanda;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface HabilidadBlandaRepository extends JpaRepository<HabilidadBlanda, Integer> {

    List<HabilidadBlanda> findByUsuario_IdUsuario(UUID idUsuario);

    boolean existsByUsuario_IdUsuarioAndNombreHabilidadIgnoreCase(UUID idUsuario, String nombreHabilidad);

    @org.springframework.data.jpa.repository.Query("SELECT new com.portly.dto.SkillReportDto(hb.nombreHabilidad, 'Blandas', COUNT(hb)) " +
            "FROM HabilidadBlanda hb " +
            "WHERE hb.fechaCreacion BETWEEN :desde AND :hasta " +
            "GROUP BY hb.nombreHabilidad")
    List<com.portly.dto.SkillReportDto> getSkillReport(java.time.LocalDateTime desde, java.time.LocalDateTime hasta);
}
