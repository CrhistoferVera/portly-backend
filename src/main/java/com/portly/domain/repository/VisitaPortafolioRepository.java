package com.portly.domain.repository;

import com.portly.domain.entity.VisitaPortafolio;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

public interface VisitaPortafolioRepository extends JpaRepository<VisitaPortafolio, Long> {

    /** Total de visitas en un rango de fechas */
    long countByIdPortafolioAndFechaVisitaBetween(UUID idPortafolio, LocalDateTime desde, LocalDateTime hasta);

    /** Visitantes únicos en un rango de fechas */
    @Query("SELECT COUNT(DISTINCT v.visitorId) FROM VisitaPortafolio v " +
           "WHERE v.idPortafolio = :id AND v.fechaVisita BETWEEN :desde AND :hasta")
    long countDistinctVisitorsByPortafolioAndFecha(
            @Param("id") UUID idPortafolio,
            @Param("desde") LocalDateTime desde,
            @Param("hasta") LocalDateTime hasta);

    /** Suma total de duración en segundos */
    @Query("SELECT COALESCE(SUM(v.duracionSegundos), 0) FROM VisitaPortafolio v " +
           "WHERE v.idPortafolio = :id AND v.fechaVisita BETWEEN :desde AND :hasta")
    long sumDuracionByPortafolioAndFecha(
            @Param("id") UUID idPortafolio,
            @Param("desde") LocalDateTime desde,
            @Param("hasta") LocalDateTime hasta);

    /** Visitas agrupadas por hora para el gráfico */
    @Query("SELECT EXTRACT(HOUR FROM v.fechaVisita) AS hora, COUNT(v) AS total " +
           "FROM VisitaPortafolio v " +
           "WHERE v.idPortafolio = :id AND v.fechaVisita BETWEEN :desde AND :hasta " +
           "GROUP BY EXTRACT(HOUR FROM v.fechaVisita) ORDER BY hora")
    List<Object[]> countByHour(
            @Param("id") UUID idPortafolio,
            @Param("desde") LocalDateTime desde,
            @Param("hasta") LocalDateTime hasta);

    /** Visitas agrupadas por día para el gráfico (periodos largos) */
    @Query("SELECT CAST(v.fechaVisita AS DATE) AS dia, COUNT(v) AS total " +
           "FROM VisitaPortafolio v " +
           "WHERE v.idPortafolio = :id AND v.fechaVisita BETWEEN :desde AND :hasta " +
           "GROUP BY CAST(v.fechaVisita AS DATE) ORDER BY dia")
    List<Object[]> countByDay(
            @Param("id") UUID idPortafolio,
            @Param("desde") LocalDateTime desde,
            @Param("hasta") LocalDateTime hasta);
}
