package com.portly.service;

import com.portly.domain.entity.ExperienciaLaboral;
import com.portly.domain.entity.Usuario;
import com.portly.domain.repository.ExperienciaLaboralRepository;
import com.portly.domain.repository.UsuarioRepository;
import com.portly.dto.ExperienceRequest;
import com.portly.dto.ExperienceResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDate;
import java.util.Collections;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class ExperienciaLaboralService {

    private final ExperienciaLaboralRepository experienciaRepository;
    private final UsuarioRepository usuarioRepository;

    // ──────────────────────────────────────────────────────────────
    // GET /api/profile/experiencia
    // ──────────────────────────────────────────────────────────────
    @Transactional(readOnly = true)
    public List<ExperienceResponse> listar(UUID idUsuario) {
        return experienciaRepository.findByUsuario_IdUsuario(idUsuario)
                .stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    // ──────────────────────────────────────────────────────────────
    // POST /api/profile/experiencia
    // ──────────────────────────────────────────────────────────────
    @Transactional
    public ExperienceResponse crear(UUID idUsuario, ExperienceRequest req) {
        Usuario usuario = usuarioRepository.findById(idUsuario)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Usuario no encontrado"));

        ExperienciaLaboral exp = ExperienciaLaboral.builder()
                .usuario(usuario)
                .empresa(req.getNombreEmpresa())
                .cargo(req.getCargo())
                .fechaInicio(LocalDate.parse(req.getFechaInicio()))
                .fechaFin(req.getFechaFin() != null && !req.getFechaFin().isBlank() ? LocalDate.parse(req.getFechaFin()) : null)
                .esEmpleoActual(req.getActualmenteTrabajando() != null ? req.getActualmenteTrabajando() : false)
                .descripcion(req.getDescripcion())
                .funcionesPrincipales(req.getFuncionesPrincipales() != null ? req.getFuncionesPrincipales() : Collections.emptyList())
                .logros(req.getLogros() != null ? req.getLogros() : Collections.emptyList())
                .correoJefe(req.getReferenciaProfesional() != null ? req.getReferenciaProfesional().getCorreoJefe() : null)
                .numeroJefe(req.getReferenciaProfesional() != null ? req.getReferenciaProfesional().getNumeroJefe() : null)
                .cargoJefe(req.getReferenciaProfesional() != null ? req.getReferenciaProfesional().getCargoJefe() : null)
                .build();

        ExperienciaLaboral saved = experienciaRepository.save(exp);
        log.info("Experiencia creada: id={}, usuario={}", saved.getIdExperienciaLaboral(), idUsuario);
        return toResponse(saved);
    }

    // ──────────────────────────────────────────────────────────────
    // PUT /api/profile/experiencia/{id}
    // ──────────────────────────────────────────────────────────────
    @Transactional
    public ExperienceResponse actualizar(UUID idUsuario, Integer idExperiencia, ExperienceRequest req) {
        ExperienciaLaboral exp = experienciaRepository.findById(idExperiencia)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Experiencia no encontrada"));

        verificarPropietario(exp, idUsuario);

        exp.setEmpresa(req.getNombreEmpresa());
        exp.setCargo(req.getCargo());
        exp.setFechaInicio(LocalDate.parse(req.getFechaInicio()));
        exp.setFechaFin(req.getFechaFin() != null && !req.getFechaFin().isBlank() ? LocalDate.parse(req.getFechaFin()) : null);
        exp.setEsEmpleoActual(req.getActualmenteTrabajando() != null ? req.getActualmenteTrabajando() : false);
        exp.setDescripcion(req.getDescripcion());
        exp.setFuncionesPrincipales(req.getFuncionesPrincipales() != null ? req.getFuncionesPrincipales() : Collections.emptyList());
        exp.setLogros(req.getLogros() != null ? req.getLogros() : Collections.emptyList());

        if (req.getReferenciaProfesional() != null) {
            exp.setCorreoJefe(req.getReferenciaProfesional().getCorreoJefe());
            exp.setNumeroJefe(req.getReferenciaProfesional().getNumeroJefe());
            exp.setCargoJefe(req.getReferenciaProfesional().getCargoJefe());
        }

        experienciaRepository.save(exp);
        log.info("Experiencia actualizada: id={}, usuario={}", idExperiencia, idUsuario);
        return toResponse(exp);
    }

    // ──────────────────────────────────────────────────────────────
    // DELETE /api/profile/experiencia/{id}
    // ──────────────────────────────────────────────────────────────
    @Transactional
    public void eliminar(UUID idUsuario, Integer idExperiencia) {
        ExperienciaLaboral exp = experienciaRepository.findById(idExperiencia)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Experiencia no encontrada"));

        verificarPropietario(exp, idUsuario);

        experienciaRepository.delete(exp);
        log.info("Experiencia eliminada: id={}, usuario={}", idExperiencia, idUsuario);
    }

    // ──────────────────────────────────────────────────────────────
    // Helpers
    // ──────────────────────────────────────────────────────────────
    private void verificarPropietario(ExperienciaLaboral exp, UUID idUsuario) {
        if (!exp.getUsuario().getIdUsuario().equals(idUsuario)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "No tienes permiso para modificar esta experiencia");
        }
    }

    private ExperienceResponse toResponse(ExperienciaLaboral exp) {
        ExperienceResponse.ReferenciaProfesionalDto ref = ExperienceResponse.ReferenciaProfesionalDto.builder()
                .correoJefe(exp.getCorreoJefe() != null ? exp.getCorreoJefe() : "")
                .numeroJefe(exp.getNumeroJefe() != null ? exp.getNumeroJefe() : "+591 ")
                .cargoJefe(exp.getCargoJefe() != null ? exp.getCargoJefe() : "")
                .build();

        return ExperienceResponse.builder()
                .id(exp.getIdExperienciaLaboral())
                .nombreEmpresa(exp.getEmpresa())
                .cargo(exp.getCargo())
                .fechaInicio(exp.getFechaInicio() != null ? exp.getFechaInicio().toString() : null)
                .fechaFin(exp.getFechaFin() != null ? exp.getFechaFin().toString() : null)
                .actualmenteTrabajando(exp.getEsEmpleoActual())
                .descripcion(exp.getDescripcion())
                .funcionesPrincipales(exp.getFuncionesPrincipales() != null ? exp.getFuncionesPrincipales() : Collections.emptyList())
                .logros(exp.getLogros() != null ? exp.getLogros() : Collections.emptyList())
                .referenciaProfesional(ref)
                .build();
    }
}
