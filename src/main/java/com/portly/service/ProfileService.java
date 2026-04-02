package com.portly.service;

import com.portly.domain.entity.*;
import com.portly.domain.repository.*;
import com.portly.dto.*;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ProfileService {

    private final UsuarioRepository           usuarioRepository;
    private final PerfilUsuarioRepository     perfilRepository;
    private final ProveedorOauthRepository    proveedorRepository;
    private final EnlaceProfesionalRepository enlaceRepository;
    private final ExperienciaLaboralRepository experienciaRepository;

    // ------------------------------------------------------------
    // GET /api/profile — Obtener perfil completo del usuario autenticado
    // ------------------------------------------------------------
    @Transactional(readOnly = true)
    public UsuarioProfileResponse getProfile(UUID usuarioId) {
        Usuario usuario = usuarioRepository.findById(usuarioId)
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));

        PerfilUsuario perfil = perfilRepository.findByUsuario_UsuarioId(usuarioId).orElse(null);

        List<ProveedorOauth>     proveedores = proveedorRepository.findByUsuario_UsuarioId(usuarioId);
        List<EnlaceProfesional>  enlaces     = enlaceRepository.findByUsuario_UsuarioId(usuarioId)
                .stream().filter(EnlaceProfesional::getVisible).collect(Collectors.toList());
        List<ExperienciaLaboral> exps        = experienciaRepository.findByUsuario_UsuarioId(usuarioId);

        return buildResponse(usuario, perfil, proveedores, enlaces, exps);
    }

    // ------------------------------------------------------------
    // PUT /api/profile — Actualizar datos del perfil del usuario autenticado
    // ------------------------------------------------------------
    @Transactional
    public UsuarioProfileResponse actualizarPerfil(UUID usuarioId, ActualizarPerfilRequest request) {
        Usuario usuario = usuarioRepository.findById(usuarioId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Usuario no encontrado"));

        PerfilUsuario perfil = perfilRepository.findByUsuario_UsuarioId(usuarioId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Perfil no encontrado"));

        perfil.setNombres(request.getNombres());
        perfil.setApellidos(request.getApellidos());
        perfil.setTitularProfesional(request.getTitularProfesional());
        perfil.setSobreMi(request.getSobreMi());
        perfil.setFotoUrl(request.getFotoUrl());
        perfil.setPais(request.getPais());
        perfil.setCiudad(request.getCiudad());
        perfil.setActualizadoEn(LocalDateTime.now());

        perfilRepository.save(perfil);

        List<ProveedorOauth>     proveedores = proveedorRepository.findByUsuario_UsuarioId(usuarioId);
        List<EnlaceProfesional>  enlaces     = enlaceRepository.findByUsuario_UsuarioId(usuarioId)
                .stream().filter(EnlaceProfesional::getVisible).collect(Collectors.toList());
        List<ExperienciaLaboral> exps        = experienciaRepository.findByUsuario_UsuarioId(usuarioId);

        return buildResponse(usuario, perfil, proveedores, enlaces, exps);
    }

    // ------------------------------------------------------------
    // POST /api/profile/experiencia — Agregar experiencia laboral
    // ------------------------------------------------------------
    @Transactional
    public UsuarioProfileResponse.ExperienciaDto agregarExperiencia(UUID usuarioId, ExperienciaRequest request) {
        Usuario usuario = usuarioRepository.findById(usuarioId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Usuario no encontrado"));

        ExperienciaLaboral exp = ExperienciaLaboral.builder()
                .usuario(usuario)
                .empresa(request.getEmpresa())
                .cargo(request.getCargo())
                .modalidad(request.getModalidad())
                .fechaIni(request.getFechaIni())
                .fechaFin(request.getFechaFin())
                .descripcion(request.getDescripcion())
                .esEmpleoActual(request.getEsEmpleoActual())
                .build();

        experienciaRepository.save(exp);
        return toExperienciaDto(exp);
    }

    // ------------------------------------------------------------
    // PUT /api/profile/experiencia/{id} — Editar experiencia laboral
    // ------------------------------------------------------------
    @Transactional
    public UsuarioProfileResponse.ExperienciaDto actualizarExperiencia(UUID usuarioId, Integer idExp, ExperienciaRequest request) {
        ExperienciaLaboral exp = experienciaRepository.findById(idExp)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Experiencia no encontrada"));

        // Verificar que la experiencia pertenece al usuario autenticado
        if (!exp.getUsuario().getUsuarioId().equals(usuarioId)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "No tienes permiso para editar esta experiencia");
        }

        exp.setEmpresa(request.getEmpresa());
        exp.setCargo(request.getCargo());
        exp.setModalidad(request.getModalidad());
        exp.setFechaIni(request.getFechaIni());
        exp.setFechaFin(request.getFechaFin());
        exp.setDescripcion(request.getDescripcion());
        exp.setEsEmpleoActual(request.getEsEmpleoActual());

        experienciaRepository.save(exp);
        return toExperienciaDto(exp);
    }

    // ------------------------------------------------------------
    // DELETE /api/profile/experiencia/{id} — Eliminar experiencia laboral
    // ------------------------------------------------------------
    @Transactional
    public void eliminarExperiencia(UUID usuarioId, Integer idExp) {
        ExperienciaLaboral exp = experienciaRepository.findById(idExp)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Experiencia no encontrada"));

        if (!exp.getUsuario().getUsuarioId().equals(usuarioId)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "No tienes permiso para eliminar esta experiencia");
        }

        experienciaRepository.delete(exp);
    }

    // ------------------------------------------------------------
    // POST /api/profile/enlace — Agregar enlace profesional
    // ------------------------------------------------------------
    @Transactional
    public UsuarioProfileResponse.EnlaceDto agregarEnlace(UUID usuarioId, EnlaceRequest request) {
        Usuario usuario = usuarioRepository.findById(usuarioId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Usuario no encontrado"));

        EnlaceProfesional enlace = EnlaceProfesional.builder()
                .usuario(usuario)
                .plataforma(request.getPlataforma())
                .urlPerfil(request.getUrlPerfil())
                .visible(request.getVisible() != null ? request.getVisible() : true)
                .build();

        enlaceRepository.save(enlace);
        return toEnlaceDto(enlace);
    }

    // ------------------------------------------------------------
    // DELETE /api/profile/enlace/{id} — Eliminar enlace profesional
    // ------------------------------------------------------------
    @Transactional
    public void eliminarEnlace(UUID usuarioId, Integer idEnlace) {
        EnlaceProfesional enlace = enlaceRepository.findById(idEnlace)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Enlace no encontrado"));

        if (!enlace.getUsuario().getUsuarioId().equals(usuarioId)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "No tienes permiso para eliminar este enlace");
        }

        enlaceRepository.delete(enlace);
    }

    // ------------------------------------------------------------
    // Metodos privados de mapeo
    // ------------------------------------------------------------
    private UsuarioProfileResponse buildResponse(Usuario usuario, PerfilUsuario perfil,
            List<ProveedorOauth> proveedores, List<EnlaceProfesional> enlaces, List<ExperienciaLaboral> exps) {
        return UsuarioProfileResponse.builder()
                .usuarioId(usuario.getUsuarioId())
                .email(usuario.getEmail())
                .rol(usuario.getRol())
                .estado(usuario.getEstado())
                .emailVerificado(usuario.getEmailVerificado())
                .fechaRegistro(usuario.getFechaRegistro())
                .ultimoAcceso(usuario.getUltimoAcceso())
                .nombres(perfil != null ? perfil.getNombres() : null)
                .apellidos(perfil != null ? perfil.getApellidos() : null)
                .titularProfesional(perfil != null ? perfil.getTitularProfesional() : null)
                .sobreMi(perfil != null ? perfil.getSobreMi() : null)
                .fotoUrl(perfil != null ? perfil.getFotoUrl() : null)
                .pais(perfil != null ? perfil.getPais() : null)
                .ciudad(perfil != null ? perfil.getCiudad() : null)
                .proveedores(proveedores.stream().map(p ->
                        UsuarioProfileResponse.ProveedorDto.builder()
                                .proveedor(p.getProveedor())
                                .usernameExterno(p.getUsernameExterno())
                                .ultimaSync(p.getUltimaSync())
                                .metadatos(p.getMetadatos())
                                .build()
                ).collect(Collectors.toList()))
                .enlaces(enlaces.stream().map(this::toEnlaceDto).collect(Collectors.toList()))
                .experiencias(exps.stream().map(this::toExperienciaDto).collect(Collectors.toList()))
                .build();
    }

    private UsuarioProfileResponse.ExperienciaDto toExperienciaDto(ExperienciaLaboral ex) {
        return UsuarioProfileResponse.ExperienciaDto.builder()
                .empresa(ex.getEmpresa())
                .cargo(ex.getCargo())
                .modalidad(ex.getModalidad())
                .fechaIni(ex.getFechaIni())
                .fechaFin(ex.getFechaFin())
                .descripcion(ex.getDescripcion())
                .esEmpleoActual(ex.getEsEmpleoActual())
                .build();
    }

    private UsuarioProfileResponse.EnlaceDto toEnlaceDto(EnlaceProfesional e) {
        return UsuarioProfileResponse.EnlaceDto.builder()
                .plataforma(e.getPlataforma())
                .urlPerfil(e.getUrlPerfil())
                .visible(e.getVisible())
                .build();
    }
}
