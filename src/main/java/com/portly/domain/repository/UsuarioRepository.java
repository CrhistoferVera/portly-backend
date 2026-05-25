package com.portly.domain.repository;

import com.portly.domain.entity.Usuario;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface UsuarioRepository extends JpaRepository<Usuario, UUID> {

    Optional<Usuario> findByEmail(String email);

    boolean existsByEmail(String email);

    @Query("SELECT u FROM Usuario u WHERE u.fechaCreacion >= :desde AND u.fechaCreacion <= :hasta AND (:estado IS NULL OR u.estado = :estado)")
    List<Usuario> findByFechaCreacionBetweenAndEstado(
        @Param("desde") LocalDateTime desde,
        @Param("hasta") LocalDateTime hasta,
        @Param("estado") String estado
    );

    @Query("SELECT COUNT(u) FROM Usuario u WHERE u.fechaCreacion >= :desde")
    long countByFechaCreacionAfter(@Param("desde") LocalDateTime desde);

    @Query("SELECT COUNT(u) FROM Usuario u WHERE LOWER(u.estado) = 'suspendido'")
    long countSuspendidos();
}
