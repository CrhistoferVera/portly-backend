package com.portly.domain.repository;

import com.portly.domain.entity.Proyecto;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface ProyectoRepository extends JpaRepository<Proyecto, Long> {

    List<Proyecto> findByUsuario_IdUsuarioOrderByFechaCreacionDesc(UUID idUsuario);
}
