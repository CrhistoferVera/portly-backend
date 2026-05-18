package com.portly.domain.repository;

import com.portly.domain.entity.ProveedorOauth;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface ProveedorOauthRepository extends JpaRepository<ProveedorOauth, Integer> {

    List<ProveedorOauth> findByUsuario_IdUsuario(UUID idUsuario);

    Optional<ProveedorOauth> findByUsuario_IdUsuarioAndNombreProveedor(UUID idUsuario, String nombreProveedor);

    Optional<ProveedorOauth> findByNombreProveedorAndIdUsuarioProveedor(String nombreProveedor, String idUsuarioProveedor);
}
