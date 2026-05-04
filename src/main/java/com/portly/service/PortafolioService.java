package com.portly.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.portly.domain.entity.Plantilla;
import com.portly.domain.entity.Portafolio;
import com.portly.domain.entity.Usuario;
import com.portly.domain.entity.PerfilUsuario;
import com.portly.domain.repository.PlantillaRepository;
import com.portly.domain.repository.PortafolioRepository;
import com.portly.domain.repository.RedesSocialesRepository;
import com.portly.domain.repository.UsuarioRepository;
import com.portly.dto.PortafolioRequest;
import com.portly.dto.PortafolioResponse;
import com.portly.dto.PortafolioPublicoResponse;
import com.portly.dto.VisibilidadItemsRequest;
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
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class PortafolioService {

    private final PortafolioRepository portafolioRepository;
    private final PlantillaRepository plantillaRepository;
    private final UsuarioRepository usuarioRepository;
    private final RedesSocialesRepository redesSocialesRepository;
    private final ObjectMapper objectMapper;

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

        String visibilidad = request.getVisibilidad().toUpperCase();
        if (!visibilidad.equals("PUBLICO") && !visibilidad.equals("PRIVADO")) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "La visibilidad debe ser PUBLICO o PRIVADO");
        }

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

    /** Guarda la configuración de visibilidad de items de un portafolio. */
    @Transactional
    public PortafolioResponse updateVisibilidad(UUID idUsuario, UUID idPortafolio, VisibilidadItemsRequest request) {
        Portafolio portafolio = portafolioRepository.findById(idPortafolio)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND,
                        "Portafolio no encontrado"));

        if (!portafolio.getUsuario().getIdUsuario().equals(idUsuario)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN,
                    "No tienes permiso para modificar este portafolio");
        }

        try {
            portafolio.setConfiguracionVisibilidad(objectMapper.writeValueAsString(request));
        } catch (JsonProcessingException e) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR,
                    "Error al procesar la configuración de visibilidad");
        }

        portafolioRepository.save(portafolio);
        return toResponse(portafolio);
    }

    /** Cambia la visibilidad del portafolio a PUBLICO. */
    @Transactional
    public PortafolioResponse publicar(UUID idUsuario, UUID idPortafolio) {
        Portafolio portafolio = portafolioRepository.findById(idPortafolio)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND,
                        "Portafolio no encontrado"));

        if (!portafolio.getUsuario().getIdUsuario().equals(idUsuario)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN,
                    "No tienes permiso para publicar este portafolio");
        }

        portafolio.setVisibilidad("PUBLICO");
        portafolioRepository.save(portafolio);
        log.info("Portafolio publicado: idPortafolio={}, idUsuario={}", idPortafolio, idUsuario);

        return toResponse(portafolio);
    }

    /** Obtiene el portafolio público con toda su data, aplicando filtros de visibilidad. */
    @Transactional(readOnly = true)
    public PortafolioPublicoResponse getPublico(String identifier, org.springframework.security.core.Authentication authentication) {
        Portafolio portafolio = null;
        try {
            UUID idPortafolio = UUID.fromString(identifier);
            portafolio = portafolioRepository.findById(idPortafolio).orElse(null);
        } catch (IllegalArgumentException e) {
            // No es UUID válido, buscar por slug
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

        // Filtros de visibilidad de sección (desde PerfilUsuario)
        boolean showEmail      = perfil == null || !Boolean.FALSE.equals(perfil.getMostrarCorreo());
        boolean showProfession = perfil == null || !Boolean.FALSE.equals(perfil.getMostrarProfesion());
        boolean showBio        = perfil == null || !Boolean.FALSE.equals(perfil.getMostrarBiografia());
        boolean showPhone      = perfil == null || !Boolean.FALSE.equals(perfil.getMostrarTelefono());
        boolean showCountry    = perfil == null || !Boolean.FALSE.equals(perfil.getMostrarPais());
        boolean showLinkedin   = perfil == null || !Boolean.FALSE.equals(perfil.getMostrarLinkedin());
        boolean showGithub     = perfil == null || !Boolean.FALSE.equals(perfil.getMostrarGithub());
        boolean showInstagram  = perfil == null || !Boolean.FALSE.equals(perfil.getMostrarInstagram());
        boolean showFacebook   = perfil == null || !Boolean.FALSE.equals(perfil.getMostrarFacebook());
        boolean showYoutube    = perfil == null || !Boolean.FALSE.equals(perfil.getMostrarYoutube());
        boolean showTechSkills = perfil == null || !Boolean.FALSE.equals(perfil.getMostrarHabilidadesTecnicas());
        boolean showSoftSkills = perfil == null || !Boolean.FALSE.equals(perfil.getMostrarHabilidadesBlandas());
        boolean showExperience = perfil == null || !Boolean.FALSE.equals(perfil.getMostrarTrayectoria());
        boolean showEducation  = perfil == null || !Boolean.FALSE.equals(perfil.getMostrarFormacion());

        // Redes sociales del perfil (Instagram, Facebook, YouTube, LinkedIn, GitHub)
        Map<String, String> redesMap = java.util.Collections.emptyMap();
        if (perfil != null) {
            redesMap = redesSocialesRepository
                    .findAllByPerfilUsuario_IdPerfilUsuario(perfil.getIdPerfilUsuario())
                    .stream()
                    .collect(Collectors.toMap(
                            r -> r.getNombre().toLowerCase(),
                            r -> r.getEnlace(),
                            (a, b) -> a
                    ));
        }
        final Map<String, String> redes = redesMap;

        // Filtros de visibilidad de items individuales (desde configuracionVisibilidad)
        VisibilidadItemsRequest itemVis = parseItemVisibilidad(portafolio.getConfiguracionVisibilidad());

        String telefono = null;
        if (perfil != null && perfil.getTelefono() != null && !perfil.getTelefono().isBlank()) {
            telefono = (perfil.getCodigoTelefono() != null && !perfil.getCodigoTelefono().isBlank())
                    ? perfil.getCodigoTelefono() + " " + perfil.getTelefono()
                    : perfil.getTelefono();
        }

        return PortafolioPublicoResponse.builder()
                .id(portafolio.getIdPortafolio().toString())
                .nombre(portafolio.getNombre())
                .visibilidad(portafolio.getVisibilidad())
                .templateNombre(portafolio.getPlantilla().getNombre())
                .templateSchema(portafolio.getPlantilla().getEsquemaConfiguracion())
                .usuario(PortafolioPublicoResponse.UsuarioPublico.builder()
                        .nombre(perfil != null ? perfil.getNombre() : "")
                        .apellido(perfil != null ? perfil.getApellido() : "")
                        .profesion(showProfession && perfil != null ? perfil.getTitularProfesional() : null)
                        .descripcion(showBio && perfil != null ? perfil.getAcercaDeMi() : null)
                        .avatarUrl(perfil != null ? perfil.getEnlaceFoto() : "")
                        .email(showEmail ? u.getEmail() : null)
                        .telefono(showPhone ? telefono : null)
                        .pais(showCountry && perfil != null ? perfil.getPais() : null)
                        .instagram(showInstagram ? redes.get("instagram") : null)
                        .facebook(showFacebook ? redes.get("facebook") : null)
                        .youtube(showYoutube ? redes.get("youtube") : null)
                        .linkedin(showLinkedin ? redes.get("linkedin") : null)
                        .github(showGithub ? redes.get("github") : null)
                        .build())
                .skills(showTechSkills
                        ? u.getHabilidades().stream()
                                .filter(h -> isItemVisible(itemVis.getTechSkillItems(), h.getIdHabilidad().toString()))
                                .map(h -> PortafolioPublicoResponse.SkillPublica.builder()
                                        .id(h.getIdHabilidad().toString())
                                        .name(h.getNombre())
                                        .level(h.getNivel())
                                        .build())
                                .collect(Collectors.toList())
                        : List.of())
                .softSkills(showSoftSkills && u.getHabilidadesBlandas() != null
                        ? u.getHabilidadesBlandas().stream()
                                .filter(h -> isItemVisible(itemVis.getSoftSkillItems(), String.valueOf(h.getId())))
                                .map(h -> PortafolioPublicoResponse.SoftSkillPublica.builder()
                                        .id(h.getId().longValue())
                                        .nombreHabilidad(h.getNombreHabilidad())
                                        .build())
                                .collect(Collectors.toList())
                        : List.of())
                .experiencias(showExperience
                        ? u.getExperiencias().stream()
                                .filter(e -> isItemVisible(itemVis.getExperienceItems(),
                                        e.getIdExperienciaLaboral() != null ? e.getIdExperienciaLaboral().toString() : ""))
                                .map(e -> PortafolioPublicoResponse.ExperienciaPublica.builder()
                                        .id(e.getIdExperienciaLaboral() != null ? e.getIdExperienciaLaboral().longValue() : null)
                                        .nombreEmpresa(e.getEmpresa())
                                        .cargo(e.getCargo())
                                        .modalidadTrabajo(e.getModalidadTrabajo())
                                        .fechaInicio(e.getFechaInicio() != null ? e.getFechaInicio().toString() : "")
                                        .fechaFin(e.getFechaFin() != null ? e.getFechaFin().toString() : "")
                                        .actualmenteTrabajando(e.getEsEmpleoActual() != null && e.getEsEmpleoActual())
                                        .descripcion(e.getDescripcion())
                                        .funcionesPrincipales(e.getFuncionesPrincipales())
                                        .logros(e.getLogros())
                                        .correoJefe(e.getCorreoJefe())
                                        .numeroJefe(e.getNumeroJefe())
                                        .cargoJefe(e.getCargoJefe())
                                        .build())
                                .collect(Collectors.toList())
                        : List.of())
                .proyectos(!Boolean.FALSE.equals(itemVis.getShowProjects())
                        ? u.getProyectos().stream()
                                .filter(p -> isItemVisible(itemVis.getProjectItems(),
                                        p.getIdProyecto() != null ? p.getIdProyecto().toString() : ""))
                                .map(p -> PortafolioPublicoResponse.ProyectoPublico.builder()
                                        .id(p.getIdProyecto())
                                        .nombre(p.getTitulo())
                                        .descripcionCorta(p.getResumen())
                                        .descripcionDetallada(p.getDescripcionRepositorio())
                                        .tecnologias(p.getTecnologias().stream().map(t -> t.getNombre()).collect(Collectors.toList()))
                                        .urlDemo(p.getEnlaceRepositorio())
                                        .iconoUrl(p.getEnlaceIcono())
                                        .evidencias(p.getEvidencias() != null
                                                ? p.getEvidencias().stream()
                                                        .map(ev -> ev.getEnlaceEvidencia())
                                                        .filter(url -> url != null && !url.isBlank())
                                                        .collect(Collectors.toList())
                                                : List.of())
                                        .enlaces(p.getEnlaces() != null
                                                ? p.getEnlaces().stream()
                                                        .map(en -> PortafolioPublicoResponse.EnlacePublico.builder()
                                                                .titulo(en.getTitulo())
                                                                .url(en.getUrl())
                                                                .build())
                                                        .collect(Collectors.toList())
                                                : List.of())
                                        .documentos(p.getDocumentos() != null
                                                ? p.getDocumentos().stream()
                                                        .map(doc -> PortafolioPublicoResponse.DocumentoPublico.builder()
                                                                .id(doc.getIdDocumentoProyecto())
                                                                .nombre(doc.getNombreOriginal())
                                                                .urlDescarga("/api/public/documentos/" + doc.getIdDocumentoProyecto() + "/descargar")
                                                                .formato(doc.getFormato())
                                                                .pesoBytes(doc.getTamanoBytes())
                                                                .build())
                                                        .collect(Collectors.toList())
                                                : List.of())
                                        .build())
                                .collect(Collectors.toList())
                        : List.of())
                .formaciones(showEducation && u.getFormaciones() != null
                        ? u.getFormaciones().stream()
                                .filter(f -> isItemVisible(itemVis.getEducationItems(),
                                        f.getIdFormacionAcademica() != null ? f.getIdFormacionAcademica().toString() : ""))
                                .map(f -> PortafolioPublicoResponse.FormacionPublica.builder()
                                        .idFormacionAcademica(f.getIdFormacionAcademica())
                                        .institucion(f.getInstitucion())
                                        .carrera(f.getCarrera())
                                        .fechaInicio(f.getFechaInicio() != null ? f.getFechaInicio().toString() : "")
                                        .fechaFinalizacion(f.getFechaFinalizacion() != null ? f.getFechaFinalizacion().toString() : "")
                                        .actualmenteEstudiando(f.getActualmenteEstudiando())
                                        .nivel(f.getNivel())
                                        .descripcion(f.getDescripcion())
                                        .build())
                                .collect(Collectors.toList())
                        : List.of())
                .build();
    }

    // ─── Helpers ──────────────────────────────────────────────────────────────

    /** Devuelve true si el item debe mostrarse (no está explícitamente en false). */
    private boolean isItemVisible(Map<String, Boolean> map, String id) {
        if (map == null || id == null || id.isEmpty()) return true;
        return !Boolean.FALSE.equals(map.get(id));
    }

    /** Parsea el JSON de configuracionVisibilidad o devuelve un objeto vacío. */
    private VisibilidadItemsRequest parseItemVisibilidad(String json) {
        if (json == null || json.isBlank()) return new VisibilidadItemsRequest();
        try {
            return objectMapper.readValue(json, VisibilidadItemsRequest.class);
        } catch (JsonProcessingException e) {
            log.warn("No se pudo parsear configuracionVisibilidad: {}", e.getMessage());
            return new VisibilidadItemsRequest();
        }
    }

    private String generarUrlPublica(Usuario usuario) {
        String slug = generarSlug(usuario);
        String urlBase = frontendUrl + "/p/" + slug;
        if (portafolioRepository.existsByUrlPublica(urlBase)) {
            String sufijo = UUID.randomUUID().toString().substring(0, 4);
            urlBase = urlBase + "-" + sufijo;
        }
        return urlBase;
    }

    private String generarSlug(Usuario usuario) {
        String nombreCompleto = "";
        if (usuario.getPerfil() != null) {
            PerfilUsuario perfil = usuario.getPerfil();
            nombreCompleto = (perfil.getNombre() + " " + perfil.getApellido()).trim();
        }
        if (nombreCompleto.isBlank()) {
            nombreCompleto = usuario.getEmail().split("@")[0];
        }
        String normalizado = Normalizer.normalize(nombreCompleto, Normalizer.Form.NFD)
                .replaceAll("[\\p{InCombiningDiacriticalMarks}]", "")
                .toLowerCase()
                .replaceAll("[^a-z0-9\\s-]", "")
                .trim()
                .replaceAll("\\s+", "-");
        String hashCorto = UUID.randomUUID().toString().substring(0, 4);
        return normalizado + "-" + hashCorto;
    }

    private PortafolioResponse toResponse(Portafolio p) {
        PortafolioResponse.ItemVisibilidadDto itemVis = null;
        if (p.getConfiguracionVisibilidad() != null) {
            try {
                VisibilidadItemsRequest parsed = objectMapper.readValue(
                        p.getConfiguracionVisibilidad(), VisibilidadItemsRequest.class);
                itemVis = PortafolioResponse.ItemVisibilidadDto.builder()
                        .showProjects(parsed.getShowProjects())
                        .techSkillItems(parsed.getTechSkillItems())
                        .softSkillItems(parsed.getSoftSkillItems())
                        .experienceItems(parsed.getExperienceItems())
                        .educationItems(parsed.getEducationItems())
                        .projectItems(parsed.getProjectItems())
                        .build();
            } catch (JsonProcessingException e) {
                log.warn("Error al parsear itemVisibilidad para portafolio {}: {}", p.getIdPortafolio(), e.getMessage());
            }
        }

        return PortafolioResponse.builder()
                .id(p.getIdPortafolio().toString())
                .nombre(p.getNombre())
                .visibilidad(p.getVisibilidad())
                .templateId(p.getPlantilla().getIdPlantilla())
                .publicUrl(p.getUrlPublica())
                .previewImageUrl(p.getImagenVistaPrevia())
                .createdAt(p.getFechaCreacion().toString())
                .itemVisibilidad(itemVis)
                .build();
    }
}
