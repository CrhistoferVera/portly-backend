package com.portly.service;

import com.portly.domain.entity.EvidenciaProyecto;
import com.portly.domain.entity.Usuario;
import com.portly.domain.repository.EvidenciaProyectoRepository;
import com.portly.domain.repository.UsuarioRepository;
import com.portly.dto.EvidenciaProyectoResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class EvidenciaProyectoService {

    /** Tamaño máximo permitido: 10 MB en bytes */
    private static final long MAX_SIZE_BYTES = 10L * 1024 * 1024;

    /** Formatos de imagen aceptados (extensión en minúsculas) */
    private static final Set<String> FORMATOS_PERMITIDOS = Set.of("png", "jpg", "jpeg", "gif");

    private final EvidenciaProyectoRepository evidenciaRepository;
    private final UsuarioRepository           usuarioRepository;
    private final CloudinaryService           cloudinaryService;

    // ──────────────────────────────────────────────────────────────
    // POST /api/proyectos/evidencias  — Subir una imagen de evidencia
    // ──────────────────────────────────────────────────────────────
    @Transactional
    public EvidenciaProyectoResponse subirEvidencia(UUID idUsuario, MultipartFile file) {

        // 1. Validar que se envió un archivo
        if (file == null || file.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Debe proporcionar un archivo");
        }

        // 2. Validar tamaño (≤ 10 MB)
        if (file.getSize() > MAX_SIZE_BYTES) {
            throw new ResponseStatusException(
                    HttpStatus.UNPROCESSABLE_ENTITY,
                    "Tamaño superado (máx. 10MB)"
            );
        }

        // 3. Validar formato (PNG, JPG, JPEG, GIF)
        String extension = obtenerExtension(file.getOriginalFilename());
        if (!FORMATOS_PERMITIDOS.contains(extension)) {
            throw new ResponseStatusException(
                    HttpStatus.UNSUPPORTED_MEDIA_TYPE,
                    "Formato no permitido. Use PNG, JPG o GIF"
            );
        }

        // 4. Verificar que el usuario existe
        Usuario usuario = usuarioRepository.findById(idUsuario)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Usuario no encontrado"));

        // 5. Subir a Cloudinary y obtener metadatos
        Map<String, Object> meta;
        try {
            meta = cloudinaryService.uploadImageWithMetadata(file, "portly/evidencias");
        } catch (IOException e) {
            log.error("Error al subir evidencia a Cloudinary: {}", e.getMessage());
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Error al subir la imagen");
        }

        // Normalizar formato: JPEG → JPG para consistencia con el front
        String formato = "jpeg".equalsIgnoreCase((String) meta.get("format")) ? "JPG" : (String) meta.get("format");

        // 6. Persistir en la base de datos
        EvidenciaProyecto evidencia = EvidenciaProyecto.builder()
                .usuario(usuario)
                .nombreOriginal(file.getOriginalFilename() != null ? file.getOriginalFilename() : "archivo")
                .enlaceEvidencia((String) meta.get("url"))
                .enlaceMiniatura((String) meta.get("thumbnailUrl"))
                .formato(formato)
                .tamanoBytes((Long) meta.get("bytes"))
                .fechaSubida(LocalDateTime.now())
                .build();

        evidenciaRepository.save(evidencia);
        log.info("Evidencia subida: idUsuario={}, archivo={}, bytes={}", idUsuario, evidencia.getNombreOriginal(), evidencia.getTamanoBytes());

        return toDto(evidencia);
    }

    // ──────────────────────────────────────────────────────────────
    // GET /api/proyectos/evidencias  — Listar evidencias del usuario
    // ──────────────────────────────────────────────────────────────
    @Transactional(readOnly = true)
    public List<EvidenciaProyectoResponse> listarEvidencias(UUID idUsuario) {
        usuarioRepository.findById(idUsuario)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Usuario no encontrado"));

        return evidenciaRepository
                .findByUsuario_IdUsuarioOrderByFechaSubidaDesc(idUsuario)
                .stream()
                .map(this::toDto)
                .collect(Collectors.toList());
    }

    // ──────────────────────────────────────────────────────────────
    // DELETE /api/proyectos/evidencias/{id}  — Eliminar una evidencia
    // ──────────────────────────────────────────────────────────────
    @Transactional
    public void eliminarEvidencia(UUID idUsuario, Integer idEvidencia) {
        EvidenciaProyecto evidencia = evidenciaRepository.findById(idEvidencia)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Evidencia no encontrada"));

        // Verificar que la evidencia pertenece al usuario autenticado
        if (!evidencia.getUsuario().getIdUsuario().equals(idUsuario)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "No tienes permiso para eliminar esta evidencia");
        }

        evidenciaRepository.delete(evidencia);
        log.info("Evidencia eliminada: idEvidencia={}, idUsuario={}", idEvidencia, idUsuario);
    }

    // ──────────────────────────────────────────────────────────────
    // Helpers
    // ──────────────────────────────────────────────────────────────

    private EvidenciaProyectoResponse toDto(EvidenciaProyecto e) {
        return EvidenciaProyectoResponse.builder()
                .idEvidenciaProyecto(e.getIdEvidenciaProyecto())
                .nombreOriginal(e.getNombreOriginal())
                .enlaceEvidencia(e.getEnlaceEvidencia())
                .enlaceMiniatura(e.getEnlaceMiniatura())
                .formato(e.getFormato())
                .tamanoBytes(e.getTamanoBytes())
                .fechaSubida(e.getFechaSubida())
                .build();
    }

    /**
     * Extrae la extensión en minúsculas de un nombre de archivo.
     * Por ejemplo: "foto.PNG" → "png"
     */
    private String obtenerExtension(String filename) {
        if (filename == null || !filename.contains(".")) {
            return "";
        }
        return filename.substring(filename.lastIndexOf('.') + 1).toLowerCase();
    }
}
