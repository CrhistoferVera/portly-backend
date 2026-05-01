package com.portly.service;

import com.portly.domain.entity.Plantilla;
import com.portly.domain.entity.Portafolio;
import com.portly.domain.entity.Usuario;
import com.portly.domain.entity.PerfilUsuario;
import com.portly.domain.repository.PlantillaRepository;
import com.portly.domain.repository.PortafolioRepository;
import com.portly.domain.repository.UsuarioRepository;
import com.portly.dto.PortafolioRequest;
import com.portly.dto.PortafolioResponse;
import com.portly.dto.PortafolioPublicoResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.text.Normalizer;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class PortafolioService {

    private final PortafolioRepository portafolioRepository;
    private final PlantillaRepository plantillaRepository;
    private final UsuarioRepository usuarioRepository;

    @Value("${app.frontend-url}")
    private String frontendUrl;

    /** Obtiene todos los portafolios del usuario autenticado. */
    @Transactional(readOnly = true)
    public List<PortafolioResponse> getAll(UUID idUsuario) {
        return portafolioRepository.findByUsuario_IdUsuarioOrderByFechaCreacionDesc(idUsuario)
                .stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    /** Crea un nuevo portafolio para el usuario autenticado. */
    @Transactional
    public PortafolioResponse create(UUID idUsuario, PortafolioRequest request) {
        Usuario usuario = usuarioRepository.findById(idUsuario)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Usuario no encontrado"));

        Plantilla plantilla = plantillaRepository.findById(request.getTemplateId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Plantilla no encontrada"));

        // Validar visibilidad
        String visibilidad = request.getVisibilidad().toUpperCase();
        if (!visibilidad.equals("PUBLICO") && !visibilidad.equals("PRIVADO")) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "La visibilidad debe ser PUBLICO o PRIVADO");
        }

        // Generar URL pública única
        String urlPublica = generarUrlPublica(usuario);

        Portafolio portafolio = Portafolio.builder()
                .usuario(usuario)
                .plantilla(plantilla)
                .nombre(request.getNombre())
                .visibilidad(visibilidad)
                .urlPublica(urlPublica)
                .fechaCreacion(LocalDateTime.now())
                .build();

        portafolioRepository.save(portafolio);
        log.info("Portafolio creado: idUsuario={}, plantilla={}, url={}",
                idUsuario, request.getTemplateId(), urlPublica);

        return toResponse(portafolio);
    }

    /** Elimina un portafolio verificando que pertenezca al usuario. */
    @Transactional
    public void delete(UUID idUsuario, UUID idPortafolio) {
        Portafolio portafolio = portafolioRepository.findById(idPortafolio)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND,
                        "Portafolio no encontrado"));

        if (!portafolio.getUsuario().getIdUsuario().equals(idUsuario)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN,
                    "No tienes permiso para eliminar este portafolio");
        }

        portafolioRepository.delete(portafolio);
        log.info("Portafolio eliminado: idPortafolio={}, idUsuario={}", idPortafolio, idUsuario);
    }

    /** Obtiene el portafolio público con toda su data */
    @Transactional(readOnly = true)
    public PortafolioPublicoResponse getPublico(String identifier, org.springframework.security.core.Authentication authentication) {
        Portafolio portafolio = null;
        try {
            UUID idPortafolio = UUID.fromString(identifier);
            portafolio = portafolioRepository.findById(idPortafolio).orElse(null);
        } catch (IllegalArgumentException e) {
            // No es un UUID válido, intentar buscar por slug
        }

        if (portafolio == null) {
            portafolio = portafolioRepository.findByUrlPublicaEndingWith("/" + identifier)
                    .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Portafolio no encontrado"));
        }

        if (!portafolio.getVisibilidad().equalsIgnoreCase("PUBLICO")) {
            boolean isOwner = false;
            if (authentication != null && authentication.getPrincipal() instanceof UUID) {
                UUID authUserId = (UUID) authentication.getPrincipal();
                isOwner = portafolio.getUsuario().getIdUsuario().equals(authUserId);
            }
            if (!isOwner) {
                throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Este portafolio es privado");
            }
        }

        Usuario u = portafolio.getUsuario();
        PerfilUsuario perfil = u.getPerfil();

        return PortafolioPublicoResponse.builder()
                .id(portafolio.getIdPortafolio().toString())
                .nombre(portafolio.getNombre())
                .visibilidad(portafolio.getVisibilidad())
                .templateNombre(portafolio.getPlantilla().getNombre())
                .templateSchema(portafolio.getPlantilla().getEsquemaConfiguracion())
                .usuario(PortafolioPublicoResponse.UsuarioPublico.builder()
                        .nombre(perfil != null ? perfil.getNombre() : "")
                        .apellido(perfil != null ? perfil.getApellido() : "")
                        .profesion(perfil != null ? perfil.getTitularProfesional() : "")
                        .descripcion(perfil != null ? perfil.getAcercaDeMi() : "")
                        .avatarUrl(perfil != null ? perfil.getEnlaceFoto() : "")
                        .email(u.getEmail())
                        .build())
                .skills(u.getHabilidades().stream().map(h -> PortafolioPublicoResponse.SkillPublica.builder()
                        .id(h.getIdHabilidad().toString())
                        .name(h.getNombre())
                        .level(h.getNivel())
                        .build()).collect(Collectors.toList()))
                .softSkills(u.getHabilidadesBlandas() != null ? u.getHabilidadesBlandas().stream().map(h -> PortafolioPublicoResponse.SoftSkillPublica.builder()
                        .id(h.getId().longValue())
                        .nombreHabilidad(h.getNombreHabilidad())
                        .build()).collect(Collectors.toList()) : List.of())
                .experiencias(u.getExperiencias().stream().map(e -> PortafolioPublicoResponse.ExperienciaPublica.builder()
                        .id(e.getIdExperienciaLaboral() != null ? e.getIdExperienciaLaboral().longValue() : null)
                        .nombreEmpresa(e.getEmpresa())
                        .cargo(e.getCargo())
                        .fechaInicio(e.getFechaInicio() != null ? e.getFechaInicio().toString() : "")
                        .fechaFin(e.getFechaFin() != null ? e.getFechaFin().toString() : "")
                        .actualmenteTrabajando(e.getEsEmpleoActual() != null && e.getEsEmpleoActual())
                        .descripcion(e.getDescripcion())
                        .build()).collect(Collectors.toList()))
                .proyectos(u.getProyectos().stream().map(p -> PortafolioPublicoResponse.ProyectoPublico.builder()
                        .id(p.getIdProyecto())
                        .nombre(p.getTitulo())
                        .descripcionCorta(p.getResumen())
                        .descripcionDetallada(p.getDescripcionRepositorio())
                        .tecnologias(p.getTecnologias().stream().map(t -> t.getNombre()).collect(Collectors.toList()))
                        .urlDemo(p.getEnlaceRepositorio())
                        .iconoUrl(p.getEnlaceIcono())
                        .build()).collect(Collectors.toList()))
                .formaciones(u.getFormaciones() != null ? u.getFormaciones().stream().map(f -> PortafolioPublicoResponse.FormacionPublica.builder()
                        .idFormacionAcademica(f.getIdFormacionAcademica())
                        .institucion(f.getInstitucion())
                        .carrera(f.getCarrera())
                        .fechaInicio(f.getFechaInicio() != null ? f.getFechaInicio().toString() : "")
                        .fechaFinalizacion(f.getFechaFinalizacion() != null ? f.getFechaFinalizacion().toString() : "")
                        .actualmenteEstudiando(f.getActualmenteEstudiando())
                        .nivel(f.getNivel())
                        .build()).collect(Collectors.toList()) : List.of())
                .build();
    }

    // ─── Helpers ──────────────────────────────────────────────────────────────

    /**
     * Genera una URL pública única para el portafolio.
     * Formato: {frontendUrl}/p/{nombre-apellido}-{hash-corto}
     */
    private String generarUrlPublica(Usuario usuario) {
        String slug = generarSlug(usuario);
        String urlBase = frontendUrl + "/p/" + slug;

        // Si ya existe, agregar un sufijo adicional
        if (portafolioRepository.existsByUrlPublica(urlBase)) {
            String sufijo = UUID.randomUUID().toString().substring(0, 4);
            urlBase = urlBase + "-" + sufijo;
        }

        return urlBase;
    }

    /**
     * Genera un slug amigable a partir del nombre del usuario.
     * Ejemplo: "Víctor Terrazas" → "victor-terrazas-a3f2"
     */
    private String generarSlug(Usuario usuario) {
        String nombreCompleto = "";

        if (usuario.getPerfil() != null) {
            PerfilUsuario perfil = usuario.getPerfil();
            nombreCompleto = (perfil.getNombre() + " " + perfil.getApellido()).trim();
        }

        // Si no tiene perfil, usar parte del email
        if (nombreCompleto.isBlank()) {
            nombreCompleto = usuario.getEmail().split("@")[0];
        }

        // Normalizar: quitar acentos, minúsculas, reemplazar espacios con guiones
        String normalizado = Normalizer.normalize(nombreCompleto, Normalizer.Form.NFD)
                .replaceAll("[\\p{InCombiningDiacriticalMarks}]", "")
                .toLowerCase()
                .replaceAll("[^a-z0-9\\s-]", "")
                .trim()
                .replaceAll("\\s+", "-");

        // Agregar hash corto del UUID para unicidad
        String hashCorto = UUID.randomUUID().toString().substring(0, 4);
        return normalizado + "-" + hashCorto;
    }

    private PortafolioResponse toResponse(Portafolio p) {
        return PortafolioResponse.builder()
                .id(p.getIdPortafolio().toString())
                .nombre(p.getNombre())
                .visibilidad(p.getVisibilidad())
                .templateId(p.getPlantilla().getIdPlantilla())
                .publicUrl(p.getUrlPublica())
                .previewImageUrl(p.getImagenVistaPrevia())
                .createdAt(p.getFechaCreacion().toString())
                .build();
    }
}
