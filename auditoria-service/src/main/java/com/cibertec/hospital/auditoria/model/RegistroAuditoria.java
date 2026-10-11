package com.cibertec.hospital.auditoria.model;

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
@Table("registro_auditoria")
public class RegistroAuditoria {
    @Id
    private Long id;
    private String eventoId;
    private String tipoEvento;
    private String recurso;
    private Long recursoId;
    private String estadoAnterior;
    private String estadoNuevo;
    private String origen;
    private LocalDateTime fechaEvento;
    private LocalDateTime fechaRegistro;
}
