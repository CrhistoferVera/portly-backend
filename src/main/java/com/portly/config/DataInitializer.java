package com.portly.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.portly.domain.entity.Plantilla;
import com.portly.domain.entity.TemplateSchema;
import com.portly.domain.entity.TemplateSection;
import com.portly.domain.repository.PlantillaRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;

/**
 * Inicializador de datos que se ejecuta al arrancar la aplicación.
 * Inserta las plantillas por defecto si aún no existen en la base de datos.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class DataInitializer implements ApplicationRunner {

    private final PlantillaRepository plantillaRepository;

    @Override
    public void run(ApplicationArguments args) {
        sembrarPlantillas();
    }

    private void sembrarPlantillas() {

        // ── Plantilla 1: Brutalist Architect ──────────────────────────────────
        String idBrutalist = "template-brutalist-architect";
        if (!plantillaRepository.existsById(idBrutalist)) {

            TemplateSchema schema = TemplateSchema.builder()
                    .colorScheme("brutalist-dark")
                    .fontFamily("Space Mono")
                    .sections(List.of(
                            TemplateSection.builder().type("hero")       .title("Presentación")       .visible(true).order(0).build(),
                            TemplateSection.builder().type("projects")   .title("Trabajos Seleccionados").visible(true).order(1).build(),
                            TemplateSection.builder().type("skills")     .title("Stack Tecnológico")   .visible(true).order(2).build(),
                            TemplateSection.builder().type("softskills") .title("Capacidades")         .visible(true).order(3).build(),
                            TemplateSection.builder().type("about")      .title("Filosofía")           .visible(true).order(4).build(),
                            TemplateSection.builder().type("contact")    .title("Contacto")            .visible(true).order(5).build()
                    ))
                    .build();

            Plantilla brutalist = Plantilla.builder()
                    .idPlantilla(idBrutalist)
                    .nombre("Brutalist Architect")
                    .descripcion(
                            "Diseño brutalista de alto impacto con tipografía monoespaciada, bloques de color " +
                            "negro, blanco y amarillo intenso. Ideal para desarrolladores senior y arquitectos " +
                            "de software que quieren transmitir autoridad técnica y un estilo visual sin concesiones."
                    )
                    .etiquetas(List.of("Brutalista", "Tech", "Desarrollador", "Dark", "Impactante"))
                    .imagenVistaPrevia("")   // ← Se actualizará con la URL de Cloudinary
                    .urlVistaPrevia("")
                    .cantidadSecciones(6)
                    .impacto("3.4k")
                    .tiempoConfiguracion("3 min")
                    .esquemaConfiguracion(schema)
                    .build();

            plantillaRepository.save(brutalist);
            log.info("✅ Plantilla '{}' insertada correctamente.", brutalist.getNombre());
        } else {
            log.info("ℹ️  Plantilla '{}' ya existe, se omite la inserción.", idBrutalist);
        }

        // ── Plantilla 2: The Architect v2 (Brutalist Light) ──────────────────────────────────
        String idArchitectV2 = "template-architect-v2";
        if (!plantillaRepository.existsById(idArchitectV2)) {

            TemplateSchema schemaV2 = TemplateSchema.builder()
                    .colorScheme(Map.of(
                            "background", "#FFFFFF",
                            "primary", "#FFCC00",   // Amarillo
                            "secondary", "#FF3333", // Rojo
                            "text", "#000000"       // Negro
                    ))
                    .fontFamily("Space Mono")
                    .sections(List.of(
                            TemplateSection.builder().type("hero")       .title("Presentación")       .visible(true).order(0).build(),
                            TemplateSection.builder().type("projects")   .title("Trabajos Seleccionados").visible(true).order(1).build(),
                            TemplateSection.builder().type("skills")     .title("Stack Tecnológico")   .visible(true).order(2).build(),
                            TemplateSection.builder().type("softskills") .title("Capacidades")         .visible(true).order(3).build(),
                            TemplateSection.builder().type("about")      .title("Filosofía")           .visible(true).order(4).build(),
                            TemplateSection.builder().type("contact")    .title("Contacto")            .visible(true).order(5).build()
                    ))
                    .build();

            Plantilla architectV2 = Plantilla.builder()
                    .idPlantilla(idArchitectV2)
                    .nombre("The Architect v2")
                    .descripcion(
                            "Evolución del diseño brutalista con fondo claro (blanco), texto en negro y " +
                            "bloques de impacto en rojo y amarillo. Estructura que prioriza la legibilidad " +
                            "manteniendo un estilo vanguardista y técnico."
                    )
                    .etiquetas(List.of("Brutalista", "Light", "Tech", "Minimalista", "Alto Contraste"))
                    .imagenVistaPrevia("")   // ← Se actualizará con la URL de Cloudinary
                    .urlVistaPrevia("")
                    .cantidadSecciones(6)
                    .impacto("4.8k")
                    .tiempoConfiguracion("3 min")
                    .esquemaConfiguracion(schemaV2)
                    .build();

            plantillaRepository.save(architectV2);
            log.info("✅ Plantilla '{}' insertada correctamente.", architectV2.getNombre());
        } else {
            log.info("ℹ️  Plantilla '{}' ya existe, se omite la inserción.", idArchitectV2);
        }

        // ── Plantilla 3: Tercera Plantilla (Brutalist Space Grotesk) ──────────────────────────
        String idTercera = "template-tercera-brutalist";
        if (!plantillaRepository.existsById(idTercera)) {

            TemplateSchema schemaTercera = TemplateSchema.builder()
                    .colorScheme("brutalist")
                    .fontFamily("Space Grotesk")
                    .sections(List.of(
                            TemplateSection.builder().type("hero")       .title("Work")           .visible(true).order(0).build(),
                            TemplateSection.builder().type("projects")   .title("Selected Works") .visible(true).order(1).build(),
                            TemplateSection.builder().type("skills")     .title("Capabilities")   .visible(true).order(2).build(),
                            TemplateSection.builder().type("experience") .title("Experience")     .visible(true).order(3).build(),
                            TemplateSection.builder().type("contact")    .title("Contact")        .visible(true).order(4).build()
                    ))
                    .build();

            Plantilla tercera = Plantilla.builder()
                    .idPlantilla(idTercera)
                    .nombre("Brutalist Space")
                    .descripcion(
                            "Diseño brutalista moderno utilizando la tipografía Space Grotesk. " +
                            "Ideal para portafolios enfocados en la experiencia y trabajos seleccionados."
                    )
                    .etiquetas(List.of("Brutalista", "Moderno", "Grotesk", "Portafolio"))
                    .imagenVistaPrevia("")
                    .urlVistaPrevia("")
                    .cantidadSecciones(5)
                    .impacto("2.1k")
                    .tiempoConfiguracion("4 min")
                    .esquemaConfiguracion(schemaTercera)
                    .build();

            plantillaRepository.save(tercera);
            log.info("✅ Plantilla '{}' insertada correctamente.", tercera.getNombre());
        } else {
            log.info("ℹ️  Plantilla '{}' ya existe, se omite la inserción.", idTercera);
        }
    }
}
