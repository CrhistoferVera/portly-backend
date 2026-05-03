package com.portly.config;

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
import java.util.function.UnaryOperator;

@Slf4j
@Component
@RequiredArgsConstructor
public class DataInitializer implements ApplicationRunner {

    private static final String S_HERO       = "hero";
    private static final String S_SKILLS     = "skills";
    private static final String S_SOFTSKILLS = "softskills";
    private static final String S_EXPERIENCE = "experience";
    private static final String S_EDUCATION  = "education";
    private static final String S_PROJECTS   = "projects";
    private static final String S_ABOUT      = "about";
    private static final String S_CONTACT    = "contact";
    private static final String TAG_BRUTALISTA = "Brutalista";

    private final PlantillaRepository plantillaRepository;

    @Override
    public void run(ApplicationArguments args) {
        sembrarPlantillas();
    }

    private void sembrarPlantillas() {

        // ── Plantilla 1: Brutalist Architect ──────────────────────────────────
        String idBrutalist = "template-brutalist-architect";
        TemplateSchema schemaBrutalist = TemplateSchema.builder()
                .colorScheme("brutalist-dark")
                .fontFamily("Space Mono")
                .sections(List.of(
                        TemplateSection.builder().type(S_HERO)      .title("Presentación")          .visible(true).order(0).build(),
                        TemplateSection.builder().type(S_SKILLS)    .title("Stack Tecnológico")      .visible(true).order(1).build(),
                        TemplateSection.builder().type(S_SOFTSKILLS).title("Capacidades")            .visible(true).order(2).build(),
                        TemplateSection.builder().type(S_EXPERIENCE).title("Trayectoria")            .visible(true).order(3).build(),
                        TemplateSection.builder().type(S_EDUCATION) .title("Formación Académica")    .visible(true).order(4).build(),
                        TemplateSection.builder().type(S_PROJECTS)  .title("Trabajos Seleccionados") .visible(true).order(5).build(),
                        TemplateSection.builder().type(S_ABOUT)     .title("Filosofía")              .visible(true).order(6).build(),
                        TemplateSection.builder().type(S_CONTACT)   .title("Contacto")               .visible(true).order(7).build()
                ))
                .build();

        upsertPlantilla(idBrutalist, p -> {
            if (p == null) {
                return Plantilla.builder()
                        .idPlantilla(idBrutalist)
                        .nombre("Brutalist Architect")
                        .descripcion(
                                "Diseño brutalista de alto impacto con tipografía monoespaciada, bloques de color " +
                                "negro, blanco y amarillo intenso. Ideal para desarrolladores senior y arquitectos " +
                                "de software que quieren transmitir autoridad técnica y un estilo visual sin concesiones."
                        )
                        .etiquetas(List.of(TAG_BRUTALISTA, "Tech", "Desarrollador", "Dark", "Impactante"))
                        .imagenVistaPrevia("")
                        .urlVistaPrevia("")
                        .cantidadSecciones(8)
                        .impacto("3.4k")
                        .tiempoConfiguracion("3 min")
                        .esquemaConfiguracion(schemaBrutalist)
                        .build();
            }
            p.setEsquemaConfiguracion(schemaBrutalist);
            p.setCantidadSecciones(8);
            return p;
        });

        // ── Plantilla 2: The Architect v2 (Brutalist Light) ──────────────────
        String idArchitectV2 = "template-architect-v2";
        TemplateSchema schemaV2 = TemplateSchema.builder()
                .colorScheme(Map.of(
                        "background", "#FFFFFF",
                        "primary", "#FFCC00",
                        "secondary", "#FF3333",
                        "text", "#000000"
                ))
                .fontFamily("Space Mono")
                .sections(List.of(
                        TemplateSection.builder().type(S_HERO)      .title("Presentación")          .visible(true).order(0).build(),
                        TemplateSection.builder().type(S_SKILLS)    .title("Stack Tecnológico")      .visible(true).order(1).build(),
                        TemplateSection.builder().type(S_SOFTSKILLS).title("Capacidades")            .visible(true).order(2).build(),
                        TemplateSection.builder().type(S_EXPERIENCE).title("Trayectoria")            .visible(true).order(3).build(),
                        TemplateSection.builder().type(S_EDUCATION) .title("Formación Académica")    .visible(true).order(4).build(),
                        TemplateSection.builder().type(S_PROJECTS)  .title("Trabajos Seleccionados") .visible(true).order(5).build(),
                        TemplateSection.builder().type(S_ABOUT)     .title("Filosofía")              .visible(true).order(6).build(),
                        TemplateSection.builder().type(S_CONTACT)   .title("Contacto")               .visible(true).order(7).build()
                ))
                .build();

        upsertPlantilla(idArchitectV2, p -> {
            if (p == null) {
                return Plantilla.builder()
                        .idPlantilla(idArchitectV2)
                        .nombre("The Architect v2")
                        .descripcion(
                                "Evolución del diseño brutalista con fondo claro (blanco), texto en negro y " +
                                "bloques de impacto en rojo y amarillo. Estructura que prioriza la legibilidad " +
                                "manteniendo un estilo vanguardista y técnico."
                        )
                        .etiquetas(List.of(TAG_BRUTALISTA, "Light", "Tech", "Minimalista", "Alto Contraste"))
                        .imagenVistaPrevia("")
                        .urlVistaPrevia("")
                        .cantidadSecciones(8)
                        .impacto("4.8k")
                        .tiempoConfiguracion("3 min")
                        .esquemaConfiguracion(schemaV2)
                        .build();
            }
            p.setEsquemaConfiguracion(schemaV2);
            p.setCantidadSecciones(8);
            return p;
        });

        // ── Plantilla 3: Brutalist Space ─────────────────────────────────────
        String idTercera = "template-tercera-brutalist";
        TemplateSchema schemaTercera = TemplateSchema.builder()
                .colorScheme("brutalist")
                .fontFamily("Space Grotesk")
                .sections(List.of(
                        TemplateSection.builder().type(S_HERO)      .title("Work")             .visible(true).order(0).build(),
                        TemplateSection.builder().type(S_SKILLS)    .title("Capabilities")     .visible(true).order(1).build(),
                        TemplateSection.builder().type(S_SOFTSKILLS).title("Soft Skills")      .visible(true).order(2).build(),
                        TemplateSection.builder().type(S_EXPERIENCE).title("Experience")       .visible(true).order(3).build(),
                        TemplateSection.builder().type(S_EDUCATION) .title("Education")        .visible(true).order(4).build(),
                        TemplateSection.builder().type(S_PROJECTS)  .title("Selected Works")   .visible(true).order(5).build(),
                        TemplateSection.builder().type(S_CONTACT)   .title("Contact")          .visible(true).order(6).build()
                ))
                .build();

        upsertPlantilla(idTercera, p -> {
            if (p == null) {
                return Plantilla.builder()
                        .idPlantilla(idTercera)
                        .nombre("Brutalist Space")
                        .descripcion(
                                "Diseño brutalista moderno utilizando la tipografía Space Grotesk. " +
                                "Ideal para portafolios enfocados en la experiencia y trabajos seleccionados."
                        )
                        .etiquetas(List.of(TAG_BRUTALISTA, "Moderno", "Grotesk", "Portafolio"))
                        .imagenVistaPrevia("")
                        .urlVistaPrevia("")
                        .cantidadSecciones(7)
                        .impacto("2.1k")
                        .tiempoConfiguracion("4 min")
                        .esquemaConfiguracion(schemaTercera)
                        .build();
            }
            p.setEsquemaConfiguracion(schemaTercera);
            p.setCantidadSecciones(7);
            return p;
        });
    }

    private void upsertPlantilla(String id, UnaryOperator<Plantilla> mapper) {
        Plantilla existing = plantillaRepository.findById(id).orElse(null);
        Plantilla plantilla = mapper.apply(existing);
        plantillaRepository.save(plantilla);
        log.info("{} plantilla '{}'.", existing == null ? "Insertada" : "Actualizada", id);
    }
}
