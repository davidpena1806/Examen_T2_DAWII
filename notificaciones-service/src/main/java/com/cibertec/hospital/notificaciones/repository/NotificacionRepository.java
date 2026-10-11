package com.cibertec.hospital.notificaciones.repository;

import com.cibertec.hospital.notificaciones.model.Notificacion;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface NotificacionRepository extends ReactiveCrudRepository<Notificacion, Long> {
    Mono<Boolean> existsByEventoId(String eventoId);

    Flux<Notificacion> findByPacienteId(Long pacienteId);
}
