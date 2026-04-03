package com.portly.domain.repository;

import com.portly.domain.entity.EnlaceProfesional;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface EnlaceProfesionalRepository extends JpaRepository<EnlaceProfesional, Integer> {

    List<EnlaceProfesional> findByUsuario_IdUsuario(UUID idUsuario);
}
