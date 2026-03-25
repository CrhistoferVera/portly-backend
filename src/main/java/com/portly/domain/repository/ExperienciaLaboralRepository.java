package com.portly.domain.repository;

import com.portly.domain.entity.ExperienciaLaboral;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface ExperienciaLaboralRepository extends JpaRepository<ExperienciaLaboral, Integer> {

    List<ExperienciaLaboral> findByUsuario_UsuarioId(UUID usuarioId);
}
