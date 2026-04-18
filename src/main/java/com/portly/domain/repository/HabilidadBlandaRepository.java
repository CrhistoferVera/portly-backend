package com.portly.domain.repository;

import com.portly.domain.entity.HabilidadBlanda;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface HabilidadBlandaRepository extends JpaRepository<HabilidadBlanda, Integer> {

    List<HabilidadBlanda> findByUsuario_IdUsuario(UUID idUsuario);

    boolean existsByUsuario_IdUsuarioAndNombreHabilidadIgnoreCase(UUID idUsuario, String nombreHabilidad);
}
