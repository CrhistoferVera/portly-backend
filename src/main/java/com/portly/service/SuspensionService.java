package com.portly.service;

import com.portly.domain.entity.DenunciaAgrupada;
import com.portly.domain.entity.Suspension;
import com.portly.domain.entity.Usuario;
import com.portly.domain.repository.DenunciaAgrupadaRepository;
import com.portly.domain.repository.PortafolioRepository;
import com.portly.domain.repository.SuspensionRepository;
import com.portly.domain.repository.UsuarioRepository;
import com.portly.dto.SuspensionResponse;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class SuspensionService {

    private final SuspensionRepository suspensionRepository;
    private final UsuarioRepository usuarioRepository;
    private final DenunciaAgrupadaRepository denunciaAgrupadaRepository;
    private final PortafolioRepository portafolioRepository;
    private final EmailService emailService;

    /**
     * Suspende un usuario. Crea registro de suspensión, actualiza estado del usuario
     * y actualiza el ownerUserStatus en todas las denuncias agrupadas del usuario.
     */
    @Transactional
    public SuspensionResponse suspenderUsuario(UUID userId, String motivo, UUID adminId) {
        Usuario usuario = usuarioRepository.findById(userId)
                .orElseThrow(() -> new EntityNotFoundException("Usuario no encontrado con id: " + userId));

        // Verificar que no esté ya suspendido
        if ("suspendido".equalsIgnoreCase(usuario.getEstado())) {
            throw new IllegalStateException("El usuario ya se encuentra suspendido");
        }

        // Cancelar cualquier suspensión/restricción activa previa para evitar duplicados
        List<Suspension> activeSuspensions = suspensionRepository.findAllByUsuario_IdUsuarioAndCanceladaFalse(userId);
        for (Suspension s : activeSuspensions) {
            s.setCancelada(true);
            s.setFechaCancelacion(LocalDateTime.now());
        }
        suspensionRepository.saveAll(activeSuspensions);

        // Crear registro de suspensión
        Suspension suspension = Suspension.builder()
                .usuario(usuario)
                .motivo(motivo)
                .adminId(adminId.toString())
                .build();
        suspension = suspensionRepository.save(suspension);

        // Actualizar estado del usuario
        usuario.setEstado("suspendido");
        usuarioRepository.save(usuario);

        // Privatizar portafolios del usuario
        List<com.portly.domain.entity.Portafolio> portfolios = portafolioRepository.findByUsuario_IdUsuarioOrderByFechaCreacionDesc(userId);
        for (com.portly.domain.entity.Portafolio p : portfolios) {
            p.setVisibilidad("PRIVADO");
        }
        portafolioRepository.saveAll(portfolios);

        // Actualizar ownerUserStatus en todas las denuncias agrupadas del usuario
        List<DenunciaAgrupada> denuncias = denunciaAgrupadaRepository
                .findAllByOwnerUsuario_IdUsuario(userId);
        for (DenunciaAgrupada denuncia : denuncias) {
            denuncia.setOwnerUserStatus("suspendido");
            if ("pendiente".equalsIgnoreCase(denuncia.getStatus())) {
                denuncia.setStatus("revisado");
                denuncia.setRevisionResultado("Usuario suspendido: " + motivo);
                denuncia.setRevisionFecha(LocalDateTime.now());
                denuncia.setRevisionAdminId(adminId.toString());
            }
        }
        denunciaAgrupadaRepository.saveAll(denuncias);

        log.info("Usuario id={} suspendido por admin={}, motivo='{}'", userId, adminId, motivo);

        String userName = obtenerNombreUsuario(usuario);
        
        notificarDenunciantes(denuncias, userName, true);

        return SuspensionResponse.builder()
                .id(suspension.getId())
                .userId(userId.toString())
                .userName(userName)
                .motivo(suspension.getMotivo())
                .fechaSuspension(suspension.getFechaSuspension())
                .adminId(suspension.getAdminId())
                .build();
    }

    /**
     * Reactiva un usuario suspendido. Marca la suspensión como cancelada,
     * actualiza estado del usuario y denuncias agrupadas.
     */
    @Transactional
    public void reactivarUsuario(UUID userId) {
        Usuario usuario = usuarioRepository.findById(userId)
                .orElseThrow(() -> new EntityNotFoundException("Usuario no encontrado con id: " + userId));

        List<Suspension> activeSuspensions = suspensionRepository.findAllByUsuario_IdUsuarioAndCanceladaFalse(userId);
        if (activeSuspensions.isEmpty()) {
            log.warn("No se encontró una suspensión activa para el usuario id={}. Se procederá a reactivar de todas formas.", userId);
        } else {
            // Marcar todas las suspensiones activas como canceladas
            for (Suspension suspension : activeSuspensions) {
                suspension.setCancelada(true);
                suspension.setFechaCancelacion(LocalDateTime.now());
            }
            suspensionRepository.saveAll(activeSuspensions);
        }

        // Actualizar estado del usuario
        usuario.setEstado("activo");
        usuarioRepository.save(usuario);

        // Actualizar ownerUserStatus en denuncias agrupadas y resolver las pendientes
        List<DenunciaAgrupada> denuncias = denunciaAgrupadaRepository
                .findAllByOwnerUsuario_IdUsuario(userId);
        for (DenunciaAgrupada denuncia : denuncias) {
            denuncia.setOwnerUserStatus("activo");
            if ("pendiente".equalsIgnoreCase(denuncia.getStatus())) {
                denuncia.setStatus("revisado");
                denuncia.setRevisionResultado("Reactivado tras apelación / revisión de cuenta");
                denuncia.setRevisionFecha(LocalDateTime.now());
            }
        }
        denunciaAgrupadaRepository.saveAll(denuncias);

        log.info("Usuario id={} reactivado. {} suspensiones canceladas", userId, activeSuspensions.size());
        
        emailService.enviarNotificacionReactivacionCuenta(usuario.getEmail(), obtenerNombreUsuario(usuario));
    }

    /**
     * Restringe un usuario (similar a suspender pero con estado restringido).
     */
    @Transactional
    public SuspensionResponse restringirUsuario(UUID userId, String motivo, UUID adminId) {
        Usuario usuario = usuarioRepository.findById(userId)
                .orElseThrow(() -> new EntityNotFoundException("Usuario no encontrado con id: " + userId));

        // Verificar que no esté ya restringido
        if ("restringido".equalsIgnoreCase(usuario.getEstado())) {
            throw new IllegalStateException("El usuario ya se encuentra restringido");
        }

        // Cancelar cualquier suspensión/restricción activa previa para evitar duplicados
        List<Suspension> activeSuspensions = suspensionRepository.findAllByUsuario_IdUsuarioAndCanceladaFalse(userId);
        for (Suspension s : activeSuspensions) {
            s.setCancelada(true);
            s.setFechaCancelacion(LocalDateTime.now());
        }
        suspensionRepository.saveAll(activeSuspensions);

        // Crear registro de suspensión (usado también para restricción)
        Suspension suspension = Suspension.builder()
                .usuario(usuario)
                .motivo(motivo)
                .adminId(adminId.toString())
                .build();
        suspension = suspensionRepository.save(suspension);

        // Actualizar estado del usuario a restringido
        usuario.setEstado("restringido");
        usuarioRepository.save(usuario);

        // Privatizar portafolios del usuario
        List<com.portly.domain.entity.Portafolio> portfolios = portafolioRepository.findByUsuario_IdUsuarioOrderByFechaCreacionDesc(userId);
        for (com.portly.domain.entity.Portafolio p : portfolios) {
            p.setVisibilidad("PRIVADO");
        }
        portafolioRepository.saveAll(portfolios);

        // Actualizar ownerUserStatus en todas las denuncias agrupadas del usuario
        List<DenunciaAgrupada> denuncias = denunciaAgrupadaRepository
                .findAllByOwnerUsuario_IdUsuario(userId);
        for (DenunciaAgrupada denuncia : denuncias) {
            denuncia.setOwnerUserStatus("restringido");
            if ("pendiente".equalsIgnoreCase(denuncia.getStatus())) {
                denuncia.setStatus("revisado");
                denuncia.setRevisionResultado("Usuario restringido: " + motivo);
                denuncia.setRevisionFecha(LocalDateTime.now());
                denuncia.setRevisionAdminId(adminId.toString());
            }
        }
        denunciaAgrupadaRepository.saveAll(denuncias);

        log.info("Usuario id={} restringido por admin={}, motivo='{}'", userId, adminId, motivo);

        String userName = obtenerNombreUsuario(usuario);
        
        notificarDenunciantes(denuncias, userName, false);

        return SuspensionResponse.builder()
                .id(suspension.getId())
                .userId(userId.toString())
                .userName(userName)
                .motivo(suspension.getMotivo())
                .fechaSuspension(suspension.getFechaSuspension())
                .adminId(suspension.getAdminId())
                .build();
    }

    /**
     * Lista todos los usuarios con suspensiones activas.
     */
    public List<SuspensionResponse> listarSuspendidos() {
        return suspensionRepository.findAllByCanceladaFalse()
                .stream()
                .map(s -> {
                    String userName = obtenerNombreUsuario(s.getUsuario());
                    return SuspensionResponse.builder()
                            .id(s.getId())
                            .userId(s.getUsuario().getIdUsuario().toString())
                            .userName(userName)
                            .motivo(s.getMotivo())
                            .fechaSuspension(s.getFechaSuspension())
                            .adminId(s.getAdminId())
                            .build();
                })
                .collect(Collectors.toList());
    }

    // ─── Helpers ──────────────────────────────────────────────────────────────

    private void notificarDenunciantes(List<DenunciaAgrupada> denuncias, String userName, boolean esSuspension) {
        Set<String> correosNotificados = new HashSet<>();
        for (DenunciaAgrupada denuncia : denuncias) {
            for (com.portly.domain.entity.DenunciaIndividual ind : denuncia.getDenunciasIndividuales()) {
                String creadoPor = ind.getCreadoPor();
                if (creadoPor == null || creadoPor.isBlank()) continue;
                
                String emailDestino = null;
                try {
                    UUID id = UUID.fromString(creadoPor);
                    emailDestino = usuarioRepository.findById(id)
                            .map(Usuario::getEmail)
                            .orElse(null);
                } catch (IllegalArgumentException e) {
                    if (creadoPor.contains("@")) {
                        emailDestino = creadoPor;
                    }
                }
                
                if (emailDestino != null && !correosNotificados.contains(emailDestino)) {
                    if (esSuspension) {
                        emailService.enviarNotificacionSuspensionDenunciantes(emailDestino, userName);
                    } else {
                        emailService.enviarNotificacionRestriccionDenunciantes(emailDestino, userName);
                    }
                    correosNotificados.add(emailDestino);
                }
            }
        }
    }

    private String obtenerNombreUsuario(Usuario usuario) {
        if (usuario.getPerfil() != null) {
            String nombre = usuario.getPerfil().getNombre();
            String apellido = usuario.getPerfil().getApellido();
            String completo = ((nombre != null ? nombre : "") + " " + (apellido != null ? apellido : "")).trim();
            return completo.isEmpty() ? "Sin Nombre" : completo;
        }
        return "Sin Nombre";
    }
}
