package com.portly.service;

import com.portly.domain.entity.*;
import com.portly.domain.repository.*;
import com.portly.dto.UsuarioProfileResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ProfileService {

    private final UsuarioRepository          usuarioRepository;
    private final PerfilUsuarioRepository    perfilRepository;
    private final ProveedorOauthRepository   proveedorRepository;
    private final EnlaceProfesionalRepository enlaceRepository;
    private final ExperienciaLaboralRepository experienciaRepository;

    @Transactional(readOnly = true)
    public UsuarioProfileResponse getProfile(UUID usuarioId) {
        Usuario usuario = usuarioRepository.findById(usuarioId)
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));

        PerfilUsuario perfil = perfilRepository.findByUsuario_UsuarioId(usuarioId)
                .orElse(null);

        List<ProveedorOauth> proveedores = proveedorRepository.findByUsuario_UsuarioId(usuarioId);
        List<EnlaceProfesional> enlaces  = enlaceRepository.findByUsuario_UsuarioId(usuarioId)
                .stream().filter(EnlaceProfesional::getVisible).collect(Collectors.toList());
        List<ExperienciaLaboral> exps    = experienciaRepository.findByUsuario_UsuarioId(usuarioId);

        return UsuarioProfileResponse.builder()
                .usuarioId(usuario.getUsuarioId())
                .email(usuario.getEmail())
                .rol(usuario.getRol())
                .estado(usuario.getEstado())
                .emailVerificado(usuario.getEmailVerificado())
                .fechaRegistro(usuario.getFechaRegistro())
                .ultimoAcceso(usuario.getUltimoAcceso())
                // Perfil
                .nombres(perfil != null ? perfil.getNombres() : null)
                .apellidos(perfil != null ? perfil.getApellidos() : null)
                .titularProfesional(perfil != null ? perfil.getTitularProfesional() : null)
                .sobreMi(perfil != null ? perfil.getSobreMi() : null)
                .fotoUrl(perfil != null ? perfil.getFotoUrl() : null)
                .pais(perfil != null ? perfil.getPais() : null)
                .ciudad(perfil != null ? perfil.getCiudad() : null)
                // Proveedores
                .proveedores(proveedores.stream().map(p ->
                        UsuarioProfileResponse.ProveedorDto.builder()
                                .proveedor(p.getProveedor())
                                .usernameExterno(p.getUsernameExterno())
                                .ultimaSync(p.getUltimaSync())
                                .metadatos(p.getMetadatos())
                                .build()
                ).collect(Collectors.toList()))
                // Redes soc
                .enlaces(enlaces.stream().map(e ->
                        UsuarioProfileResponse.EnlaceDto.builder()
                                .plataforma(e.getPlataforma())
                                .urlPerfil(e.getUrlPerfil())
                                .visible(e.getVisible())
                                .build()
                ).collect(Collectors.toList()))
                // Experiencia laboral
                .experiencias(exps.stream().map(ex ->
                        UsuarioProfileResponse.ExperienciaDto.builder()
                                .empresa(ex.getEmpresa())
                                .cargo(ex.getCargo())
                                .modalidad(ex.getModalidad())
                                .fechaIni(ex.getFechaIni())
                                .fechaFin(ex.getFechaFin())
                                .descripcion(ex.getDescripcion())
                                .esEmpleoActual(ex.getEsEmpleoActual())
                                .build()
                ).collect(Collectors.toList()))
                .build();
    }
}
