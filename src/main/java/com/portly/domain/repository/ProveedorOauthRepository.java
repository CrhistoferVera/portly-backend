package com.portly.domain.repository;

import com.portly.domain.entity.ProveedorOauth;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface ProveedorOauthRepository extends JpaRepository<ProveedorOauth, Integer> {

    List<ProveedorOauth> findByUsuario_UsuarioId(UUID usuarioId);

    Optional<ProveedorOauth> findByUsuario_UsuarioIdAndProveedor(UUID usuarioId, String proveedor);

    Optional<ProveedorOauth> findByProveedorAndProveedorUserId(String proveedor, String proveedorUserId);
}
