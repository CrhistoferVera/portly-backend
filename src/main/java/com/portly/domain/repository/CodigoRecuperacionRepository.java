package com.portly.domain.repository;

import com.portly.domain.entity.CodigoRecuperacion;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface CodigoRecuperacionRepository extends JpaRepository<CodigoRecuperacion, Integer> {

    Optional<CodigoRecuperacion> findByCodigoAndUsuario_IdUsuario(String codigo, UUID idUsuario);

    void deleteByUsuario_IdUsuario(UUID idUsuario);
}
