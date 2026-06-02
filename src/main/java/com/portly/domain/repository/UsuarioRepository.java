package com.portly.domain.repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.portly.domain.entity.Usuario;

public interface UsuarioRepository extends JpaRepository<Usuario, UUID> {

    Optional<Usuario> findByEmail(String email);

    boolean existsByEmail(String email);

    Optional<Usuario> findByUsernameIgnoreCase(String username);

    boolean existsByUsernameIgnoreCase(String username);

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

    @Query("""
        SELECT COUNT(u) FROM Usuario u
        WHERE LOWER(u.estado) IN ('suspendido', 'suspendida')
           OR EXISTS (
               SELECT 1 FROM Suspension s
               WHERE s.usuario.idUsuario = u.idUsuario AND s.cancelada = false
           )
        """)
    long countCuentasBloqueadas();
}
