package com.cibertec.hospital.citas.dto;

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
