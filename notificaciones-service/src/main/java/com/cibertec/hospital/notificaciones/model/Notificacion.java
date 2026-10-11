package com.cibertec.hospital.notificaciones.model;

import java.time.LocalDateTime;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Table;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table("notificacion")
public class Notificacion {
    @Id
    private Long id;
    private String eventoId;
    private String tipo;
    private Long pacienteId;
    private Long citaId;
    private String mensaje;
    private String canal;
    private LocalDateTime fechaEnvio;
}
