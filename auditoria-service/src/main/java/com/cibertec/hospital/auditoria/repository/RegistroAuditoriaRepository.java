package com.cibertec.hospital.auditoria.repository;

import com.cibertec.hospital.auditoria.model.RegistroAuditoria;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface RegistroAuditoriaRepository extends ReactiveCrudRepository<RegistroAuditoria, Long> {
    Mono<Boolean> existsByEventoId(String eventoId);

    Flux<RegistroAuditoria> findByRecursoAndRecursoIdOrderByFechaEventoAsc(String recurso, Long recursoId);
}
