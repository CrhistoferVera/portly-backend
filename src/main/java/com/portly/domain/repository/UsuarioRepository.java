package com.portly.domain.repository;

import com.portly.domain.entity.Usuario;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface UsuarioRepository extends JpaRepository<Usuario, UUID> {

    Optional<Usuario> findByEmail(String email);

    boolean existsByEmail(String email);

    @org.springframework.data.jpa.repository.Query("SELECT u FROM Usuario u WHERE u.fechaCreacion >= :desde AND u.fechaCreacion <= :hasta AND (:estado IS NULL OR u.estado = :estado)")
    java.util.List<Usuario> findByFechaCreacionBetweenAndEstado(
        @org.springframework.data.repository.query.Param("desde") java.time.LocalDateTime desde,
        @org.springframework.data.repository.query.Param("hasta") java.time.LocalDateTime hasta,
        @org.springframework.data.repository.query.Param("estado") String estado
    );
}
