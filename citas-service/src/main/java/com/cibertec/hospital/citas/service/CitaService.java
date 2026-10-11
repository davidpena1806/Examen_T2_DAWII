package com.cibertec.hospital.citas.service;

import java.time.LocalDateTime;
import java.util.UUID;

import com.cibertec.hospital.citas.client.PacienteClient;
import com.cibertec.hospital.citas.common.ApiException;
import com.cibertec.hospital.citas.dto.CitaEvento;
import com.cibertec.hospital.citas.dto.CitaRequest;
import com.cibertec.hospital.citas.messaging.EventoPublisher;
import com.cibertec.hospital.citas.model.Cita;
import com.cibertec.hospital.citas.model.EstadoCita;
import com.cibertec.hospital.citas.repository.CitaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Service
@RequiredArgsConstructor
public class CitaService {

    private final CitaRepository repository;
    private final PacienteClient pacienteClient;
    private final EventoPublisher publisher;

    public Mono<Cita> crear(CitaRequest r) {
        return pacienteClient.obtenerPaciente(r.pacienteId())
                .flatMap(p -> {
                    if (!"ACTIVO".equals(p.estado())) {
                        return Mono.<Boolean>error(new ApiException(HttpStatus.UNPROCESSABLE_ENTITY,
                                "El paciente " + p.id() + " esta INACTIVO"));
                    }
                    return repository.existsByProfesionalCodigoAndFechaHoraAndEstadoNot(
                            r.profesionalCodigo(), r.fechaHora(), EstadoCita.CANCELADA);
                })
                .flatMap(ocupado -> {
                    if (ocupado) {
                        return Mono.<Cita>error(new ApiException(HttpStatus.CONFLICT,
                                "El profesional " + r.profesionalCodigo() + " ya tiene una cita en " + r.fechaHora()));
                    }
                    return repository.save(nueva(r));
                })
                .flatMap(guardada -> publicar(guardada, null).thenReturn(guardada));
    }

    public Mono<Cita> obtener(Long id) {
        return repository.findById(id)
                .switchIfEmpty(Mono.error(() -> new ApiException(HttpStatus.NOT_FOUND, "Cita no encontrada: " + id)));
    }

    public Flux<Cita> listar(Long pacienteId) {
        return pacienteId == null ? repository.findAll() : repository.findByPacienteId(pacienteId);
    }

    /** Valida la transicion de estado, persiste y publica el evento correspondiente. */
    public Mono<Cita> cambiarEstado(Long id, EstadoCita nuevo) {
        return obtener(id).flatMap(cita -> {
            EstadoCita anterior = cita.getEstado();
            if (!anterior.puedePasarA(nuevo)) {
                return Mono.<Cita>error(new ApiException(HttpStatus.CONFLICT,
                        "Transicion no permitida: " + anterior + " -> " + nuevo));
            }
            cita.setEstado(nuevo);
            cita.setFechaActualizacion(LocalDateTime.now());
            return repository.save(cita)
                    .flatMap(guardada -> publicar(guardada, anterior).thenReturn(guardada));
        });
    }

    private Cita nueva(CitaRequest r) {
        LocalDateTime ahora = LocalDateTime.now();
        return Cita.builder()
                .codigo("CIT-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase())
                .pacienteId(r.pacienteId())
                .especialidad(r.especialidad())
                .fechaHora(r.fechaHora())
                .profesionalCodigo(r.profesionalCodigo())
                .motivo(r.motivo())
                .estado(EstadoCita.PENDIENTE)
                .fechaCreacion(ahora)
                .fechaActualizacion(ahora)
                .build();
    }

    private Mono<Void> publicar(Cita c, EstadoCita anterior) {
        String tipo = anterior == null ? "CitaRegistrada" : switch (c.getEstado()) {
            case CONFIRMADA -> "CitaConfirmada";
            case CANCELADA -> "CitaCancelada";
            case ATENDIDA -> "CitaAtendida";
            case AUSENTE -> "CitaAusente";
            case PENDIENTE -> "CitaPendiente";
        };
        String routingKey = anterior == null ? "cita.registrada" : "cita." + c.getEstado().name().toLowerCase();
        CitaEvento evento = new CitaEvento(UUID.randomUUID().toString(), tipo, c.getId(), c.getCodigo(),
                c.getPacienteId(), anterior == null ? null : anterior.name(), c.getEstado().name(),
                LocalDateTime.now(), "citas-service");
        return publisher.publicar(routingKey, evento);
    }
}
