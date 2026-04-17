package com.portly.domain.repository;

import com.portly.domain.entity.CodigoRegistro;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface CodigoRegistroRepository extends JpaRepository<CodigoRegistro, Integer> {

    Optional<CodigoRegistro> findByEmail(String email);

    void deleteByEmail(String email);
}
