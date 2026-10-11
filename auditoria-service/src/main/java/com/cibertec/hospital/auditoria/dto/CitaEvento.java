package com.cibertec.hospital.auditoria.dto;

import java.time.LocalDateTime;

public record CitaEvento(
        String eventoId,
        String tipo,
        Long citaId,
        String codigoCita,
        Long pacienteId,
        String estadoAnterior,
        String estadoNuevo,
        LocalDateTime fechaEvento,
        String origen) {
}
