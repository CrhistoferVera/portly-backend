package com.portly.service;

import com.portly.domain.entity.*;
import com.portly.domain.repository.*;
import com.portly.dto.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.function.Function;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class ProfileService {

    private final UsuarioRepository           usuarioRepository;
    private final PerfilUsuarioRepository     perfilRepository;
    private final ProveedorOauthRepository    proveedorRepository;
    private final EnlaceProfesionalRepository enlaceRepository;
    private final ExperienciaLaboralRepository experienciaRepository;
    private final CloudinaryService           cloudinaryService;
    private final RedesSocialesRepository     redesSocialesRepository;

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
        log.info("Perfil actualizado: idUsuario={}", idUsuario);

        List<ProveedorOauth>     proveedores = proveedorRepository.findByUsuario_IdUsuario(idUsuario);
        List<EnlaceProfesional>  enlaces     = enlaceRepository.findByUsuario_IdUsuario(idUsuario)
                .stream().filter(EnlaceProfesional::getEsVisible).collect(Collectors.toList());
        List<ExperienciaLaboral> exps        = experienciaRepository.findByUsuario_IdUsuario(idUsuario);

        return buildResponse(usuario, perfil, proveedores, enlaces, exps);
    }

    // POST /api/redes-sociales — Actualizar redes sociales
    @Transactional
    public void actualizarRedesSociales(UUID idUsuario, RedesSocialesRequest request) {
        PerfilUsuario perfil = perfilRepository.findByUsuario_IdUsuario(idUsuario)
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Perfil no encontrado"));

        actualizarOEliminarRedSocial(perfil, "instagram", request.getInstagram());
        actualizarOEliminarRedSocial(perfil, "facebook", request.getFacebook());
        actualizarOEliminarRedSocial(perfil, "youtube", request.getYoutube());
    }

    private void actualizarOEliminarRedSocial(PerfilUsuario perfil, String nombre, String enlace) {
        Optional<RedesSociales> redSocialOpt = redesSocialesRepository.findByPerfilUsuario_IdPerfilUsuarioAndNombre(perfil.getIdPerfilUsuario(), nombre);
        
        if (enlace == null || enlace.trim().isEmpty()) {
            if (redSocialOpt.isPresent()) {
                redesSocialesRepository.delete(redSocialOpt.get());
                log.info("Red social eliminada: perfilId={}, nombre={}", perfil.getIdPerfilUsuario(), nombre);
            }
        } else {
            if (redSocialOpt.isPresent()) {
                RedesSociales redSocial = redSocialOpt.get();
                redSocial.setEnlace(enlace);
                redesSocialesRepository.save(redSocial);
            } else {
                RedesSociales redSocial = RedesSociales.builder()
                        .perfilUsuario(perfil)
                        .nombre(nombre)
                        .enlace(enlace)
                        .build();
                redesSocialesRepository.save(redSocial);
            }
        }
    }

    // POST /api/redes-sociales/user — Obtener redes sociales por email
    @Transactional(readOnly = true)
    public RedesSocialesRequest obtenerRedesSocialesPorEmail(String email) {
        List<RedesSociales> redes = redesSocialesRepository.findByPerfilUsuario_Usuario_Email(email);
        RedesSocialesRequest request = new RedesSocialesRequest();
        request.setGmail(email);
        if (redes == null || redes.isEmpty()) {
            return request;
        }

        for (RedesSociales red : redes) {
            String nombre = red.getNombre().toLowerCase();
            switch (nombre) {
                case "instagram":
                    request.setInstagram(red.getEnlace());
                    break;
                case "facebook":
                    request.setFacebook(red.getEnlace());
                    break;
                case "youtube":
                    request.setYoutube(red.getEnlace());
                    break;
            }
        }
        return request;
    }

    @Transactional
    public UsuarioProfileResponse subirAvatar(UUID idUsuario, MultipartFile file) {
        PerfilUsuario perfil = perfilRepository.findByUsuario_IdUsuario(idUsuario)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Perfil no encontrado"));

        try {
            String url = cloudinaryService.uploadImage(file, "portly/avatars");
            perfil.setEnlaceFoto(url);
            perfil.setFechaActualizacion(LocalDateTime.now());
            perfilRepository.save(perfil);
        } catch (IOException e) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Error al subir imagen");
        }

        Usuario usuario = usuarioRepository.findById(idUsuario)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Usuario no encontrado"));
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
        log.info("Experiencia agregada: idUsuario={}", idUsuario);
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
        log.info("Experiencia actualizada: idExp={}, idUsuario={}", idExp, idUsuario);
        return toExperienciaDto(exp);
    }

    // DELETE /api/profile/experiencia/{id} — Eliminar experiencia laboral
    @Transactional
    public void eliminarExperiencia(UUID idUsuario, Integer idExp) {
        ExperienciaLaboral exp = experienciaRepository.findById(idExp)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Experiencia no encontrada"));

        verificarPropietario(exp.getUsuario().getIdUsuario(), idUsuario, "eliminar esta experiencia");

        experienciaRepository.delete(exp);
        log.info("Experiencia eliminada: idExp={}, idUsuario={}", idExp, idUsuario);
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
        log.info("Enlace agregado: idUsuario={}, plataforma={}", idUsuario, request.getPlataformaProfesional());
        return toEnlaceDto(enlace);
    }

    // DELETE /api/profile/enlace/{id} — Eliminar enlace profesional
    @Transactional
    public void eliminarEnlace(UUID idUsuario, Integer idEnlace) {
        EnlaceProfesional enlace = enlaceRepository.findById(idEnlace)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Enlace no encontrado"));

        verificarPropietario(enlace.getUsuario().getIdUsuario(), idUsuario, "eliminar este enlace");

        enlaceRepository.delete(enlace);
        log.info("Enlace eliminado: idEnlace={}, idUsuario={}", idEnlace, idUsuario);
    }

    // Extrae un campo de perfil de forma segura cuando perfil puede ser null
    private <T> T fromPerfil(PerfilUsuario perfil, Function<PerfilUsuario, T> getter) {
        return perfil != null ? getter.apply(perfil) : null;
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
                .nombre(fromPerfil(perfil, PerfilUsuario::getNombre))
                .apellido(fromPerfil(perfil, PerfilUsuario::getApellido))
                .titularProfesional(fromPerfil(perfil, PerfilUsuario::getTitularProfesional))
                .acercaDeMi(fromPerfil(perfil, PerfilUsuario::getAcercaDeMi))
                .enlaceFoto(fromPerfil(perfil, PerfilUsuario::getEnlaceFoto))
                .pais(fromPerfil(perfil, PerfilUsuario::getPais))
                .ciudad(fromPerfil(perfil, PerfilUsuario::getCiudad))
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
