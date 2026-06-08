package com.portly.service;

import com.portly.domain.entity.Apelacion;
import com.portly.domain.entity.Usuario;
import com.portly.domain.entity.PerfilUsuario;
import com.portly.domain.repository.ApelacionRepository;
import com.portly.domain.repository.UsuarioRepository;
import com.portly.dto.AppealResponse;
import com.portly.dto.CrearApelacionRequest;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class ApelacionService {

    private final ApelacionRepository apelacionRepository;
    private final UsuarioRepository usuarioRepository;
    private final SuspensionService suspensionService;

    @Transactional
    public void crearApelacion(CrearApelacionRequest request) {
        Usuario usuario = usuarioRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new EntityNotFoundException("Usuario no encontrado con email: " + request.getEmail()));

        if (apelacionRepository.existsByUsuario_IdUsuarioAndEstadoApelacionIgnoreCase(usuario.getIdUsuario(), "pendiente")) {
            throw new IllegalStateException("Ya tienes una apelación en trámite");
        }

        Apelacion apelacion = Apelacion.builder()
                .usuario(usuario)
                .motivo(request.getMotivo())
                .tipoEstado(request.getTipoEstado().toLowerCase())
                .estadoApelacion("pendiente")
                .fechaApelacion(LocalDateTime.now())
                .build();

        apelacionRepository.save(apelacion);
        log.info("Nueva apelación registrada para el usuario id={}", usuario.getIdUsuario());
    }

    @Transactional(readOnly = true)
    public List<AppealResponse> listarApelaciones() {
        return apelacionRepository.findAllByOrderByFechaApelacionDesc()
                .stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public AppealResponse obtenerApelacion(Long id) {
        Apelacion apelacion = apelacionRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Apelación no encontrada con id: " + id));
        return toResponse(apelacion);
    }

    @Transactional
    public void aprobarApelacion(Long id, UUID adminId) {
        Apelacion apelacion = apelacionRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Apelación no encontrada con id: " + id));

        if (!"pendiente".equalsIgnoreCase(apelacion.getEstadoApelacion())) {
            throw new IllegalStateException("Esta apelación ya fue resuelta");
        }

        apelacion.setEstadoApelacion("aprobada");
        apelacion.setFechaResolucion(LocalDateTime.now());
        apelacion.setAdminId(adminId.toString());
        apelacionRepository.save(apelacion);

        // Reactivar al usuario
        suspensionService.reactivarUsuario(apelacion.getUsuario().getIdUsuario());
        log.info("Apelación id={} aprobada por admin={}. Usuario reactivado.", id, adminId);
    }

    @Transactional
    public void rechazarApelacion(Long id, UUID adminId) {
        Apelacion apelacion = apelacionRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Apelación no encontrada con id: " + id));

        if (!"pendiente".equalsIgnoreCase(apelacion.getEstadoApelacion())) {
            throw new IllegalStateException("Esta apelación ya fue resuelta");
        }

        apelacion.setEstadoApelacion("rechazada");
        apelacion.setFechaResolucion(LocalDateTime.now());
        apelacion.setAdminId(adminId.toString());
        apelacionRepository.save(apelacion);

        log.info("Apelación id={} rechazada por admin={}.", id, adminId);
    }

    private AppealResponse toResponse(Apelacion entity) {
        Usuario u = entity.getUsuario();
        PerfilUsuario perfil = u.getPerfil();
        String userName = perfil != null 
                ? (perfil.getNombre() + " " + perfil.getApellido()).trim()
                : "Sin Nombre";
        if (userName.isEmpty()) userName = "Sin Nombre";

        return AppealResponse.builder()
                .id(entity.getId())
                .userId(u.getIdUsuario().toString())
                .userName(userName)
                .userEmail(u.getEmail())
                .motivo(entity.getMotivo())
                .fechaApelacion(entity.getFechaApelacion())
                .estadoCuenta(u.getEstado())
                .estadoApelacion(entity.getEstadoApelacion())
                .fechaResolucion(entity.getFechaResolucion())
                .adminId(entity.getAdminId())
                .build();
    }
}
