package com.portly.service;

import com.portly.domain.entity.Plantilla;
import com.portly.domain.repository.PlantillaRepository;
import com.portly.dto.PlantillaResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class PlantillaService {

    private final PlantillaRepository plantillaRepository;

    /** Obtiene todas las plantillas disponibles en el sistema. */
    @Transactional(readOnly = true)
    public List<PlantillaResponse> getAll() {
        return plantillaRepository.findAll()
                .stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    private PlantillaResponse toResponse(Plantilla p) {
        return PlantillaResponse.builder()
                .id(p.getIdPlantilla())
                .nombre(p.getNombre())
                .descripcion(p.getDescripcion())
                .tags(p.getEtiquetas())
                .previewImageUrl(p.getImagenVistaPrevia())
                .previewUrl(p.getUrlVistaPrevia())
                .stats(PlantillaResponse.PlantillaStatsResponse.builder()
                        .secciones(p.getCantidadSecciones() != null ? p.getCantidadSecciones() : 0)
                        .impacto(p.getImpacto())
                        .tiempoConfiguracion(p.getTiempoConfiguracion())
                        .build())
                .schema(p.getEsquemaConfiguracion())
                .build();
    }
}
