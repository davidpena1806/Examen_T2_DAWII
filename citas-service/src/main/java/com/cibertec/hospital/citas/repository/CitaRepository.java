package com.cibertec.hospital.citas.repository;

import java.time.LocalDateTime;

import com.cibertec.hospital.citas.model.Cita;
import com.cibertec.hospital.citas.model.EstadoCita;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface CitaRepository extends ReactiveCrudRepository<Cita, Long> {

    Flux<Cita> findByPacienteId(Long pacienteId);

    /** Agenda: mismo profesional y misma fecha/hora con una cita que no este cancelada. */
    Mono<Boolean> existsByProfesionalCodigoAndFechaHoraAndEstadoNot(String profesionalCodigo,
                                                                    LocalDateTime fechaHora,
                                                                    EstadoCita estado);
}
