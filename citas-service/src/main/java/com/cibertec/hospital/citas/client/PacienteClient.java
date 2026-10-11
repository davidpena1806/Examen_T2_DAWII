package com.cibertec.hospital.citas.client;

import com.cibertec.hospital.citas.dto.PacienteInfo;
import reactor.core.publisher.Mono;

public interface PacienteClient {
    Mono<PacienteInfo> obtenerPaciente(Long id);
}
