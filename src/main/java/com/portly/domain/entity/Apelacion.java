package com.portly.domain.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "apelacion")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
@ToString(exclude = {"usuario"})
public class Apelacion {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    @EqualsAndHashCode.Include
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false,
                foreignKey = @ForeignKey(name = "fk_apelacion_usuario"))
    private Usuario usuario;

    @Column(name = "motivo", nullable = false, columnDefinition = "TEXT")
    private String motivo;

    @Column(name = "tipo_estado", nullable = false, length = 20)
    private String tipoEstado; // 'suspendido' o 'restringido'

    @Column(name = "estado_apelacion", nullable = false, length = 20)
    private String estadoApelacion; // 'pendiente', 'aprobada', 'rechazada'

    @Builder.Default
    @Column(name = "fecha_apelacion", nullable = false)
    private LocalDateTime fechaApelacion = LocalDateTime.now();

    @Column(name = "fecha_resolucion")
    private LocalDateTime fechaResolucion;

    @Column(name = "admin_id", length = 50)
    private String adminId;
}
