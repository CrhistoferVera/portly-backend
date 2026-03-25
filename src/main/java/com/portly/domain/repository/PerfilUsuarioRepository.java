package com.portly.domain.repository;

import com.portly.domain.entity.PerfilUsuario;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface PerfilUsuarioRepository extends JpaRepository<PerfilUsuario, Integer> {

    Optional<PerfilUsuario> findByUsuario_UsuarioId(UUID usuarioId);
}
