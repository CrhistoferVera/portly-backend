package com.portly.service;

import com.portly.domain.entity.FormacionAcademica;
import com.portly.domain.entity.Usuario;
import com.portly.domain.repository.FormacionAcademicaRepository;
import com.portly.domain.repository.UsuarioRepository;
import com.portly.dto.FormacionAcademicaRequest;
import com.portly.dto.FormacionAcademicaResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class FormacionAcademicaService {

    private final FormacionAcademicaRepository formacionRepository;
    private final UsuarioRepository            usuarioRepository;

    // GET /api/profile/formacion — Listar formaciones del usuario autenticado
    @Transactional(readOnly = true)
    public List<FormacionAcademicaResponse> listarFormaciones(UUID idUsuario) {
        return formacionRepository.findByUsuario_IdUsuario(idUsuario)
                .stream()
                .map(this::toDto)
                .collect(Collectors.toList());
    }

    // POST /api/profile/formacion — Agregar nueva formación académica
    @Transactional
    public FormacionAcademicaResponse agregarFormacion(UUID idUsuario, FormacionAcademicaRequest request) {
        Usuario usuario = usuarioRepository.findById(idUsuario)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Usuario no encontrado"));

        FormacionAcademica formacion = FormacionAcademica.builder()
                .usuario(usuario)
                .institucion(request.getInstitucion())
                .carrera(request.getCarrera())
                .fechaInicio(request.getFechaInicio())
                .fechaFinalizacion(request.getFechaFinalizacion())
                .actualmenteEstudiando(request.getActualmenteEstudiando())
                .descripcion(request.getDescripcion())
                .nivel(request.getNivel())
                .build();

        formacionRepository.save(formacion);
        log.info("Formación académica agregada: idUsuario={}", idUsuario);
        return toDto(formacion);
    }

    // PUT /api/profile/formacion/{id} — Editar formación académica existente
    @Transactional
    public FormacionAcademicaResponse actualizarFormacion(UUID idUsuario, Long idFormacion, FormacionAcademicaRequest request) {
        FormacionAcademica formacion = formacionRepository.findById(idFormacion)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Formación académica no encontrada"));

        verificarPropietario(formacion.getUsuario().getIdUsuario(), idUsuario);

        formacion.setInstitucion(request.getInstitucion());
        formacion.setCarrera(request.getCarrera());
        formacion.setFechaInicio(request.getFechaInicio());
        formacion.setFechaFinalizacion(request.getFechaFinalizacion());
        formacion.setActualmenteEstudiando(request.getActualmenteEstudiando());
        formacion.setDescripcion(request.getDescripcion());
        formacion.setNivel(request.getNivel());

        formacionRepository.save(formacion);
        log.info("Formación académica actualizada: idFormacion={}, idUsuario={}", idFormacion, idUsuario);
        return toDto(formacion);
    }

    // DELETE /api/profile/formacion/{id} — Eliminar formación académica
    @Transactional
    public void eliminarFormacion(UUID idUsuario, Long idFormacion) {
        FormacionAcademica formacion = formacionRepository.findById(idFormacion)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Formación académica no encontrada"));

        verificarPropietario(formacion.getUsuario().getIdUsuario(), idUsuario);

        formacionRepository.delete(formacion);
        log.info("Formación académica eliminada: idFormacion={}, idUsuario={}", idFormacion, idUsuario);
    }

    // Verifica que el recurso pertenezca al usuario autenticado
    private void verificarPropietario(UUID propietario, UUID solicitante) {
        if (!propietario.equals(solicitante)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "No tienes permiso para modificar esta formación académica");
        }
    }

    // Mapeo entidad → DTO
    private FormacionAcademicaResponse toDto(FormacionAcademica f) {
        return FormacionAcademicaResponse.builder()
                .idFormacionAcademica(f.getIdFormacionAcademica())
                .institucion(f.getInstitucion())
                .carrera(f.getCarrera())
                .fechaInicio(f.getFechaInicio())
                .fechaFinalizacion(f.getFechaFinalizacion())
                .actualmenteEstudiando(f.getActualmenteEstudiando())
                .descripcion(f.getDescripcion())
                .nivel(f.getNivel())
                .build();
    }
}
