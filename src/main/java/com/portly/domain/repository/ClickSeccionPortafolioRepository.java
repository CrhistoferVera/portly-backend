package com.portly.domain.repository;

import com.portly.domain.entity.ClickSeccionPortafolio;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

public interface ClickSeccionPortafolioRepository extends JpaRepository<ClickSeccionPortafolio, Long> {

    /** Clics agrupados por sección con nombre para ranking */
    @Query("SELECT c.idReferencia, c.nombreReferencia, COUNT(c) FROM ClickSeccionPortafolio c " +
           "WHERE c.idPortafolio = :id AND c.tipoSeccion = :tipo " +
           "AND c.fechaClick BETWEEN :desde AND :hasta " +
           "GROUP BY c.idReferencia, c.nombreReferencia ORDER BY COUNT(c) DESC")
    List<Object[]> countBySeccion(
            @Param("id") UUID idPortafolio,
            @Param("tipo") String tipoSeccion,
            @Param("desde") LocalDateTime desde,
            @Param("hasta") LocalDateTime hasta);
}
