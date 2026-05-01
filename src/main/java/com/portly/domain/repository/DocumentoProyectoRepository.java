package com.portly.domain.repository;

import com.portly.domain.entity.DocumentoProyecto;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface DocumentoProyectoRepository extends JpaRepository<DocumentoProyecto, Integer> {
    List<DocumentoProyecto> findByUsuario_IdUsuarioOrderByFechaSubidaDesc(UUID idUsuario);
}
