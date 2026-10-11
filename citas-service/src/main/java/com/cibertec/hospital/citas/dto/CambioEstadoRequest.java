package com.cibertec.hospital.citas.dto;

import com.cibertec.hospital.citas.model.EstadoCita;
import jakarta.validation.constraints.NotNull;

public record CambioEstadoRequest(@NotNull EstadoCita estado) {
}
