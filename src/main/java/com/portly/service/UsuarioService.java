package com.portly.service;

import com.portly.domain.entity.*;
import com.portly.domain.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

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
            usuario = usuarioRepository.findByEmail(info.getEmail()).orElse(null);

            if (usuario == null) {
                usuario = Usuario.builder()
                        .email(info.getEmail())
                        .rol("usuario")
                        .estado("activo")
                        .correoVerificado(true)
                        .fechaCreacion(LocalDateTime.now())
                        .build();
                usuario = usuarioRepository.save(usuario);

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
            } else {
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

            proveedor = ProveedorOauth.builder()
                    .usuario(usuario)
                    .nombreProveedor(info.getProveedor())
                    .idUsuarioProveedor(info.getProveedorUserId())
                    .nombreUsuarioExterno(info.getUsernameExterno())
                    .claveAccesoProveedor(info.getAccessToken())
                    .fechaCreacion(LocalDateTime.now())
                    .build();
        }

        proveedor.setClaveAccesoProveedor(info.getAccessToken());
        if (info.getUsernameExterno() != null) proveedor.setNombreUsuarioExterno(info.getUsernameExterno());
        if (info.getRefreshToken() != null)    proveedor.setClaveActualizacion(info.getRefreshToken());
        if (info.getMetadatos() != null)       proveedor.setMetadatos(info.getMetadatos());
        proveedor.setFechaUltimaSincronizacion(LocalDateTime.now());
        proveedorRepository.save(proveedor);

        usuario.setFechaUltimoAcceso(LocalDateTime.now());
        return usuarioRepository.save(usuario);
    }
}
