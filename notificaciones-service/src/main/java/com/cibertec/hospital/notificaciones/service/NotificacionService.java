package com.cibertec.hospital.notificaciones.service;

import java.time.LocalDateTime;

import com.cibertec.hospital.notificaciones.dto.CitaEvento;
import com.cibertec.hospital.notificaciones.model.Notificacion;
import com.cibertec.hospital.notificaciones.repository.NotificacionRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Slf4j
@Service
@RequiredArgsConstructor
public class NotificacionService {

    private final NotificacionRepository repository;

    public Mono<Void> registrar(CitaEvento e) {
        return repository.existsByEventoId(e.eventoId()).flatMap(existe -> {
            if (existe) {
                log.info("Evento {} ya procesado, se ignora", e.eventoId());
                return Mono.<Void>empty();
            }
            Notificacion n = Notificacion.builder()
                    .eventoId(e.eventoId())
                    .tipo(e.tipo())
                    .pacienteId(e.pacienteId())
                    .citaId(e.citaId())
                    .mensaje(mensaje(e))
                    .canal("SIMULADO")
                    .fechaEnvio(LocalDateTime.now())
                    .build();
            return repository.save(n)
                    .doOnNext(g -> log.info("NOTIFICACION [paciente={}] {}", g.getPacienteId(), g.getMensaje()))
                    .then();
        });
    }

    public Flux<Notificacion> listar() {
        return repository.findAll();
    }

    public Flux<Notificacion> listarPorPaciente(Long pacienteId) {
        return repository.findByPacienteId(pacienteId);
    }

    String mensaje(CitaEvento e) {
        return switch (e.tipo()) {
            case "CitaRegistrada" -> "Su cita " + e.codigoCita() + " fue registrada y esta PENDIENTE de confirmacion.";
            case "CitaConfirmada" -> "Su cita " + e.codigoCita() + " fue CONFIRMADA.";
            case "CitaCancelada" -> "Su cita " + e.codigoCita() + " fue CANCELADA.";
            default -> "El estado de su cita " + e.codigoCita() + " cambio a " + e.estadoNuevo() + ".";
        };
    }
}
