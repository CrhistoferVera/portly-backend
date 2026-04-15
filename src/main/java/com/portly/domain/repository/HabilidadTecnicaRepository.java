package com.portly.domain.repository;

import com.portly.domain.entity.HabilidadTecnica;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface HabilidadTecnicaRepository extends JpaRepository<HabilidadTecnica, Integer> {

    List<HabilidadTecnica> findByUsuario_IdUsuario(UUID idUsuario);

    boolean existsByUsuario_IdUsuarioAndNombreIgnoreCase(UUID idUsuario, String nombre);

    boolean existsByUsuario_IdUsuarioAndNombreIgnoreCaseAndIdHabilidadNot(
            UUID idUsuario, String nombre, Integer idHabilidad);
}
