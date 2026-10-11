package com.cibertec.hospital.pacientes.dto;

import com.cibertec.hospital.pacientes.model.EstadoPaciente;
import jakarta.validation.constraints.NotNull;

public record CambioEstadoRequest(@NotNull EstadoPaciente estado) {
}
