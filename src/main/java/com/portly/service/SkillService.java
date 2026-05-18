package com.portly.service;

import com.portly.domain.entity.HabilidadTecnica;
import com.portly.domain.entity.Usuario;
import com.portly.domain.repository.HabilidadTecnicaRepository;
import com.portly.domain.repository.UsuarioRepository;
import com.portly.dto.SkillRequest;
import com.portly.dto.SkillResponse;
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
public class SkillService {

    private final HabilidadTecnicaRepository habilidadRepository;
    private final UsuarioRepository           usuarioRepository;

    @Transactional(readOnly = true)
    public List<SkillResponse> getAll(UUID idUsuario) {
        return habilidadRepository.findByUsuario_IdUsuario(idUsuario)
                .stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    @Transactional
    public SkillResponse create(UUID idUsuario, SkillRequest request) {
        Usuario usuario = usuarioRepository.findById(idUsuario)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Usuario no encontrado"));

        if (habilidadRepository.existsByUsuario_IdUsuarioAndNombreIgnoreCase(idUsuario, request.getName())) {
            throw new IllegalArgumentException("Ya tienes esta habilidad registrada");
        }

        HabilidadTecnica habilidad = HabilidadTecnica.builder()
                .usuario(usuario)
                .nombre(request.getName())
                .nivel(request.getLevel())
                .build();

        habilidadRepository.save(habilidad);
        log.info("Habilidad creada: idUsuario={}, nombre={}", idUsuario, request.getName());
        return toResponse(habilidad);
    }

    @Transactional
    public SkillResponse update(UUID idUsuario, Integer idHabilidad, SkillRequest request) {
        HabilidadTecnica habilidad = habilidadRepository.findById(idHabilidad)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Habilidad no encontrada"));

        if (!habilidad.getUsuario().getIdUsuario().equals(idUsuario)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "No tienes permiso para editar esta habilidad");
        }

        if (habilidadRepository.existsByUsuario_IdUsuarioAndNombreIgnoreCaseAndIdHabilidadNot(
                idUsuario, request.getName(), idHabilidad)) {
            throw new IllegalArgumentException("Ya tienes esta habilidad registrada");
        }

        habilidad.setNombre(request.getName());
        habilidad.setNivel(request.getLevel());
        habilidadRepository.save(habilidad);
        log.info("Habilidad actualizada: idHabilidad={}, idUsuario={}", idHabilidad, idUsuario);
        return toResponse(habilidad);
    }

    @Transactional
    public void delete(UUID idUsuario, Integer idHabilidad) {
        HabilidadTecnica habilidad = habilidadRepository.findById(idHabilidad)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Habilidad no encontrada"));

        if (!habilidad.getUsuario().getIdUsuario().equals(idUsuario)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "No tienes permiso para eliminar esta habilidad");
        }

        habilidadRepository.delete(habilidad);
        log.info("Habilidad eliminada: idHabilidad={}, idUsuario={}", idHabilidad, idUsuario);
    }

    private SkillResponse toResponse(HabilidadTecnica h) {
        return SkillResponse.builder()
                .id(h.getIdHabilidad())
                .name(h.getNombre())
                .level(h.getNivel())
                .build();
    }
}
