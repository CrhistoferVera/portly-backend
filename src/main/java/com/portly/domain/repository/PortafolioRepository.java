package com.portly.domain.repository;

import com.portly.domain.entity.Portafolio;
import com.portly.dto.DashboardStatsResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

public interface PortafolioRepository extends JpaRepository<Portafolio, UUID> {

    List<Portafolio> findByUsuario_IdUsuarioOrderByFechaCreacionDesc(UUID idUsuario);

    List<Portafolio> findByPlantilla_IdPlantilla(String idPlantilla);

    boolean existsByUrlPublica(String urlPublica);

    boolean existsByUsuario_IdUsuarioAndNombreIgnoreCase(UUID idUsuario, String nombre);

    java.util.Optional<Portafolio> findByUrlPublicaEndingWith(String suffix);

    @Query("SELECT p FROM Portafolio p JOIN p.usuario u LEFT JOIN u.perfil pf " +
           "WHERE p.visibilidad = 'PUBLICO' " +
           "AND (:q IS NULL OR :q = '' " +
           "OR LOWER(p.nombre) LIKE LOWER(CONCAT('%', :q, '%')) " +
           "OR LOWER(pf.nombre) LIKE LOWER(CONCAT('%', :q, '%')) " +
           "OR LOWER(pf.apellido) LIKE LOWER(CONCAT('%', :q, '%')) " +
           "OR LOWER(pf.titularProfesional) LIKE LOWER(CONCAT('%', :q, '%'))) " +
           "AND (:nacionalidad IS NULL OR :nacionalidad = '' OR pf.pais = :nacionalidad) " +
           "AND (:gradoAcademico IS NULL OR :gradoAcademico = '' OR EXISTS (SELECT 1 FROM u.formaciones f WHERE f.nivel = :gradoAcademico)) " +
           "AND (:habilidadesTecnicas IS NULL OR :habilidadesTecnicas = '' OR EXISTS (SELECT 1 FROM u.habilidades h WHERE h.nombre = :habilidadesTecnicas)) " +
           "AND (:habilidadesBlandas IS NULL OR :habilidadesBlandas = '' OR EXISTS (SELECT 1 FROM u.habilidadesBlandas hb WHERE hb.nombreHabilidad = :habilidadesBlandas))")
    Page<Portafolio> searchPublicPortafolios(
            @Param("q") String q,
            @Param("nacionalidad") String nacionalidad,
            @Param("gradoAcademico") String gradoAcademico,
            @Param("habilidadesTecnicas") String habilidadesTecnicas,
            @Param("habilidadesBlandas") String habilidadesBlandas,
            Pageable pageable);

    @Query("SELECT COUNT(p) FROM Portafolio p WHERE p.visibilidad = 'PUBLICO' AND p.fechaCreacion >= :desde")
    long countPublicosDesde(@Param("desde") LocalDateTime desde);

    @Query("SELECT new com.portly.dto.DashboardStatsResponse$PlantillaStats(p.plantilla.nombre, COUNT(p)) " +
           "FROM Portafolio p GROUP BY p.plantilla.nombre ORDER BY COUNT(p) DESC")
    List<DashboardStatsResponse.PlantillaStats> findTopPlantillas(Pageable pageable);
}
