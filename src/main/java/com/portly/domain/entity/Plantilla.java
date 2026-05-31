package com.portly.domain.entity;

import com.portly.util.StringListConverter;
import com.portly.util.TemplateSchemaConverter;
import jakarta.persistence.*;
import lombok.*;

import java.util.List;

/**
 * Entidad que representa una plantilla de portafolio.
 * Contiene la configuración visual (esquema de colores, tipografía, secciones)
 * que se aplica cuando un usuario crea un portafolio con esta plantilla.
 */
@Entity
@Table(name = "plantilla")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
@ToString
public class Plantilla {

    @Id
    @Column(name = "id_plantilla", nullable = false, length = 100)
    @EqualsAndHashCode.Include
    private String idPlantilla;

    @Column(name = "nombre", nullable = false, length = 150)
    private String nombre;

    @Column(name = "descripcion", columnDefinition = "TEXT")
    private String descripcion;

    @Convert(converter = StringListConverter.class)
    @Column(name = "etiquetas", columnDefinition = "TEXT")
    private List<String> etiquetas;

    @Column(name = "imagen_vista_previa", length = 500)
    private String imagenVistaPrevia;

    @Column(name = "url_vista_previa", length = 500)
    private String urlVistaPrevia;

    @Column(name = "cantidad_secciones")
    private Integer cantidadSecciones;

    @Column(name = "impacto", length = 20)
    private String impacto;

    @Column(name = "tiempo_configuracion", length = 20)
    private String tiempoConfiguracion;

    @Convert(converter = TemplateSchemaConverter.class)
    @Column(name = "esquema_configuracion", columnDefinition = "TEXT", nullable = false)
    private TemplateSchema esquemaConfiguracion;

    @Builder.Default
    @Column(name = "estado", length = 20)
    private String estado = "ACTIVA";
}
