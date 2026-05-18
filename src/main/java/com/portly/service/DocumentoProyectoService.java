package com.portly.service;

import com.portly.domain.entity.DocumentoProyecto;
import com.portly.domain.entity.Usuario;
import com.portly.domain.repository.DocumentoProyectoRepository;
import com.portly.domain.repository.UsuarioRepository;
import com.portly.dto.DocumentoProyectoResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

import java.io.IOException;
import java.net.MalformedURLException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class DocumentoProyectoService {

    private static final long MAX_SIZE_BYTES = 10L * 1024 * 1024; // 10MB
    private static final Set<String> FORMATOS_PERMITIDOS = Set.of("pdf", "doc", "docx");
    private static final String DOCUMENTO_NO_ENCONTRADO = "Documento no encontrado";

    private final DocumentoProyectoRepository documentoRepository;
    private final UsuarioRepository usuarioRepository;
    private final CloudinaryService cloudinaryService;

    @Transactional
    public DocumentoProyectoResponse subirDocumento(UUID idUsuario, MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Debe proporcionar un archivo");
        }

        if (file.getSize() > MAX_SIZE_BYTES) {
            throw new ResponseStatusException(HttpStatus.UNPROCESSABLE_ENTITY, "Tamaño superado (máx. 10MB)");
        }

        String extension = obtenerExtension(file.getOriginalFilename());
        if (!FORMATOS_PERMITIDOS.contains(extension)) {
            throw new ResponseStatusException(HttpStatus.UNSUPPORTED_MEDIA_TYPE, "Formato no permitido. Use PDF, DOC o DOCX");
        }

        Usuario usuario = usuarioRepository.findById(idUsuario)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Usuario no encontrado"));

        String cloudinaryUrl;
        try {
            cloudinaryUrl = cloudinaryService.uploadRawFile(file, "portly/documentos");
            if (cloudinaryUrl == null) {
                throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Error al guardar el archivo");
            }
        } catch (IOException e) {
            log.error("Error al subir documento a Cloudinary: {}", e.getMessage(), e);
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Error al guardar el archivo");
        }

        DocumentoProyecto documento = DocumentoProyecto.builder()
                .usuario(usuario)
                .nombreOriginal(file.getOriginalFilename() != null ? file.getOriginalFilename() : "documento")
                .rutaLocal(cloudinaryUrl)
                .formato(extension)
                .tamanoBytes(file.getSize())
                .fechaSubida(LocalDateTime.now())
                .build();

        documentoRepository.save(documento);
        log.info("Documento subido: idUsuario={}, archivo={}, bytes={}", idUsuario, documento.getNombreOriginal(), documento.getTamanoBytes());

        return toDto(documento);
    }

    @Transactional(readOnly = true)
    public List<DocumentoProyectoResponse> listarDocumentos(UUID idUsuario) {
        usuarioRepository.findById(idUsuario)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Usuario no encontrado"));

        return documentoRepository.findByUsuario_IdUsuarioOrderByFechaSubidaDesc(idUsuario)
                .stream()
                .map(this::toDto)
                .toList();
    }

    @Transactional
    public void eliminarDocumento(UUID idUsuario, Integer idDocumento) {
        DocumentoProyecto documento = documentoRepository.findById(idDocumento)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, DOCUMENTO_NO_ENCONTRADO));

        if (!documento.getUsuario().getIdUsuario().equals(idUsuario)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "No tienes permiso para eliminar este documento");
        }

        String ruta = documento.getRutaLocal();
        if (ruta != null && !ruta.startsWith("http")) {
            try {
                Files.deleteIfExists(Paths.get(ruta));
            } catch (IOException e) {
                log.error("Error al eliminar el archivo físico: {}", e.getMessage());
            }
        }

        documentoRepository.delete(documento);
        log.info("Documento eliminado: idDocumento={}, idUsuario={}", idDocumento, idUsuario);
    }

    public Resource cargarDocumentoComoRecurso(Integer idDocumento) {
        DocumentoProyecto documento = documentoRepository.findById(idDocumento)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, DOCUMENTO_NO_ENCONTRADO));

        try {
            Resource resource;
            String ruta = documento.getRutaLocal();
            if (ruta != null && ruta.startsWith("http")) {
                resource = new UrlResource(ruta);
            } else {
                resource = new UrlResource(Paths.get(ruta).toUri());
            }

            if (resource.exists() && resource.isReadable()) {
                return resource;
            } else {
                throw new ResponseStatusException(HttpStatus.NOT_FOUND, "No se pudo leer el archivo");
            }
        } catch (MalformedURLException e) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Error en la ruta del archivo", e);
        }
    }

    public DocumentoProyecto obtenerDocumentoEntity(Integer idDocumento) {
        return documentoRepository.findById(idDocumento)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, DOCUMENTO_NO_ENCONTRADO));
    }

    private DocumentoProyectoResponse toDto(DocumentoProyecto d) {
        String urlDescarga = d.getRutaLocal() != null && d.getRutaLocal().startsWith("http")
                ? d.getRutaLocal()
                : "/api/public/documentos/" + d.getIdDocumentoProyecto() + "/descargar";
        return DocumentoProyectoResponse.builder()
                .idDocumentoProyecto(d.getIdDocumentoProyecto())
                .nombreOriginal(d.getNombreOriginal())
                .urlDescarga(urlDescarga)
                .formato(d.getFormato())
                .tamanoBytes(d.getTamanoBytes())
                .fechaSubida(d.getFechaSubida())
                .build();
    }

    private String obtenerExtension(String filename) {
        if (filename == null || !filename.contains(".")) {
            return "";
        }
        return filename.substring(filename.lastIndexOf('.') + 1).toLowerCase();
    }
}
