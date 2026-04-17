package com.portly.domain.repository;

import com.portly.domain.entity.EvidenciaProyecto;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface EvidenciaProyectoRepository extends JpaRepository<EvidenciaProyecto, Integer> {

    List<EvidenciaProyecto> findByUsuario_IdUsuarioOrderByFechaSubidaDesc(UUID idUsuario);
}
