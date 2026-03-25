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

    private final UsuarioRepository usuarioRepository;
    private final ProveedorOauthRepository proveedorOauthRepository;
    private final PerfilUsuarioRepository perfilUsuarioRepository;
    private final EnlaceProfesionalRepository enlaceProfesionalRepository;

    @Transactional
    public Usuario findOrCreate(OAuthUserInfo info) {
        ProveedorOauth proveedor = proveedorOauthRepository
                .findByProveedorAndProveedorUserId(info.getProveedor(), info.getProveedorUserId())
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
                        .emailVerificado(true)
                        .fechaRegistro(LocalDateTime.now())
                        .build();
                usuario = usuarioRepository.save(usuario);

   
                PerfilUsuario perfil = PerfilUsuario.builder()
                        .usuario(usuario)
                        .nombres(info.getNombres())
                        .apellidos(info.getApellidos() != null ? info.getApellidos() : "")
                        .titularProfesional(info.getTitularProfesional())
                        .fotoUrl(info.getFotoUrl())
                        .cvAutomatico(false)
                        .actualizadoEn(LocalDateTime.now())
                        .build();
                perfilUsuarioRepository.save(perfil);

                
                if (info.getUrlPerfil() != null) {
                    EnlaceProfesional enlace = EnlaceProfesional.builder()
                            .usuario(usuario)
                            .plataforma(info.getProveedor())
                            .urlPerfil(info.getUrlPerfil())
                            .visible(true)
                            .build();
                    enlaceProfesionalRepository.save(enlace);
                }
            } else {
                
                perfilUsuarioRepository.findByUsuario_UsuarioId(usuario.getUsuarioId())
                        .ifPresent(perfil -> {
                            if (perfil.getFotoUrl() == null && info.getFotoUrl() != null)
                                perfil.setFotoUrl(info.getFotoUrl());
                            if (perfil.getTitularProfesional() == null && info.getTitularProfesional() != null)
                                perfil.setTitularProfesional(info.getTitularProfesional());
                            perfil.setActualizadoEn(LocalDateTime.now());
                            perfilUsuarioRepository.save(perfil);
                        });
            }

            proveedor = ProveedorOauth.builder()
                    .usuario(usuario)
                    .proveedor(info.getProveedor())
                    .proveedorUserId(info.getProveedorUserId())
                    .usernameExterno(info.getUsernameExterno())
                    .accessToken(info.getAccessToken())
                    .creadoEn(LocalDateTime.now())
                    .build();
        }

        proveedor.setAccessToken(info.getAccessToken());
        if (info.getUsernameExterno() != null) proveedor.setUsernameExterno(info.getUsernameExterno());
        if (info.getRefreshToken() != null) proveedor.setRefreshToken(info.getRefreshToken());
        if (info.getMetadatos() != null) proveedor.setMetadatos(info.getMetadatos());
        proveedor.setUltimaSync(LocalDateTime.now());
        proveedorOauthRepository.save(proveedor);

        usuario.setUltimoAcceso(LocalDateTime.now());
        return usuarioRepository.save(usuario);
    }
}
