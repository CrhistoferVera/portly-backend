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
import java.util.List;
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
        }
        denunciaAgrupadaRepository.saveAll(denuncias);

        log.info("Usuario id={} suspendido por admin={}, motivo='{}'", userId, adminId, motivo);

        String userName = obtenerNombreUsuario(usuario);

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

        Suspension suspension = suspensionRepository.findByUsuario_IdUsuarioAndCanceladaFalse(userId)
                .orElseThrow(() -> new EntityNotFoundException(
                        "No se encontró una suspensión activa para el usuario: " + userId));

        // Marcar suspensión como cancelada
        suspension.setCancelada(true);
        suspension.setFechaCancelacion(LocalDateTime.now());
        suspensionRepository.save(suspension);

        // Actualizar estado del usuario
        usuario.setEstado("activo");
        usuarioRepository.save(usuario);

        // Actualizar ownerUserStatus en denuncias agrupadas
        List<DenunciaAgrupada> denuncias = denunciaAgrupadaRepository
                .findAllByOwnerUsuario_IdUsuario(userId);
        for (DenunciaAgrupada denuncia : denuncias) {
            denuncia.setOwnerUserStatus("activo");
        }
        denunciaAgrupadaRepository.saveAll(denuncias);

        log.info("Usuario id={} reactivado. Suspensión id={} cancelada", userId, suspension.getId());
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
        }
        denunciaAgrupadaRepository.saveAll(denuncias);

        log.info("Usuario id={} restringido por admin={}, motivo='{}'", userId, adminId, motivo);

        String userName = obtenerNombreUsuario(usuario);

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
