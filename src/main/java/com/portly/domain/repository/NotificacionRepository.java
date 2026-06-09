package com.portly.domain.repository;

import com.portly.domain.entity.Notificacion;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface NotificacionRepository extends JpaRepository<Notificacion, UUID> {
    List<Notificacion> findByUsuario_IdUsuarioOrderByFechaCreacionDesc(UUID idUsuario);
    long countByUsuario_IdUsuarioAndLeidoFalse(UUID idUsuario);
    List<Notificacion> findByUsuario_IdUsuarioAndLeidoFalse(UUID idUsuario);
}
