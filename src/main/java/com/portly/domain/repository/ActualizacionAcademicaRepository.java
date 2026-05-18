package com.portly.domain.repository;

import com.portly.domain.entity.ActualizacionAcademica;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface ActualizacionAcademicaRepository extends JpaRepository<ActualizacionAcademica, Long> {

    List<ActualizacionAcademica> findByUsuario_IdUsuario(UUID idUsuario);
}
