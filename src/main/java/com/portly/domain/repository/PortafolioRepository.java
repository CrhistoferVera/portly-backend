package com.portly.domain.repository;

import com.portly.domain.entity.Portafolio;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
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
           "OR LOWER(pf.titularProfesional) LIKE LOWER(CONCAT('%', :q, '%')))")
    Page<Portafolio> searchPublicPortafolios(@Param("q") String q, Pageable pageable);
}
