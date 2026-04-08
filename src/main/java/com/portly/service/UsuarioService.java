package com.portly.service;

import com.portly.domain.entity.*;
import com.portly.domain.repository.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Slf4j
@Service
@RequiredArgsConstructor
public class UsuarioService {

    private final UsuarioRepository           usuarioRepository;
    private final ProveedorOauthRepository    proveedorRepository;
    private final PerfilUsuarioRepository     perfilRepository;
    private final EnlaceProfesionalRepository enlaceRepository;

    @Transactional
    public Usuario findOrCreate(OAuthUserInfo info) {
        ProveedorOauth proveedor = proveedorRepository
                .findByNombreProveedorAndIdUsuarioProveedor(info.getProveedor(), info.getProveedorUserId())
                .orElse(null);

        Usuario usuario;
        if (proveedor != null) {
            usuario = proveedor.getUsuario();
        } else {
            usuario  = resolverUsuario(info);
            proveedor = crearProveedor(usuario, info);
        }

        actualizarProveedor(proveedor, info);
        usuario.setFechaUltimoAcceso(LocalDateTime.now());
        log.info("Login OAuth exitoso: proveedor={}, email={}", info.getProveedor(), info.getEmail());
        return usuarioRepository.save(usuario);
    }

    // Busca usuario por email o lo crea si no existe
    private Usuario resolverUsuario(OAuthUserInfo info) {
        return usuarioRepository.findByEmail(info.getEmail())
                .map(existente -> { actualizarPerfilDesdeOAuth(existente, info); return existente; })
                .orElseGet(() -> crearUsuarioDesdeOAuth(info));
    }

    // Crea un usuario nuevo con su perfil y enlace profesional
    private Usuario crearUsuarioDesdeOAuth(OAuthUserInfo info) {
        Usuario usuario = Usuario.builder()
                .email(info.getEmail())
                .rol("usuario")
                .estado("activo")
                .correoVerificado(true)
                .fechaCreacion(LocalDateTime.now())
                .build();
        usuario = usuarioRepository.save(usuario);
        log.info("Nuevo usuario creado via OAuth: proveedor={}, email={}", info.getProveedor(), info.getEmail());

        PerfilUsuario perfil = PerfilUsuario.builder()
                .usuario(usuario)
                .nombre(info.getNombres())
                .apellido(info.getApellidos() != null ? info.getApellidos() : "")
                .titularProfesional(info.getTitularProfesional())
                .enlaceFoto(info.getFotoUrl())
                .fechaActualizacion(LocalDateTime.now())
                .build();
        perfilRepository.save(perfil);

        if (info.getUrlPerfil() != null) {
            EnlaceProfesional enlace = EnlaceProfesional.builder()
                    .usuario(usuario)
                    .plataformaProfesional(info.getProveedor())
                    .direccionEnlace(info.getUrlPerfil())
                    .esVisible(true)
                    .build();
            enlaceRepository.save(enlace);
        }
        return usuario;
    }

    // Actualiza foto y titular de un usuario existente solo si no los tenía
    private void actualizarPerfilDesdeOAuth(Usuario usuario, OAuthUserInfo info) {
        perfilRepository.findByUsuario_IdUsuario(usuario.getIdUsuario())
                .ifPresent(perfil -> {
                    if (perfil.getEnlaceFoto() == null && info.getFotoUrl() != null)
                        perfil.setEnlaceFoto(info.getFotoUrl());
                    if (perfil.getTitularProfesional() == null && info.getTitularProfesional() != null)
                        perfil.setTitularProfesional(info.getTitularProfesional());
                    perfil.setFechaActualizacion(LocalDateTime.now());
                    perfilRepository.save(perfil);
                });
    }

    // Construye un nuevo registro de proveedor OAuth para el usuario
    private ProveedorOauth crearProveedor(Usuario usuario, OAuthUserInfo info) {
        return ProveedorOauth.builder()
                .usuario(usuario)
                .nombreProveedor(info.getProveedor())
                .idUsuarioProveedor(info.getProveedorUserId())
                .nombreUsuarioExterno(info.getUsernameExterno())
                .claveAccesoProveedor(info.getAccessToken())
                .fechaCreacion(LocalDateTime.now())
                .build();
    }

    // Sincroniza tokens y metadatos del proveedor y lo persiste
    private void actualizarProveedor(ProveedorOauth proveedor, OAuthUserInfo info) {
        proveedor.setClaveAccesoProveedor(info.getAccessToken());
        if (info.getUsernameExterno() != null) proveedor.setNombreUsuarioExterno(info.getUsernameExterno());
        if (info.getRefreshToken() != null)    proveedor.setClaveActualizacion(info.getRefreshToken());
        if (info.getMetadatos() != null)       proveedor.setMetadatos(info.getMetadatos());
        proveedor.setFechaUltimaSincronizacion(LocalDateTime.now());
        proveedorRepository.save(proveedor);
    }

    /**
     * Vincula un proveedor OAuth al usuario ACTUAL (identificado por su UUID).
     * A diferencia de findOrCreate, NO busca/crea por email del proveedor.
     */
    @Transactional
    public void linkProviderToUser(java.util.UUID userId, OAuthUserInfo info) {
        Usuario usuario = usuarioRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));

        // Verificar si ya tiene este proveedor vinculado
        ProveedorOauth existing = proveedorRepository
                .findByNombreProveedorAndIdUsuarioProveedor(info.getProveedor(), info.getProveedorUserId())
                .orElse(null);

        if (existing != null) {
            // El proveedor ya está vinculado a alguien
            if (!existing.getUsuario().getIdUsuario().equals(userId)) {
                log.warn("Intento de vincular cuenta OAuth ya en uso: proveedor={}, usuarioId={}", info.getProveedor(), userId);
                throw new RuntimeException("Esta cuenta de " + info.getProveedor() + " ya está vinculada a otro usuario.");
            }
            // Ya vinculado al mismo usuario, actualizar tokens
            existing.setClaveAccesoProveedor(info.getAccessToken());
            if (info.getRefreshToken() != null) existing.setClaveActualizacion(info.getRefreshToken());
            existing.setFechaUltimaSincronizacion(LocalDateTime.now());
            proveedorRepository.save(existing);
            return;
        }

        // Crear nueva vinculación
        ProveedorOauth proveedor = crearProveedor(usuario, info);
        proveedor.setFechaUltimaSincronizacion(LocalDateTime.now());
        if (info.getMetadatos() != null) proveedor.setMetadatos(info.getMetadatos());
        proveedorRepository.save(proveedor);
        log.info("Proveedor OAuth vinculado: proveedor={}, usuarioId={}", info.getProveedor(), userId);
    }
}
