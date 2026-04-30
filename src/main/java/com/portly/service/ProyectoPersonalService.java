package com.portly.service;

import com.portly.domain.entity.*;
import com.portly.domain.repository.*;
import com.portly.dto.EvidenciaProyectoResponse;
import com.portly.dto.FrontEvidenceResponse;
import com.portly.dto.FrontProjectRequest;
import com.portly.dto.FrontProjectResponse;
import com.portly.dto.ProyectoRequest;
import com.portly.dto.ProyectoResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class ProyectoPersonalService {

    private final ProyectoRepository proyectoRepository;
    private final UsuarioRepository usuarioRepository;
    private final HabilidadCatalogoRepository habilidadRepository;
    private final ProyectoEnlaceRepository proyectoEnlaceRepository;
    private final EvidenciaProyectoRepository evidenciaRepository;
    private final CloudinaryService cloudinaryService;

    // ──────────────────────────────────────────────────────────────
    // POST /api/proyectos  (multipart – endpoint legacy)
    // ──────────────────────────────────────────────────────────────
    @Transactional
    public ProyectoResponse crearProyecto(UUID idUsuario, ProyectoRequest request, MultipartFile iconoFile) {
        Usuario usuario = usuarioRepository.findById(idUsuario)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Usuario no encontrado"));

        Proyecto proyecto = Proyecto.builder()
                .usuario(usuario)
                .titulo(request.getTitulo())
                .resumen(request.getResumen())
                .descripcionRepositorio(request.getDescripcionRepositorio())
                .estadoPublicacion(request.getEstadoPublicacion())
                .fechaInicio(request.getFechaInicio())
                .fechaFin(request.getFechaFin())
                .esActual(request.getEsActual() != null ? request.getEsActual() : false)
                .importadoDesdeGithub(request.getImportadoDesdeGithub() != null ? request.getImportadoDesdeGithub() : false)
                .idRepositorioGithub(request.getIdRepositorioGithub())
                .fechaCreacion(LocalDateTime.now())
                .fechaActualizacion(LocalDateTime.now())
                .build();

        if (iconoFile != null && !iconoFile.isEmpty()) {
            try {
                proyecto.setEnlaceIcono(cloudinaryService.uploadImage(iconoFile, "portly/proyectos/iconos"));
            } catch (IOException e) {
                log.error("Error subiendo ícono de proyecto: {}", e.getMessage());
                throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Error al subir el ícono");
            }
        }

        if (request.getIdHabilidades() != null && !request.getIdHabilidades().isEmpty()) {
            proyecto.setTecnologias(habilidadRepository.findAllById(request.getIdHabilidades()));
        }

        Proyecto saved = proyectoRepository.save(proyecto);

        if (request.getIdEvidencias() != null && !request.getIdEvidencias().isEmpty()) {
            List<EvidenciaProyecto> evidencias = evidenciaRepository.findAllById(request.getIdEvidencias());
            for (EvidenciaProyecto ev : evidencias) {
                if (ev.getUsuario().getIdUsuario().equals(idUsuario)) {
                    ev.setProyecto(saved);
                }
            }
            evidenciaRepository.saveAll(evidencias);
            saved.setEvidencias(evidencias);
        }

        return toDto(saved);
    }

    // ──────────────────────────────────────────────────────────────
    // GET /api/proyectos
    // ──────────────────────────────────────────────────────────────
    @Transactional(readOnly = true)
    public List<ProyectoResponse> listarProyectos(UUID idUsuario) {
        return proyectoRepository.findByUsuario_IdUsuarioOrderByFechaCreacionDesc(idUsuario)
                .stream()
                .map(this::toDto)
                .collect(Collectors.toList());
    }

    // ──────────────────────────────────────────────────────────────
    // GET /api/proyectos/{id}
    // ──────────────────────────────────────────────────────────────
    @Transactional(readOnly = true)
    public ProyectoResponse obtenerProyecto(UUID idUsuario, Long idProyecto) {
        Proyecto proyecto = proyectoRepository.findById(idProyecto)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Proyecto no encontrado"));
        verificarPropietario(proyecto, idUsuario);
        return toDto(proyecto);
    }

    // ──────────────────────────────────────────────────────────────
    // PUT /api/proyectos/{id}  (multipart – endpoint legacy)
    // ──────────────────────────────────────────────────────────────
    @Transactional
    public ProyectoResponse actualizarProyecto(UUID idUsuario, Long idProyecto, ProyectoRequest request, MultipartFile iconoFile) {
        Proyecto proyecto = proyectoRepository.findById(idProyecto)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Proyecto no encontrado"));
        verificarPropietario(proyecto, idUsuario);

        proyecto.setTitulo(request.getTitulo());
        proyecto.setResumen(request.getResumen());
        proyecto.setDescripcionRepositorio(request.getDescripcionRepositorio());
        proyecto.setEstadoPublicacion(request.getEstadoPublicacion());
        proyecto.setFechaInicio(request.getFechaInicio());
        proyecto.setFechaFin(request.getFechaFin());
        proyecto.setEsActual(request.getEsActual() != null ? request.getEsActual() : false);
        proyecto.setFechaActualizacion(LocalDateTime.now());

        if (iconoFile != null && !iconoFile.isEmpty()) {
            try {
                proyecto.setEnlaceIcono(cloudinaryService.uploadImage(iconoFile, "portly/proyectos/iconos"));
            } catch (IOException e) {
                log.error("Error subiendo ícono de proyecto: {}", e.getMessage());
                throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Error al subir el ícono nuevo");
            }
        }

        if (request.getIdHabilidades() != null) {
            proyecto.setTecnologias(habilidadRepository.findAllById(request.getIdHabilidades()));
        } else {
            proyecto.getTecnologias().clear();
        }

        if (request.getIdEvidencias() != null) {
            for (EvidenciaProyecto ev : proyecto.getEvidencias()) ev.setProyecto(null);
            proyecto.getEvidencias().clear();
            if (!request.getIdEvidencias().isEmpty()) {
                List<EvidenciaProyecto> evidencias = evidenciaRepository.findAllById(request.getIdEvidencias());
                for (EvidenciaProyecto ev : evidencias) {
                    if (ev.getUsuario().getIdUsuario().equals(idUsuario)) {
                        ev.setProyecto(proyecto);
                        proyecto.getEvidencias().add(ev);
                    }
                }
            }
        }

        proyectoRepository.save(proyecto);
        return toDto(proyecto);
    }

    // ──────────────────────────────────────────────────────────────
    // DELETE /api/proyectos/{id}
    // ──────────────────────────────────────────────────────────────
    @Transactional
    public void eliminarProyecto(UUID idUsuario, Long idProyecto) {
        Proyecto proyecto = proyectoRepository.findById(idProyecto)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Proyecto no encontrado"));
        verificarPropietario(proyecto, idUsuario);

        // Desvincular evidencias
        for (EvidenciaProyecto ev : proyecto.getEvidencias()) ev.setProyecto(null);
        evidenciaRepository.saveAll(proyecto.getEvidencias());
        proyecto.getEvidencias().clear();

        // Los enlaces se eliminan en cascada (orphanRemoval = true)
        proyectoEnlaceRepository.deleteByProyecto_IdProyecto(idProyecto);
        proyecto.getEnlaces().clear();

        proyectoRepository.delete(proyecto);
        log.info("Proyecto eliminado: idProyecto={}, idUsuario={}", idProyecto, idUsuario);
    }

    private void verificarPropietario(Proyecto proyecto, UUID idUsuario) {
        if (!proyecto.getUsuario().getIdUsuario().equals(idUsuario)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "No tienes permiso para modificar este proyecto");
        }
    }

    // ──────────────────────────────────────────────────────────────
    // Métodos JSON para el frontend (/api/profile/proyectos)
    // ──────────────────────────────────────────────────────────────

    /**
     * Resuelve nombres de tecnologías a entidades HabilidadCatalogo.
     * Si no existe, la crea automáticamente.
     */
    private List<HabilidadCatalogo> resolverTecnologiasPorNombre(List<String> nombres) {
        if (nombres == null || nombres.isEmpty()) return List.of();
        return nombres.stream().map(nombre -> {
            String trimmed = nombre.trim();
            return habilidadRepository.findByNombreIgnoreCase(trimmed)
                    .orElseGet(() -> habilidadRepository.save(
                            HabilidadCatalogo.builder()
                                    .nombre(trimmed)
                                    .categoria("General")
                                    .activo(true)
                                    .build()));
        }).collect(Collectors.toList());
    }

    /**
     * Construye la lista de ProyectoEnlace a partir del DTO del frontend.
     */
    private List<ProyectoEnlace> buildEnlaces(List<FrontProjectRequest.EnlaceDto> dtos, Proyecto proyecto) {
        if (dtos == null || dtos.isEmpty()) return new ArrayList<>();
        return dtos.stream()
                .filter(e -> e.getTitulo() != null && !e.getTitulo().isBlank()
                        && e.getUrl() != null && !e.getUrl().isBlank())
                .map(e -> ProyectoEnlace.builder()
                        .proyecto(proyecto)
                        .titulo(e.getTitulo().trim())
                        .url(e.getUrl().trim())
                        .build())
                .collect(Collectors.toList());
    }

    /**
     * Crea un proyecto desde el DTO del frontend (JSON, no multipart).
     */
    @Transactional
    public FrontProjectResponse crearProyectoJson(UUID idUsuario, FrontProjectRequest req) {
        Usuario usuario = usuarioRepository.findById(idUsuario)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Usuario no encontrado"));

        Proyecto proyecto = Proyecto.builder()
                .usuario(usuario)
                .titulo(req.getNombre())
                .resumen(req.getDescripcionCorta())
                .descripcionRepositorio(req.getDescripcionDetallada())
                .estadoPublicacion(req.getVisibilidad() != null ? req.getVisibilidad().toUpperCase() : "PUBLICO")
                .enlaceIcono(req.getIconoUrl())
                .fechaInicio(req.getFechaInicio() != null ? java.time.LocalDate.parse(req.getFechaInicio()) : null)
                .fechaFin(req.getFechaFin() != null ? java.time.LocalDate.parse(req.getFechaFin()) : null)
                .esActual(req.getEsActual() != null ? req.getEsActual() : false)
                .importadoDesdeGithub(false)
                .fechaCreacion(LocalDateTime.now())
                .fechaActualizacion(LocalDateTime.now())
                .build();

        // Tecnologías
        proyecto.setTecnologias(resolverTecnologiasPorNombre(req.getTecnologias()));

        Proyecto saved = proyectoRepository.save(proyecto);

        // Enlaces
        List<ProyectoEnlace> enlaces = buildEnlaces(req.getEnlaces(), saved);
        if (!enlaces.isEmpty()) {
            proyectoEnlaceRepository.saveAll(enlaces);
            saved.setEnlaces(enlaces);
        }

        // Evidencias
        vincularEvidencias(req.getEvidencias(), saved, idUsuario);

        return toFrontDto(saved);
    }

    /**
     * Actualiza un proyecto desde el DTO del frontend (JSON).
     */
    @Transactional
    public FrontProjectResponse actualizarProyectoJson(UUID idUsuario, Long idProyecto, FrontProjectRequest req) {
        Proyecto proyecto = proyectoRepository.findById(idProyecto)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Proyecto no encontrado"));
        verificarPropietario(proyecto, idUsuario);

        proyecto.setTitulo(req.getNombre());
        proyecto.setResumen(req.getDescripcionCorta());
        proyecto.setDescripcionRepositorio(req.getDescripcionDetallada());
        proyecto.setEstadoPublicacion(req.getVisibilidad() != null ? req.getVisibilidad().toUpperCase() : "PUBLICO");
        proyecto.setEnlaceIcono(req.getIconoUrl());
        proyecto.setFechaInicio(req.getFechaInicio() != null ? java.time.LocalDate.parse(req.getFechaInicio()) : null);
        proyecto.setFechaFin(req.getFechaFin() != null ? java.time.LocalDate.parse(req.getFechaFin()) : null);
        proyecto.setEsActual(req.getEsActual() != null ? req.getEsActual() : false);
        proyecto.setFechaActualizacion(LocalDateTime.now());

        // Tecnologías (reemplazo completo)
        proyecto.setTecnologias(resolverTecnologiasPorNombre(req.getTecnologias()));

        // Enlaces (reemplazo completo)
        proyectoEnlaceRepository.deleteByProyecto_IdProyecto(idProyecto);
        proyecto.getEnlaces().clear();
        List<ProyectoEnlace> nuevosEnlaces = buildEnlaces(req.getEnlaces(), proyecto);
        if (!nuevosEnlaces.isEmpty()) {
            proyectoEnlaceRepository.saveAll(nuevosEnlaces);
            proyecto.getEnlaces().addAll(nuevosEnlaces);
        }

        // Evidencias (reemplazo completo)
        for (EvidenciaProyecto ev : proyecto.getEvidencias()) ev.setProyecto(null);
        proyecto.getEvidencias().clear();
        vincularEvidencias(req.getEvidencias(), proyecto, idUsuario);

        proyectoRepository.save(proyecto);
        return toFrontDto(proyecto);
    }

    /**
     * Lista proyectos en el formato que el frontend espera.
     */
    @Transactional(readOnly = true)
    public List<FrontProjectResponse> listarProyectosJson(UUID idUsuario) {
        return proyectoRepository.findByUsuario_IdUsuarioOrderByFechaCreacionDesc(idUsuario)
                .stream()
                .map(this::toFrontDto)
                .collect(Collectors.toList());
    }

    // ──────────────────────────────────────────────────────────────
    // Helpers privados
    // ──────────────────────────────────────────────────────────────

    private void vincularEvidencias(List<FrontProjectRequest.EvidenceDto> dtos, Proyecto proyecto, UUID idUsuario) {
        if (dtos == null || dtos.isEmpty()) return;
        List<Integer> ids = dtos.stream()
                .map(FrontProjectRequest.EvidenceDto::getId)
                .filter(id -> id != null)
                .collect(Collectors.toList());
        if (ids.isEmpty()) return;
        List<EvidenciaProyecto> evidencias = evidenciaRepository.findAllById(ids);
        for (EvidenciaProyecto ev : evidencias) {
            if (ev.getUsuario().getIdUsuario().equals(idUsuario)) {
                ev.setProyecto(proyecto);
                proyecto.getEvidencias().add(ev);
            }
        }
        evidenciaRepository.saveAll(evidencias);
    }

    /**
     * Convierte entidad Proyecto al DTO del frontend.
     */
    private FrontProjectResponse toFrontDto(Proyecto p) {
        List<FrontProjectResponse.EnlaceDto> enlaces = p.getEnlaces().stream()
                .map(e -> FrontProjectResponse.EnlaceDto.builder()
                        .titulo(e.getTitulo())
                        .url(e.getUrl())
                        .build())
                .collect(Collectors.toList());

        return FrontProjectResponse.builder()
                .id(p.getIdProyecto())
                .nombre(p.getTitulo())
                .descripcionCorta(p.getResumen())
                .descripcionDetallada(p.getDescripcionRepositorio())
                .fechaInicio(p.getFechaInicio() != null ? p.getFechaInicio().toString() : null)
                .fechaFin(p.getFechaFin() != null ? p.getFechaFin().toString() : null)
                .esActual(p.getEsActual())
                .tecnologias(p.getTecnologias().stream()
                        .map(HabilidadCatalogo::getNombre)
                        .collect(Collectors.toList()))
                .visibilidad(p.getEstadoPublicacion() != null ? p.getEstadoPublicacion().toLowerCase() : "publico")
                .enlaces(enlaces)
                .iconoUrl(p.getEnlaceIcono())
                .evidencias(p.getEvidencias().stream()
                        .map(e -> FrontEvidenceResponse.builder()
                                .id(e.getIdEvidenciaProyecto())
                                .nombre(e.getNombreOriginal())
                                .url(e.getEnlaceEvidencia())
                                .tipo(e.getFormato())
                                .pesoBytes(e.getTamanoBytes())
                                .build())
                        .collect(Collectors.toList()))
                .build();
    }

    private ProyectoResponse toDto(Proyecto p) {
        return ProyectoResponse.builder()
                .idProyecto(p.getIdProyecto())
                .titulo(p.getTitulo())
                .resumen(p.getResumen())
                .descripcionRepositorio(p.getDescripcionRepositorio())
                .enlaceIcono(p.getEnlaceIcono())
                .estadoPublicacion(p.getEstadoPublicacion())
                .fechaInicio(p.getFechaInicio())
                .fechaFin(p.getFechaFin())
                .esActual(p.getEsActual())
                .importadoDesdeGithub(p.getImportadoDesdeGithub())
                .idRepositorioGithub(p.getIdRepositorioGithub())
                .fechaSincronizacion(p.getFechaSincronizacion())
                .fechaCreacion(p.getFechaCreacion())
                .fechaActualizacion(p.getFechaActualizacion())
                .tecnologias(p.getTecnologias().stream()
                        .map(t -> ProyectoResponse.HabilidadDto.builder()
                                .idHabilidad(t.getIdHabilidad())
                                .nombre(t.getNombre())
                                .categoria(t.getCategoria())
                                .enlaceIcono(t.getEnlaceIcono())
                                .build())
                        .collect(Collectors.toList()))
                .evidencias(p.getEvidencias().stream()
                        .map(e -> EvidenciaProyectoResponse.builder()
                                .idEvidenciaProyecto(e.getIdEvidenciaProyecto())
                                .nombreOriginal(e.getNombreOriginal())
                                .enlaceEvidencia(e.getEnlaceEvidencia())
                                .enlaceMiniatura(e.getEnlaceMiniatura())
                                .formato(e.getFormato())
                                .tamanoBytes(e.getTamanoBytes())
                                .fechaSubida(e.getFechaSubida())
                                .build())
                        .collect(Collectors.toList()))
                .build();
    }
}
