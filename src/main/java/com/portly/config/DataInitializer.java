package com.portly.config;

import com.portly.domain.entity.Plantilla;
import com.portly.domain.entity.TemplateSchema;
import com.portly.domain.entity.TemplateSection;
import com.portly.domain.repository.PlantillaRepository;
import com.portly.domain.repository.PortafolioRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;

import java.util.List;
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
    private final PortafolioRepository portafolioRepository;

    @Override
    public void run(ApplicationArguments args) {
        sembrarPlantillas();
    }

    private void sembrarPlantillas() {
        eliminarPlantillasObsoletas();

        // ── Plantilla 1: Brutalist Space ──────────────────────────────────────
        String idTercera = "template-tercera-brutalist";
        TemplateSchema schemaTercera = TemplateSchema.builder()
                .colorScheme("brutalist")
                .fontFamily("Space Grotesk")
                .sections(List.of(
                        TemplateSection.builder().type(S_HERO)      .title("Presentación")       .visible(true).order(0).build(),
                        TemplateSection.builder().type(S_SKILLS)    .title("Capacidades")        .visible(true).order(1).build(),
                        TemplateSection.builder().type(S_SOFTSKILLS).title("Habilidades")        .visible(true).order(2).build(),
                        TemplateSection.builder().type(S_EXPERIENCE).title("Experiencia")        .visible(true).order(3).build(),
                        TemplateSection.builder().type(S_EDUCATION) .title("Formación")          .visible(true).order(4).build(),
                        TemplateSection.builder().type(S_PROJECTS)  .title("Proyectos")          .visible(true).order(5).build(),
                        TemplateSection.builder().type(S_CONTACT)   .title("Contacto")           .visible(true).order(6).build()
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

        // ── Plantilla 4: Azure Professional ──────────────────────────────────
        String idCorporate = "template-corporate-blue";
        TemplateSchema schemaCorporate = TemplateSchema.builder()
                .colorScheme("corporate")
                .fontFamily("Plus Jakarta Sans")
                .sections(List.of(
                        TemplateSection.builder().type(S_HERO)      .title("Presentación")         .visible(true).order(0).build(),
                        TemplateSection.builder().type(S_SKILLS)    .title("Habilidades técnicas")  .visible(true).order(1).build(),
                        TemplateSection.builder().type(S_SOFTSKILLS).title("Habilidades blandas")   .visible(true).order(2).build(),
                        TemplateSection.builder().type(S_EXPERIENCE).title("Experiencia")           .visible(true).order(3).build(),
                        TemplateSection.builder().type(S_EDUCATION) .title("Formación académica")   .visible(true).order(4).build(),
                        TemplateSection.builder().type(S_PROJECTS)  .title("Proyectos")             .visible(true).order(5).build(),
                        TemplateSection.builder().type(S_ABOUT)     .title("Sobre mí")              .visible(true).order(6).build(),
                        TemplateSection.builder().type(S_CONTACT)   .title("Contacto")              .visible(true).order(7).build()
                ))
                .build();

        upsertPlantilla(idCorporate, p -> {
            if (p == null) {
                return Plantilla.builder()
                        .idPlantilla(idCorporate)
                        .nombre("Azure Professional")
                        .descripcion(
                                "Plantilla profesional en tonos blancos y azules. Diseño limpio y corporativo " +
                                "con tipografía Plus Jakarta Sans, ideal para perfiles de negocios, consultores " +
                                "y profesionales que buscan transmitir confianza y modernidad."
                        )
                        .etiquetas(List.of("Profesional", "Corporativo", "Azul", "Limpio", "Moderno"))
                        .imagenVistaPrevia("")
                        .urlVistaPrevia("")
                        .cantidadSecciones(8)
                        .impacto("1.2k")
                        .tiempoConfiguracion("3 min")
                        .esquemaConfiguracion(schemaCorporate)
                        .build();
            }
            p.setEsquemaConfiguracion(schemaCorporate);
            p.setCantidadSecciones(8);
            return p;
        });
    }

    private void eliminarPlantillasObsoletas() {
        Plantilla fallback = plantillaRepository.findById("template-tercera-brutalist").orElse(null);

        // Eliminar por ID conocido
        List<String> obsoletosById = List.of("template-brutalist-architect", "template-architect-v2");
        for (String id : obsoletosById) {
            eliminarPlantilla(id, fallback);
        }

        // Eliminar por nombre (plantillas sin ID conocido en el código)
        List<String> obsoletasPorNombre = List.of("Corporate Teal");
        for (String nombre : obsoletasPorNombre) {
            plantillaRepository.findByNombre(nombre).ifPresent(p -> eliminarPlantilla(p.getIdPlantilla(), fallback));
        }
    }

    private void eliminarPlantilla(String id, Plantilla fallback) {
        if (!plantillaRepository.existsById(id)) return;
        if (fallback != null) {
            var afectados = portafolioRepository.findByPlantilla_IdPlantilla(id);
            afectados.forEach(p -> p.setPlantilla(fallback));
            portafolioRepository.saveAll(afectados);
            if (!afectados.isEmpty()) {
                log.info("Reasignados {} portafolios de '{}' a '{}'.", afectados.size(), id, fallback.getIdPlantilla());
            }
        }
        plantillaRepository.deleteById(id);
        log.info("Eliminada plantilla obsoleta '{}'.", id);
    }

    private void upsertPlantilla(String id, UnaryOperator<Plantilla> mapper) {
        Plantilla existing = plantillaRepository.findById(id).orElse(null);
        Plantilla plantilla = mapper.apply(existing);
        plantillaRepository.save(plantilla);
        log.info("{} plantilla '{}'.", existing == null ? "Insertada" : "Actualizada", id);
    }
}
