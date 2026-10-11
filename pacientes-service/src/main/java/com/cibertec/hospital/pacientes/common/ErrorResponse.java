package com.cibertec.hospital.pacientes.common;

import java.time.Instant;
import java.util.List;

public record ErrorResponse(Instant timestamp, int status, String error, String mensaje, List<String> detalles) {
}
