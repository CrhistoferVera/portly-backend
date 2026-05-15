package com.portly.service;

import com.portly.domain.entity.ActualizacionAcademica;
import com.portly.domain.entity.Usuario;
import com.portly.domain.repository.ActualizacionAcademicaRepository;
import com.portly.domain.repository.UsuarioRepository;
import com.portly.dto.ActualizacionAcademicaRequest;
import com.portly.dto.ActualizacionAcademicaResponse;
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
public class ActualizacionAcademicaService {

    private final ActualizacionAcademicaRepository actualizacionRepository;
    private final UsuarioRepository                usuarioRepository;

    // GET /api/profile/actualizacion-academica
    @Transactional(readOnly = true)
    public List<ActualizacionAcademicaResponse> listarActualizaciones(UUID idUsuario) {
        return actualizacionRepository.findByUsuario_IdUsuario(idUsuario)
                .stream()
                .map(this::toDto)
                .collect(Collectors.toList());
    }

    // POST /api/profile/actualizacion-academica
    @Transactional
    public ActualizacionAcademicaResponse agregarActualizacion(UUID idUsuario, ActualizacionAcademicaRequest request) {
        Usuario usuario = usuarioRepository.findById(idUsuario)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Usuario no encontrado"));

        ActualizacionAcademica actualizacion = ActualizacionAcademica.builder()
                .usuario(usuario)
                .institucion(request.getInstitucion())
                .tipo(request.getTipo())
                .titulo(request.getTitulo())
                .fechaInicio(request.getFechaInicio())
                .fechaFinalizacion(request.getFechaFinalizacion())
                .aunNoLoFinalice(request.getAunNoLoFinalice())
                .descripcion(request.getDescripcion())
                .build();

        actualizacionRepository.save(actualizacion);
        log.info("Actualización académica agregada: idUsuario={}", idUsuario);
        return toDto(actualizacion);
    }

    // PUT /api/profile/actualizacion-academica/{id}
    @Transactional
    public ActualizacionAcademicaResponse actualizarActualizacion(UUID idUsuario, Long idActualizacion, ActualizacionAcademicaRequest request) {
        ActualizacionAcademica actualizacion = actualizacionRepository.findById(idActualizacion)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Actualización académica no encontrada"));

        verificarPropietario(actualizacion.getUsuario().getIdUsuario(), idUsuario);

        actualizacion.setInstitucion(request.getInstitucion());
        actualizacion.setTipo(request.getTipo());
        actualizacion.setTitulo(request.getTitulo());
        actualizacion.setFechaInicio(request.getFechaInicio());
        actualizacion.setFechaFinalizacion(request.getFechaFinalizacion());
        actualizacion.setAunNoLoFinalice(request.getAunNoLoFinalice());
        actualizacion.setDescripcion(request.getDescripcion());

        actualizacionRepository.save(actualizacion);
        log.info("Actualización académica editada: idActualizacion={}, idUsuario={}", idActualizacion, idUsuario);
        return toDto(actualizacion);
    }

    // DELETE /api/profile/actualizacion-academica/{id}
    @Transactional
    public void eliminarActualizacion(UUID idUsuario, Long idActualizacion) {
        ActualizacionAcademica actualizacion = actualizacionRepository.findById(idActualizacion)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Actualización académica no encontrada"));

        verificarPropietario(actualizacion.getUsuario().getIdUsuario(), idUsuario);

        actualizacionRepository.delete(actualizacion);
        log.info("Actualización académica eliminada: idActualizacion={}, idUsuario={}", idActualizacion, idUsuario);
    }

    private void verificarPropietario(UUID propietario, UUID solicitante) {
        if (!propietario.equals(solicitante)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "No tienes permiso para modificar esta actualización académica");
        }
    }

    private ActualizacionAcademicaResponse toDto(ActualizacionAcademica a) {
        return ActualizacionAcademicaResponse.builder()
                .idActualizacionAcademica(a.getIdActualizacionAcademica())
                .institucion(a.getInstitucion())
                .tipo(a.getTipo())
                .titulo(a.getTitulo())
                .fechaInicio(a.getFechaInicio())
                .fechaFinalizacion(a.getFechaFinalizacion())
                .aunNoLoFinalice(a.getAunNoLoFinalice())
                .descripcion(a.getDescripcion())
                .build();
    }
}
