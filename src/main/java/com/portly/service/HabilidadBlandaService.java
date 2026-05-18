package com.portly.service;

import com.portly.domain.entity.HabilidadBlanda;
import com.portly.domain.entity.Usuario;
import com.portly.domain.repository.HabilidadBlandaRepository;
import com.portly.domain.repository.UsuarioRepository;
import com.portly.dto.HabilidadBlandaRequest;
import com.portly.dto.HabilidadBlandaResponse;
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
public class HabilidadBlandaService {

    private final HabilidadBlandaRepository habilidadBlandaRepository;
    private final UsuarioRepository usuarioRepository;

    /** Obtiene todas las habilidades blandas del usuario autenticado. */
    @Transactional(readOnly = true)
    public List<HabilidadBlandaResponse> getAll(UUID idUsuario) {
        return habilidadBlandaRepository.findByUsuario_IdUsuario(idUsuario)
                .stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    /** Agrega una nueva habilidad blanda al usuario. */
    @Transactional
    public HabilidadBlandaResponse create(UUID idUsuario, HabilidadBlandaRequest request) {
        Usuario usuario = usuarioRepository.findById(idUsuario)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Usuario no encontrado"));

        if (habilidadBlandaRepository.existsByUsuario_IdUsuarioAndNombreHabilidadIgnoreCase(
                idUsuario, request.getNombreHabilidad())) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Ya tienes esta habilidad blanda registrada");
        }

        HabilidadBlanda habilidad = HabilidadBlanda.builder()
                .usuario(usuario)
                .nombreHabilidad(request.getNombreHabilidad())
                .build();

        habilidadBlandaRepository.save(habilidad);
        log.info("Habilidad blanda creada: idUsuario={}, nombre={}", idUsuario, request.getNombreHabilidad());
        return toResponse(habilidad);
    }

    /** Elimina una habilidad blanda por su ID, verificando que pertenezca al usuario. */
    @Transactional
    public void delete(UUID idUsuario, Integer idHabilidadBlanda) {
        HabilidadBlanda habilidad = habilidadBlandaRepository.findById(idHabilidadBlanda)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Habilidad blanda no encontrada"));

        if (!habilidad.getUsuario().getIdUsuario().equals(idUsuario)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN,
                    "No tienes permiso para eliminar esta habilidad blanda");
        }

        habilidadBlandaRepository.delete(habilidad);
        log.info("Habilidad blanda eliminada: idHabilidadBlanda={}, idUsuario={}", idHabilidadBlanda, idUsuario);
    }

    private HabilidadBlandaResponse toResponse(HabilidadBlanda h) {
        return HabilidadBlandaResponse.builder()
                .id(h.getId())
                .nombreHabilidad(h.getNombreHabilidad())
                .build();
    }
}
