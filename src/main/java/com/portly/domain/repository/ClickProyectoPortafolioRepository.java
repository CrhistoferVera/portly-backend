package com.portly.domain.repository;

import com.portly.domain.entity.ClickProyectoPortafolio;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

public interface ClickProyectoPortafolioRepository extends JpaRepository<ClickProyectoPortafolio, Long> {

    /** Total de clics en proyectos para un portafolio */
    long countByIdPortafolioAndFechaClickBetween(UUID idPortafolio, LocalDateTime desde, LocalDateTime hasta);

    /** Clics agrupados por proyecto (para ranking) */
    @Query("SELECT c.idProyecto, p.titulo, COUNT(c) FROM ClickProyectoPortafolio c " +
           "JOIN Proyecto p ON c.idProyecto = p.idProyecto " +
           "WHERE c.idPortafolio = :id AND c.fechaClick BETWEEN :desde AND :hasta " +
           "GROUP BY c.idProyecto, p.titulo ORDER BY COUNT(c) DESC")
    List<Object[]> countByProyecto(
            @Param("id") UUID idPortafolio,
            @Param("desde") LocalDateTime desde,
            @Param("hasta") LocalDateTime hasta);
}
