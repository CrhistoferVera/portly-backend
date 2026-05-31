package com.portly.domain.repository;

import com.portly.domain.entity.Apelacion;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface ApelacionRepository extends JpaRepository<Apelacion, Long> {
    List<Apelacion> findAllByOrderByFechaApelacionDesc();

    boolean existsByUsuario_IdUsuarioAndEstadoApelacionIgnoreCase(UUID userId, String estadoApelacion);

    java.util.Optional<Apelacion> findFirstByUsuario_IdUsuarioAndEstadoApelacionIgnoreCaseOrderByIdDesc(UUID userId, String estadoApelacion);
}
