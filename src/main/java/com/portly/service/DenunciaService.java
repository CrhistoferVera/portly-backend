package com.portly.service;

import com.portly.domain.entity.DenunciaAgrupada;
import com.portly.domain.entity.DenunciaIndividual;
import com.portly.domain.entity.Portafolio;
import com.portly.domain.entity.PerfilUsuario;
import com.portly.domain.entity.Usuario;
import com.portly.domain.repository.DenunciaAgrupadaRepository;
import com.portly.domain.repository.DenunciaIndividualRepository;
import com.portly.domain.repository.PortafolioRepository;
import com.portly.dto.DenunciaAgrupadaResponse;
import com.portly.dto.ReportarDenunciaRequest;
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
public class DenunciaService {

    private final DenunciaAgrupadaRepository denunciaAgrupadaRepository;
    private final DenunciaIndividualRepository denunciaIndividualRepository;
    private final PortafolioRepository portafolioRepository;

    /**
     * Lista todas las denuncias agrupadas con sus denuncias individuales.
     */
    public List<DenunciaAgrupadaResponse> listarDenuncias() {
        return denunciaAgrupadaRepository.findAllByOrderByCreatedAtDesc()
                .stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    /**
     * Obtiene el detalle de una denuncia agrupada por su ID.
     */
    public DenunciaAgrupadaResponse obtenerDenuncia(Long id) {
        DenunciaAgrupada denuncia = denunciaAgrupadaRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Denuncia no encontrada con id: " + id));
        return toResponse(denuncia);
    }

    /**
     * Marca una denuncia agrupada como revisada.
     */
    @Transactional
    public DenunciaAgrupadaResponse revisarDenuncia(Long id, String resultado, UUID adminId) {
        DenunciaAgrupada denuncia = denunciaAgrupadaRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Denuncia no encontrada con id: " + id));

        if ("revisado".equalsIgnoreCase(denuncia.getStatus())) {
            throw new IllegalStateException("La denuncia ya ha sido revisada");
        }

        denuncia.setStatus("revisado");
        denuncia.setRevisionResultado(resultado);
        denuncia.setRevisionFecha(LocalDateTime.now());
        denuncia.setRevisionAdminId(adminId.toString());

        DenunciaAgrupada saved = denunciaAgrupadaRepository.save(denuncia);
        log.info("Denuncia id={} marcada como revisada por admin={}", id, adminId);
        return toResponse(saved);
    }

    /**
     * Reporta un portafolio público. Crea o reutiliza una denuncia agrupada
     * y agrega una denuncia individual. Previene duplicados por reportante.
     */
    @Transactional
    public void reportarPortafolio(ReportarDenunciaRequest req) {
        // Validar que el portafolio existe
        Portafolio portafolio = portafolioRepository.findById(req.getPortfolioId())
                .orElseThrow(() -> new EntityNotFoundException(
                        "Portafolio no encontrado con id: " + req.getPortfolioId()));

        // Validar que sea público
        if (!"PUBLICO".equalsIgnoreCase(portafolio.getVisibilidad())) {
            throw new IllegalArgumentException("Solo se pueden reportar portafolios públicos");
        }

        Usuario owner = portafolio.getUsuario();
        PerfilUsuario perfil = owner.getPerfil();
        String ownerName = perfil != null
                ? (perfil.getNombre() + " " + perfil.getApellido()).trim()
                : "Sin Nombre";

        // Buscar o crear denuncia agrupada
        DenunciaAgrupada agrupada = denunciaAgrupadaRepository
                .findByPortafolio_IdPortafolioAndStatus(portafolio.getIdPortafolio(), "pendiente")
                .orElseGet(() -> {
                    DenunciaAgrupada nueva = DenunciaAgrupada.builder()
                            .portafolio(portafolio)
                            .portfolioTitle(portafolio.getNombre())
                            .portfolioPublicUrl(portafolio.getUrlPublica())
                            .ownerUsuario(owner)
                            .ownerUserName(ownerName)
                            .ownerUserStatus(owner.getEstado() != null ? owner.getEstado().toLowerCase() : "activo")
                            .status("pendiente")
                            .build();
                    return denunciaAgrupadaRepository.save(nueva);
                });

        // Verificar duplicados
        if (denunciaIndividualRepository.existsByDenunciaAgrupada_IdAndCreadoPor(
                agrupada.getId(), req.getReportedBy())) {
            throw new IllegalStateException("Ya enviaste un reporte para este portafolio.");
        }

        // Crear denuncia individual
        DenunciaIndividual individual = DenunciaIndividual.builder()
                .denunciaAgrupada(agrupada)
                .motivo(req.getReason())
                .descripcion(req.getDescription())
                .creadoPor(req.getReportedBy())
                .reporterName(req.getReporterName())
                .reporterAvatar(req.getReporterAvatar())
                .build();

        denunciaIndividualRepository.save(individual);
        log.info("Nuevo reporte para portafolio id={} por '{}'", req.getPortfolioId(), req.getReportedBy());
    }

    // ─── Mapper ──────────────────────────────────────────────────────────────

    private DenunciaAgrupadaResponse toResponse(DenunciaAgrupada entity) {
        List<DenunciaAgrupadaResponse.ComplaintItemResponse> complaints = entity.getDenunciasIndividuales()
                .stream()
                .map(di -> DenunciaAgrupadaResponse.ComplaintItemResponse.builder()
                        .id(di.getId())
                        .reason(di.getMotivo())
                        .description(di.getDescripcion())
                        .createdAt(di.getCreatedAt())
                        .reportedBy(di.getCreadoPor())
                        .reporterName(di.getReporterName())
                        .reporterAvatar(di.getReporterAvatar())
                        .build())
                .collect(Collectors.toList());

        DenunciaAgrupadaResponse.RevisionResponse revision = null;
        if ("revisado".equalsIgnoreCase(entity.getStatus()) && entity.getRevisionResultado() != null) {
            revision = DenunciaAgrupadaResponse.RevisionResponse.builder()
                    .resultado(entity.getRevisionResultado())
                    .fecha(entity.getRevisionFecha())
                    .adminId(entity.getRevisionAdminId())
                    .build();
        }

        return DenunciaAgrupadaResponse.builder()
                .id(entity.getId())
                .portfolioId(entity.getPortafolio().getIdPortafolio().toString())
                .portfolioTitle(entity.getPortfolioTitle())
                .portfolioPublicUrl(entity.getPortfolioPublicUrl())
                .ownerUserId(entity.getOwnerUsuario().getIdUsuario().toString())
                .ownerUserName(entity.getOwnerUserName())
                .ownerUserStatus(entity.getOwnerUserStatus())
                .status(entity.getStatus())
                .complaints(complaints)
                .revision(revision)
                .createdAt(entity.getCreatedAt())
                .updatedAt(entity.getUpdatedAt())
                .build();
    }
}
