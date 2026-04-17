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
    private final ProyectoRepositorioRepository proyectoRepositorioRepository;
    private final EvidenciaProyectoRepository evidenciaRepository;
    private final CloudinaryService cloudinaryService;

    // ──────────────────────────────────────────────────────────────
    // POST /api/proyectos
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
                .enlaceProyectoDeplegado(request.getEnlaceProyectoDeplegado())
                .estadoPublicacion(request.getEstadoPublicacion())
                .fechaInicio(request.getFechaInicio())
                .fechaFin(request.getFechaFin())
                .esActual(request.getEsActual() != null ? request.getEsActual() : false)
                .importadoDesdeGithub(request.getImportadoDesdeGithub() != null ? request.getImportadoDesdeGithub() : false)
                .idRepositorioGithub(request.getIdRepositorioGithub())
                .fechaCreacion(LocalDateTime.now())
                .fechaActualizacion(LocalDateTime.now())
                .build();

        // 1. Manejar ícono
        if (iconoFile != null && !iconoFile.isEmpty()) {
            try {
                String iconoUrl = cloudinaryService.uploadImage(iconoFile, "portly/proyectos/iconos");
                proyecto.setEnlaceIcono(iconoUrl);
            } catch (IOException e) {
                log.error("Error subiendo ícono de proyecto: {}", e.getMessage());
                throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Error al subir el ícono");
            }
        }

        // 2. Vincular tecnologías
        if (request.getIdHabilidades() != null && !request.getIdHabilidades().isEmpty()) {
            List<HabilidadCatalogo> techList = habilidadRepository.findAllById(request.getIdHabilidades());
            proyecto.setTecnologias(techList);
        }

        // Se guarda el proyecto para obtener el ID
        Proyecto savedProyecto = proyectoRepository.save(proyecto);

        // 3. Crear repositorios adicionales
        if (request.getUrlsRepositorios() != null && !request.getUrlsRepositorios().isEmpty()) {
            List<ProyectoRepositorio> reposAdd = request.getUrlsRepositorios().stream().map(url ->
                ProyectoRepositorio.builder()
                    .proyecto(savedProyecto)
                    .url(url)
                    .build()
            ).collect(Collectors.toList());
            proyectoRepositorioRepository.saveAll(reposAdd);
            savedProyecto.setRepositorios(reposAdd);
        }

        // 4. Vincular evidencias huérfanas o reasignar
        if (request.getIdEvidencias() != null && !request.getIdEvidencias().isEmpty()) {
            List<EvidenciaProyecto> evidencias = evidenciaRepository.findAllById(request.getIdEvidencias());
            for (EvidenciaProyecto ev : evidencias) {
                // Solo vincular si pertenece al mismo usuario
                if (ev.getUsuario().getIdUsuario().equals(idUsuario)) {
                    ev.setProyecto(savedProyecto);
                }
            }
            evidenciaRepository.saveAll(evidencias);
            savedProyecto.setEvidencias(evidencias);
        }

        return toDto(savedProyecto);
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
    // PUT /api/proyectos/{id}
    // ──────────────────────────────────────────────────────────────
    @Transactional
    public ProyectoResponse actualizarProyecto(UUID idUsuario, Long idProyecto, ProyectoRequest request, MultipartFile iconoFile) {
        Proyecto proyecto = proyectoRepository.findById(idProyecto)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Proyecto no encontrado"));
        
        verificarPropietario(proyecto, idUsuario);

        proyecto.setTitulo(request.getTitulo());
        proyecto.setResumen(request.getResumen());
        proyecto.setDescripcionRepositorio(request.getDescripcionRepositorio());
        proyecto.setEnlaceProyectoDeplegado(request.getEnlaceProyectoDeplegado());
        proyecto.setEstadoPublicacion(request.getEstadoPublicacion());
        proyecto.setFechaInicio(request.getFechaInicio());
        proyecto.setFechaFin(request.getFechaFin());
        proyecto.setEsActual(request.getEsActual() != null ? request.getEsActual() : false);
        proyecto.setFechaActualizacion(LocalDateTime.now());

        // 1. Manejar ícono (si viene uno nuevo, reemplaza al anterior)
        if (iconoFile != null && !iconoFile.isEmpty()) {
            try {
                String iconoUrl = cloudinaryService.uploadImage(iconoFile, "portly/proyectos/iconos");
                proyecto.setEnlaceIcono(iconoUrl);
            } catch (IOException e) {
                log.error("Error subiendo ícono de proyecto: {}", e.getMessage());
                throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Error al subir el ícono nuevo");
            }
        }

        // 2. Actualizar tecnologías (reemplazo completo)
        if (request.getIdHabilidades() != null) {
            List<HabilidadCatalogo> techList = habilidadRepository.findAllById(request.getIdHabilidades());
            proyecto.setTecnologias(techList);
        } else {
            proyecto.getTecnologias().clear();
        }

        // 3. Actualizar repositorios adicionales
        // Primero borramos los anteriores y luego insertamos los nuevos
        proyectoRepositorioRepository.deleteByProyecto_IdProyecto(idProyecto);
        proyecto.getRepositorios().clear();
        
        if (request.getUrlsRepositorios() != null && !request.getUrlsRepositorios().isEmpty()) {
            List<ProyectoRepositorio> reposAdd = request.getUrlsRepositorios().stream().map(url ->
                ProyectoRepositorio.builder()
                    .proyecto(proyecto)
                    .url(url)
                    .build()
            ).collect(Collectors.toList());
            proyectoRepositorioRepository.saveAll(reposAdd);
            proyecto.setRepositorios(reposAdd);
        }

        // 4. Actualizar evidencias
        if (request.getIdEvidencias() != null) {
            // Desvincular todas las actuales
            for (EvidenciaProyecto ev : proyecto.getEvidencias()) {
                ev.setProyecto(null);
            }
            proyecto.getEvidencias().clear();
            
            // Vincular las nuevas
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

        // Desvincular evidencias (no se eliminan, solo se quita la FK al proyecto)
        for (EvidenciaProyecto ev : proyecto.getEvidencias()) {
            ev.setProyecto(null);
        }
        evidenciaRepository.saveAll(proyecto.getEvidencias());
        proyecto.getEvidencias().clear();

        // Eliminar repositorios vinculados
        proyectoRepositorioRepository.deleteByProyecto_IdProyecto(idProyecto);
        proyecto.getRepositorios().clear();

        proyectoRepository.delete(proyecto);
        log.info("Proyecto eliminado: idProyecto={}, idUsuario={}", idProyecto, idUsuario);
    }

    private void verificarPropietario(Proyecto proyecto, UUID idUsuario) {
        if (!proyecto.getUsuario().getIdUsuario().equals(idUsuario)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "No tienes permiso para modificar este proyecto");
        }
    }

    // ──────────────────────────────────────────────────────────────
    // Métodos adaptadores para el frontend (JSON)
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
                    .orElseGet(() -> {
                        HabilidadCatalogo nueva = HabilidadCatalogo.builder()
                                .nombre(trimmed)
                                .categoria("General")
                                .activo(true)
                                .build();
                        return habilidadRepository.save(nueva);
                    });
        }).collect(Collectors.toList());
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
                .enlaceProyectoDeplegado(req.getUrlDemo())
                .estadoPublicacion(req.getVisibilidad() != null ? req.getVisibilidad().toUpperCase() : "PUBLICO")
                .enlaceIcono(req.getIconoUrl())
                .fechaInicio(req.getFechaInicio() != null ? java.time.LocalDate.parse(req.getFechaInicio()) : null)
                .fechaFin(req.getFechaFin() != null ? java.time.LocalDate.parse(req.getFechaFin()) : null)
                .esActual(req.getEsActual() != null ? req.getEsActual() : false)
                .importadoDesdeGithub(false)
                .fechaCreacion(LocalDateTime.now())
                .fechaActualizacion(LocalDateTime.now())
                .build();

        // Tecnologías (por nombre)
        List<HabilidadCatalogo> techs = resolverTecnologiasPorNombre(req.getTecnologias());
        proyecto.setTecnologias(techs);

        Proyecto saved = proyectoRepository.save(proyecto);

        // Repositorios
        if (req.getRepositorios() != null && !req.getRepositorios().isEmpty()) {
            List<ProyectoRepositorio> repos = req.getRepositorios().stream()
                    .filter(url -> url != null && !url.isBlank())
                    .map(url -> ProyectoRepositorio.builder().proyecto(saved).url(url).build())
                    .collect(Collectors.toList());
            proyectoRepositorioRepository.saveAll(repos);
            saved.setRepositorios(repos);
        }

        // Evidencias
        if (req.getEvidencias() != null && !req.getEvidencias().isEmpty()) {
            List<Integer> ids = req.getEvidencias().stream()
                    .map(FrontProjectRequest.EvidenceDto::getId)
                    .filter(id -> id != null)
                    .collect(Collectors.toList());
            if (!ids.isEmpty()) {
                List<EvidenciaProyecto> evidencias = evidenciaRepository.findAllById(ids);
                for (EvidenciaProyecto ev : evidencias) {
                    if (ev.getUsuario().getIdUsuario().equals(idUsuario)) {
                        ev.setProyecto(saved);
                    }
                }
                evidenciaRepository.saveAll(evidencias);
                saved.setEvidencias(evidencias);
            }
        }

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
        proyecto.setEnlaceProyectoDeplegado(req.getUrlDemo());
        proyecto.setEstadoPublicacion(req.getVisibilidad() != null ? req.getVisibilidad().toUpperCase() : "PUBLICO");
        proyecto.setEnlaceIcono(req.getIconoUrl());
        proyecto.setFechaInicio(req.getFechaInicio() != null ? java.time.LocalDate.parse(req.getFechaInicio()) : null);
        proyecto.setFechaFin(req.getFechaFin() != null ? java.time.LocalDate.parse(req.getFechaFin()) : null);
        proyecto.setEsActual(req.getEsActual() != null ? req.getEsActual() : false);
        proyecto.setFechaActualizacion(LocalDateTime.now());

        // Tecnologías
        List<HabilidadCatalogo> techs = resolverTecnologiasPorNombre(req.getTecnologias());
        proyecto.setTecnologias(techs);

        // Repositorios (reemplazo completo)
        proyectoRepositorioRepository.deleteByProyecto_IdProyecto(idProyecto);
        proyecto.getRepositorios().clear();

        if (req.getRepositorios() != null && !req.getRepositorios().isEmpty()) {
            List<ProyectoRepositorio> repos = req.getRepositorios().stream()
                    .filter(url -> url != null && !url.isBlank())
                    .map(url -> ProyectoRepositorio.builder().proyecto(proyecto).url(url).build())
                    .collect(Collectors.toList());
            proyectoRepositorioRepository.saveAll(repos);
            proyecto.getRepositorios().addAll(repos);
        }

        // Evidencias (reemplazo completo)
        for (EvidenciaProyecto ev : proyecto.getEvidencias()) {
            ev.setProyecto(null);
        }
        proyecto.getEvidencias().clear();

        if (req.getEvidencias() != null && !req.getEvidencias().isEmpty()) {
            List<Integer> ids = req.getEvidencias().stream()
                    .map(FrontProjectRequest.EvidenceDto::getId)
                    .filter(id -> id != null)
                    .collect(Collectors.toList());
            if (!ids.isEmpty()) {
                List<EvidenciaProyecto> evidencias = evidenciaRepository.findAllById(ids);
                for (EvidenciaProyecto ev : evidencias) {
                    if (ev.getUsuario().getIdUsuario().equals(idUsuario)) {
                        ev.setProyecto(proyecto);
                        proyecto.getEvidencias().add(ev);
                    }
                }
            }
        }

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

    /**
     * Convierte entidad Proyecto al DTO del frontend.
     */
    private FrontProjectResponse toFrontDto(Proyecto p) {
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
                .urlDemo(p.getEnlaceProyectoDeplegado())
                .repositorios(p.getRepositorios().stream()
                        .map(ProyectoRepositorio::getUrl)
                        .collect(Collectors.toList()))
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
                .enlaceProyectoDeplegado(p.getEnlaceProyectoDeplegado())
                .estadoPublicacion(p.getEstadoPublicacion())
                .fechaInicio(p.getFechaInicio())
                .fechaFin(p.getFechaFin())
                .esActual(p.getEsActual())
                .importadoDesdeGithub(p.getImportadoDesdeGithub())
                .idRepositorioGithub(p.getIdRepositorioGithub())
                .fechaSincronizacion(p.getFechaSincronizacion())
                .fechaCreacion(p.getFechaCreacion())
                .fechaActualizacion(p.getFechaActualizacion())
                .urlsRepositorios(p.getRepositorios().stream()
                        .map(ProyectoRepositorio::getUrl)
                        .collect(Collectors.toList()))
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

