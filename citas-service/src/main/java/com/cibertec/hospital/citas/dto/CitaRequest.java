package com.cibertec.hospital.citas.dto;

import java.time.LocalDateTime;

import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record CitaRequest(
        @NotNull Long pacienteId,
        @NotBlank @Size(max = 80) String especialidad,
        @NotNull @Future LocalDateTime fechaHora,
        @NotBlank @Size(max = 40) String profesionalCodigo,
        @NotBlank @Size(max = 200) String motivo) {
}
