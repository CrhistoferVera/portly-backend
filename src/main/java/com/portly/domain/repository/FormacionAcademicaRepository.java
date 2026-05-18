package com.portly.domain.repository;

import com.portly.domain.entity.FormacionAcademica;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface FormacionAcademicaRepository extends JpaRepository<FormacionAcademica, Long> {

    List<FormacionAcademica> findByUsuario_IdUsuario(UUID idUsuario);
}
