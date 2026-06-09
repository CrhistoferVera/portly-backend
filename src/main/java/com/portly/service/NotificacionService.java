package com.portly.service;

import com.portly.domain.entity.Notificacion;
import com.portly.domain.entity.Usuario;
import com.portly.domain.repository.NotificacionRepository;
import com.portly.domain.repository.UsuarioRepository;
import com.portly.dto.NotificacionResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class NotificacionService {

    private final NotificacionRepository notificacionRepository;
    private final UsuarioRepository usuarioRepository;

    @Transactional
    public void crearNotificacion(UUID userId, String mensaje) {
        Usuario usuario = usuarioRepository.findById(userId).orElse(null);
        if (usuario != null) {
            Notificacion notificacion = Notificacion.builder()
                    .usuario(usuario)
                    .mensaje(mensaje)
                    .leido(false)
                    .build();
            notificacionRepository.save(notificacion);
            log.info("Notificación creada para el usuario {}: {}", userId, mensaje);
        } else {
            log.warn("No se pudo crear notificación: Usuario {} no encontrado", userId);
        }
    }

    @Transactional(readOnly = true)
    public List<NotificacionResponse> obtenerNotificacionesUsuario(UUID userId) {
        return notificacionRepository.findByUsuario_IdUsuarioOrderByFechaCreacionDesc(userId)
                .stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public long obtenerCantidadNoLeidas(UUID userId) {
        return notificacionRepository.countByUsuario_IdUsuarioAndLeidoFalse(userId);
    }

    @Transactional
    public void marcarComoLeidas(UUID userId) {
        List<Notificacion> noLeidas = notificacionRepository.findByUsuario_IdUsuarioAndLeidoFalse(userId);
        if (!noLeidas.isEmpty()) {
            noLeidas.forEach(n -> n.setLeido(true));
            notificacionRepository.saveAll(noLeidas);
            log.info("Se marcaron {} notificaciones como leídas para el usuario {}", noLeidas.size(), userId);
        }
    }

    @Transactional
    public void marcarUnaComoLeida(UUID userId, UUID notificacionId) {
        notificacionRepository.findById(notificacionId).ifPresent(n -> {
            if (n.getUsuario().getIdUsuario().equals(userId) && !n.getLeido()) {
                n.setLeido(true);
                notificacionRepository.save(n);
                log.info("Notificación {} marcada como leída para el usuario {}", notificacionId, userId);
            }
        });
    }

    private NotificacionResponse mapToResponse(Notificacion notificacion) {
        return NotificacionResponse.builder()
                .id(notificacion.getId())
                .mensaje(notificacion.getMensaje())
                .leido(notificacion.getLeido())
                .fechaCreacion(notificacion.getFechaCreacion())
                .build();
    }
}
