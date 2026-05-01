package com.portly.controller;

import com.portly.domain.entity.*;
import com.portly.domain.repository.*;
import com.portly.dto.PublicProfesionalResponse;
import com.portly.service.DocumentoProyectoService;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/public")
@RequiredArgsConstructor
public class PublicProfileController {

    private final PerfilUsuarioRepository      perfilRepository;
    private final RedesSocialesRepository      redesRepository;
    private final HabilidadTecnicaRepository   habilidadTecnicaRepository;
    private final HabilidadBlandaRepository    habilidadBlandaRepository;
    private final ExperienciaLaboralRepository experienciaRepository;
    private final FormacionAcademicaRepository formacionRepository;
    private final DocumentoProyectoService     documentoService;

    @GetMapping("/profesionales")
    public ResponseEntity<List<PublicProfesionalResponse>> listarProfesionales() {
        List<PublicProfesionalResponse> result = perfilRepository.findAll().stream()
                .filter(p -> p.getUsuario() != null)
                .map(this::buildResponse)
                .collect(Collectors.toList());
        return ResponseEntity.ok(result);
    }

    @GetMapping("/profesionales/{id}")
    public ResponseEntity<PublicProfesionalResponse> getProfesional(@PathVariable UUID id) {
        PerfilUsuario perfil = perfilRepository.findByUsuario_IdUsuario(id)
                .orElseThrow(() -> new RuntimeException("Profesional no encontrado"));
        return ResponseEntity.ok(buildResponse(perfil));
    }

    private PublicProfesionalResponse buildResponse(PerfilUsuario perfil) {
        UUID idUsuario = perfil.getUsuario().getIdUsuario();
        Usuario u = perfil.getUsuario();

        boolean mostrarCorreo   = bool(perfil.getMostrarCorreo());
        boolean mostrarProfesion = bool(perfil.getMostrarProfesion());
        boolean mostrarBio      = bool(perfil.getMostrarBiografia());
        boolean mostrarInsta    = bool(perfil.getMostrarInstagram());
        boolean mostrarFb       = bool(perfil.getMostrarFacebook());
        boolean mostrarYt       = bool(perfil.getMostrarYoutube());
        boolean mostrarTecnicas = bool(perfil.getMostrarHabilidadesTecnicas());
        boolean mostrarBlandas  = bool(perfil.getMostrarHabilidadesBlandas());
        boolean mostrarTray     = bool(perfil.getMostrarTrayectoria());
        boolean mostrarForm     = bool(perfil.getMostrarFormacion());

        // Social links
        String instagram = null, facebook = null, youtube = null;
        List<RedesSociales> redes = redesRepository.findByPerfilUsuario_Usuario_Email(u.getEmail());
        for (RedesSociales red : redes) {
            switch (red.getNombre().toLowerCase()) {
                case "instagram": if (mostrarInsta) instagram = red.getEnlace(); break;
                case "facebook":  if (mostrarFb)    facebook  = red.getEnlace(); break;
                case "youtube":   if (mostrarYt)    youtube   = red.getEnlace(); break;
            }
        }

        // Technical skills
        List<PublicProfesionalResponse.SkillDto> tecnicas = null;
        if (mostrarTecnicas) {
            tecnicas = habilidadTecnicaRepository.findByUsuario_IdUsuario(idUsuario).stream()
                    .map(h -> PublicProfesionalResponse.SkillDto.builder()
                            .nombre(h.getNombre()).nivel(h.getNivel()).build())
                    .collect(Collectors.toList());
        }

        // Soft skills
        List<String> blandas = null;
        if (mostrarBlandas) {
            blandas = habilidadBlandaRepository.findByUsuario_IdUsuario(idUsuario).stream()
                    .map(HabilidadBlanda::getNombreHabilidad)
                    .collect(Collectors.toList());
        }

        // Work experience
        List<PublicProfesionalResponse.ExperienciaDto> trayectoria = null;
        if (mostrarTray) {
            trayectoria = experienciaRepository.findByUsuario_IdUsuario(idUsuario).stream()
                    .map(e -> PublicProfesionalResponse.ExperienciaDto.builder()
                            .empresa(e.getEmpresa())
                            .cargo(e.getCargo())
                            .fechaInicio(e.getFechaInicio() != null ? e.getFechaInicio().toString() : null)
                            .fechaFin(e.getFechaFin() != null ? e.getFechaFin().toString() : null)
                            .descripcion(e.getDescripcion())
                            .esEmpleoActual(e.getEsEmpleoActual())
                            .build())
                    .collect(Collectors.toList());
        }

        // Academic formation
        List<PublicProfesionalResponse.FormacionDto> formacion = null;
        if (mostrarForm) {
            formacion = formacionRepository.findByUsuario_IdUsuario(idUsuario).stream()
                    .map(f -> PublicProfesionalResponse.FormacionDto.builder()
                            .institucion(f.getInstitucion())
                            .carrera(f.getCarrera())
                            .nivel(f.getNivel())
                            .fechaInicio(f.getFechaInicio() != null ? f.getFechaInicio().toString() : null)
                            .fechaFinalizacion(f.getFechaFinalizacion() != null ? f.getFechaFinalizacion().toString() : null)
                            .actualmenteEstudiando(f.getActualmenteEstudiando())
                            .build())
                    .collect(Collectors.toList());
        }

        return PublicProfesionalResponse.builder()
                .idUsuario(u.getIdUsuario())
                .nombre(perfil.getNombre())
                .apellido(perfil.getApellido())
                .email(mostrarCorreo   ? u.getEmail()                     : null)
                .titularProfesional(mostrarProfesion ? perfil.getTitularProfesional() : null)
                .acercaDeMi(mostrarBio ? perfil.getAcercaDeMi()           : null)
                .enlaceFoto(perfil.getEnlaceFoto())
                .instagram(instagram)
                .facebook(facebook)
                .youtube(youtube)
                .habilidadesTecnicas(tecnicas)
                .habilidadesBlandas(blandas)
                .trayectoria(trayectoria)
                .formacion(formacion)
                .build();
    }

    private boolean bool(Boolean value) {
        return value == null || value;
    }

    @GetMapping("/documentos/{id}/descargar")
    public ResponseEntity<Resource> descargarDocumentoPublico(@PathVariable Integer id) {
        Resource resource = documentoService.cargarDocumentoComoRecurso(id);
        DocumentoProyecto documento = documentoService.obtenerDocumentoEntity(id);

        String contentType = "application/octet-stream";
        if (documento.getFormato().equalsIgnoreCase("pdf")) {
            contentType = "application/pdf";
        } else if (documento.getFormato().equalsIgnoreCase("doc")) {
            contentType = "application/msword";
        } else if (documento.getFormato().equalsIgnoreCase("docx")) {
            contentType = "application/vnd.openxmlformats-officedocument.wordprocessingml.document";
        }

        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType(contentType))
                .header(HttpHeaders.CONTENT_DISPOSITION, "inline; filename=\"" + documento.getNombreOriginal() + "\"")
                .body(resource);
    }
}
