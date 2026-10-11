package com.cibertec.hospital.pacientes.repository;

import com.cibertec.hospital.pacientes.model.Paciente;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import reactor.core.publisher.Mono;

public interface PacienteRepository extends ReactiveCrudRepository<Paciente, Long> {
    Mono<Paciente> findByTipoDocumentoAndNumeroDocumento(String tipoDocumento, String numeroDocumento);
}
