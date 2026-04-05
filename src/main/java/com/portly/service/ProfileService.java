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

    // GET /api/profile — Obtener perfil completo del usuario autenticado
    @Transactional(readOnly = true)
    public UsuarioProfileResponse getProfile(UUID idUsuario) {
        Usuario usuario = usuarioRepository.findById(idUsuario)
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));

        PerfilUsuario perfil = perfilRepository.findByUsuario_IdUsuario(idUsuario).orElse(null);

        List<ProveedorOauth>     proveedores = proveedorRepository.findByUsuario_IdUsuario(idUsuario);
        List<EnlaceProfesional>  enlaces     = enlaceRepository.findByUsuario_IdUsuario(idUsuario)
                .stream().filter(EnlaceProfesional::getEsVisible).collect(Collectors.toList());
        List<ExperienciaLaboral> exps        = experienciaRepository.findByUsuario_IdUsuario(idUsuario);

        return buildResponse(usuario, perfil, proveedores, enlaces, exps);
    }

    // PUT /api/profile — Actualizar datos del perfil del usuario autenticado
    @Transactional
    public UsuarioProfileResponse actualizarPerfil(UUID idUsuario, ActualizarPerfilRequest request) {
        Usuario usuario = usuarioRepository.findById(idUsuario)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Usuario no encontrado"));

        PerfilUsuario perfil = perfilRepository.findByUsuario_IdUsuario(idUsuario)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Perfil no encontrado"));

        perfil.setNombre(request.getNombre());
        perfil.setApellido(request.getApellido());
        perfil.setTitularProfesional(request.getTitularProfesional());
        perfil.setAcercaDeMi(request.getAcercaDeMi());
        
        if (request.getEnlaceFoto() != null) {
            perfil.setEnlaceFoto(request.getEnlaceFoto());
        }
        if (request.getPais() != null) {
            perfil.setPais(request.getPais());
        }
        if (request.getCiudad() != null) {
            perfil.setCiudad(request.getCiudad());
        }
        perfil.setFechaActualizacion(LocalDateTime.now());

        perfilRepository.save(perfil);

        List<ProveedorOauth>     proveedores = proveedorRepository.findByUsuario_IdUsuario(idUsuario);
        List<EnlaceProfesional>  enlaces     = enlaceRepository.findByUsuario_IdUsuario(idUsuario)
                .stream().filter(EnlaceProfesional::getEsVisible).collect(Collectors.toList());
        List<ExperienciaLaboral> exps        = experienciaRepository.findByUsuario_IdUsuario(idUsuario);

        return buildResponse(usuario, perfil, proveedores, enlaces, exps);
    }

    // POST /api/profile/experiencia — Agregar experiencia laboral
    @Transactional
    public UsuarioProfileResponse.ExperienciaDto agregarExperiencia(UUID idUsuario, ExperienciaRequest request) {
        Usuario usuario = usuarioRepository.findById(idUsuario)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Usuario no encontrado"));

        ExperienciaLaboral exp = ExperienciaLaboral.builder()
                .usuario(usuario)
                .empresa(request.getEmpresa())
                .cargo(request.getCargo())
                .modalidadTrabajo(request.getModalidadTrabajo())
                .fechaInicio(request.getFechaInicio())
                .fechaFin(request.getFechaFin())
                .descripcion(request.getDescripcion())
                .esEmpleoActual(request.getEsEmpleoActual())
                .build();

        experienciaRepository.save(exp);
        return toExperienciaDto(exp);
    }

    // PUT /api/profile/experiencia/{id} — Editar experiencia laboral
    @Transactional
    public UsuarioProfileResponse.ExperienciaDto actualizarExperiencia(UUID idUsuario, Integer idExp, ExperienciaRequest request) {
        ExperienciaLaboral exp = experienciaRepository.findById(idExp)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Experiencia no encontrada"));

        verificarPropietario(exp.getUsuario().getIdUsuario(), idUsuario, "editar esta experiencia");

        exp.setEmpresa(request.getEmpresa());
        exp.setCargo(request.getCargo());
        exp.setModalidadTrabajo(request.getModalidadTrabajo());
        exp.setFechaInicio(request.getFechaInicio());
        exp.setFechaFin(request.getFechaFin());
        exp.setDescripcion(request.getDescripcion());
        exp.setEsEmpleoActual(request.getEsEmpleoActual());

        experienciaRepository.save(exp);
        return toExperienciaDto(exp);
    }

    // DELETE /api/profile/experiencia/{id} — Eliminar experiencia laboral
    @Transactional
    public void eliminarExperiencia(UUID idUsuario, Integer idExp) {
        ExperienciaLaboral exp = experienciaRepository.findById(idExp)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Experiencia no encontrada"));

        verificarPropietario(exp.getUsuario().getIdUsuario(), idUsuario, "eliminar esta experiencia");

        experienciaRepository.delete(exp);
    }

    // POST /api/profile/enlace — Agregar enlace profesional
    @Transactional
    public UsuarioProfileResponse.EnlaceDto agregarEnlace(UUID idUsuario, EnlaceRequest request) {
        Usuario usuario = usuarioRepository.findById(idUsuario)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Usuario no encontrado"));

        EnlaceProfesional enlace = EnlaceProfesional.builder()
                .usuario(usuario)
                .plataformaProfesional(request.getPlataformaProfesional())
                .direccionEnlace(request.getDireccionEnlace())
                .esVisible(request.getEsVisible() != null ? request.getEsVisible() : true)
                .build();

        enlaceRepository.save(enlace);
        return toEnlaceDto(enlace);
    }

    // DELETE /api/profile/enlace/{id} — Eliminar enlace profesional
    @Transactional
    public void eliminarEnlace(UUID idUsuario, Integer idEnlace) {
        EnlaceProfesional enlace = enlaceRepository.findById(idEnlace)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Enlace no encontrado"));

        verificarPropietario(enlace.getUsuario().getIdUsuario(), idUsuario, "eliminar este enlace");

        enlaceRepository.delete(enlace);
    }

    // Verifica que el recurso pertenezca al usuario autenticado
    private void verificarPropietario(UUID propietario, UUID solicitante, String accion) {
        if (!propietario.equals(solicitante)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "No tienes permiso para " + accion);
        }
    }

    // Metodos privados de mapeo
    private UsuarioProfileResponse buildResponse(Usuario usuario, PerfilUsuario perfil,
            List<ProveedorOauth> proveedores, List<EnlaceProfesional> enlaces, List<ExperienciaLaboral> exps) {
        return UsuarioProfileResponse.builder()
                .idUsuario(usuario.getIdUsuario())
                .email(usuario.getEmail())
                .rol(usuario.getRol())
                .estado(usuario.getEstado())
                .correoVerificado(usuario.getCorreoVerificado())
                .fechaCreacion(usuario.getFechaCreacion())
                .fechaUltimoAcceso(usuario.getFechaUltimoAcceso())
                .nombre(perfil != null ? perfil.getNombre() : null)
                .apellido(perfil != null ? perfil.getApellido() : null)
                .titularProfesional(perfil != null ? perfil.getTitularProfesional() : null)
                .acercaDeMi(perfil != null ? perfil.getAcercaDeMi() : null)
                .enlaceFoto(perfil != null ? perfil.getEnlaceFoto() : null)
                .pais(perfil != null ? perfil.getPais() : null)
                .ciudad(perfil != null ? perfil.getCiudad() : null)
                .proveedores(proveedores.stream().map(p ->
                        UsuarioProfileResponse.ProveedorDto.builder()
                                .nombreProveedor(p.getNombreProveedor())
                                .nombreUsuarioExterno(p.getNombreUsuarioExterno())
                                .fechaUltimaSincronizacion(p.getFechaUltimaSincronizacion())
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
                .modalidadTrabajo(ex.getModalidadTrabajo())
                .fechaInicio(ex.getFechaInicio())
                .fechaFin(ex.getFechaFin())
                .descripcion(ex.getDescripcion())
                .esEmpleoActual(ex.getEsEmpleoActual())
                .build();
    }

    private UsuarioProfileResponse.EnlaceDto toEnlaceDto(EnlaceProfesional e) {
        return UsuarioProfileResponse.EnlaceDto.builder()
                .plataformaProfesional(e.getPlataformaProfesional())
                .direccionEnlace(e.getDireccionEnlace())
                .esVisible(e.getEsVisible())
                .build();
    }
}
