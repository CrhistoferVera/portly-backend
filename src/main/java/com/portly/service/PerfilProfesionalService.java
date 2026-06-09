package com.portly.service;

import com.portly.domain.entity.Portafolio;
import com.portly.domain.entity.PerfilProfesional;
import com.portly.domain.entity.Usuario;
import com.portly.domain.repository.PerfilProfesionalRepository;
import com.portly.domain.repository.PortafolioRepository;
import com.portly.domain.repository.UsuarioRepository;
import com.portly.dto.PerfilProfesionalRequest;
import com.portly.dto.PerfilProfesionalResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class PerfilProfesionalService {

    private final PerfilProfesionalRepository perfilProfesionalRepository;
    private final PortafolioRepository portafolioRepository;
    private final UsuarioRepository usuarioRepository;

    // ──────────────────────────────────────────────────────────────
    // GET /api/perfiles-profesionales
    // ──────────────────────────────────────────────────────────────
    @Transactional(readOnly = true)
    public List<PerfilProfesionalResponse> listar(UUID idUsuario) {
        return perfilProfesionalRepository.findByUsuario_IdUsuario(idUsuario)
                .stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    // ──────────────────────────────────────────────────────────────
    // POST /api/perfiles-profesionales
    // ──────────────────────────────────────────────────────────────
    @Transactional
    public PerfilProfesionalResponse crear(UUID idUsuario, PerfilProfesionalRequest req) {
        Usuario usuario = usuarioRepository.findById(idUsuario)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Usuario no encontrado"));

        LocalDateTime now = LocalDateTime.now();
        PerfilProfesional perfil = PerfilProfesional.builder()
                .usuario(usuario)
                .etiqueta(req.getEtiqueta())
                .titularProfesional(req.getTitularProfesional())
                .acercaDeMi(req.getAcercaDeMi())
                .fotoUrl(req.getFotoUrl())
                .fechaCreacion(now)
                .fechaActualizacion(now)
                .build();

        PerfilProfesional saved = perfilProfesionalRepository.save(perfil);
        log.info("Perfil profesional creado: id={}, usuario={}", saved.getIdPerfilProfesional(), idUsuario);
        return toResponse(saved);
    }

    // ──────────────────────────────────────────────────────────────
    // PUT /api/perfiles-profesionales/{id}
    // ──────────────────────────────────────────────────────────────
    @Transactional
    public PerfilProfesionalResponse actualizar(UUID idUsuario, UUID idPerfil, PerfilProfesionalRequest req) {
        PerfilProfesional perfil = perfilProfesionalRepository.findById(idPerfil)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Perfil profesional no encontrado"));

        verificarPropietario(perfil, idUsuario);

        perfil.setEtiqueta(req.getEtiqueta());
        perfil.setTitularProfesional(req.getTitularProfesional());
        perfil.setAcercaDeMi(req.getAcercaDeMi());
        perfil.setFotoUrl(req.getFotoUrl());
        perfil.setFechaActualizacion(LocalDateTime.now());

        perfilProfesionalRepository.save(perfil);
        log.info("Perfil profesional actualizado: id={}, usuario={}", idPerfil, idUsuario);
        return toResponse(perfil);
    }

    // ──────────────────────────────────────────────────────────────
    // DELETE /api/perfiles-profesionales/{id}
    // ──────────────────────────────────────────────────────────────
    @Transactional
    public void eliminar(UUID idUsuario, UUID idPerfil) {
        PerfilProfesional perfil = perfilProfesionalRepository.findById(idPerfil)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Perfil profesional no encontrado"));

        verificarPropietario(perfil, idUsuario);

        // Limpiar la referencia en los portafolios que usaban este perfil,
        // para que caigan de vuelta al PerfilUsuario global y no romper la FK.
        List<Portafolio> afectados =
                portafolioRepository.findByPerfilProfesional_IdPerfilProfesional(idPerfil);
        if (!afectados.isEmpty()) {
            afectados.forEach(p -> p.setPerfilProfesional(null));
            portafolioRepository.saveAll(afectados);
        }

        perfilProfesionalRepository.delete(perfil);
        log.info("Perfil profesional eliminado: id={}, usuario={}, portafolios liberados={}",
                idPerfil, idUsuario, afectados.size());
    }

    // ──────────────────────────────────────────────────────────────
    // Helpers
    // ──────────────────────────────────────────────────────────────
    private void verificarPropietario(PerfilProfesional perfil, UUID idUsuario) {
        if (!perfil.getUsuario().getIdUsuario().equals(idUsuario)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN,
                    "No tienes permiso para modificar este perfil profesional");
        }
    }

    private PerfilProfesionalResponse toResponse(PerfilProfesional perfil) {
        return PerfilProfesionalResponse.builder()
                .id(perfil.getIdPerfilProfesional().toString())
                .etiqueta(perfil.getEtiqueta())
                .titularProfesional(perfil.getTitularProfesional())
                .acercaDeMi(perfil.getAcercaDeMi())
                .fotoUrl(perfil.getFotoUrl())
                .fechaCreacion(perfil.getFechaCreacion() != null ? perfil.getFechaCreacion().toString() : null)
                .build();
    }
}
