package com.cibertec.hospital.auditoria.service;

import java.time.LocalDateTime;

import com.cibertec.hospital.auditoria.dto.CitaEvento;
import com.cibertec.hospital.auditoria.model.RegistroAuditoria;
import com.cibertec.hospital.auditoria.repository.RegistroAuditoriaRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Slf4j
@Service
@RequiredArgsConstructor
public class AuditoriaService {

    private final RegistroAuditoriaRepository repository;

    public Mono<Void> registrar(CitaEvento e) {
        return repository.existsByEventoId(e.eventoId()).flatMap(existe -> {
            if (existe) {
                return Mono.<Void>empty();
            }
            RegistroAuditoria r = RegistroAuditoria.builder()
                    .eventoId(e.eventoId())
                    .tipoEvento(e.tipo())
                    .recurso("CITA")
                    .recursoId(e.citaId())
                    .estadoAnterior(e.estadoAnterior())
                    .estadoNuevo(e.estadoNuevo())
                    .origen(e.origen())
                    .fechaEvento(e.fechaEvento())
                    .fechaRegistro(LocalDateTime.now())
                    .build();
            return repository.save(r)
                    .doOnNext(g -> log.info("AUDITORIA recurso=CITA id={} {} -> {} origen={}",
                            g.getRecursoId(), g.getEstadoAnterior(), g.getEstadoNuevo(), g.getOrigen()))
                    .then();
        });
    }

    public Flux<RegistroAuditoria> listar() {
        return repository.findAll();
    }

    public Flux<RegistroAuditoria> porCita(Long citaId) {
        return repository.findByRecursoAndRecursoIdOrderByFechaEventoAsc("CITA", citaId);
    }
}
