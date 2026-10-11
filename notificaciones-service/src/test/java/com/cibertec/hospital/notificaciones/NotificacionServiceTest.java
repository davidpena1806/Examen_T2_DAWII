package com.cibertec.hospital.notificaciones;

import java.time.LocalDateTime;
import java.util.concurrent.ThreadLocalRandom;
import java.util.UUID;

import com.cibertec.hospital.notificaciones.dto.CitaEvento;
import com.cibertec.hospital.notificaciones.messaging.CitaEventoListener;
import com.cibertec.hospital.notificaciones.repository.NotificacionRepository;
import com.cibertec.hospital.notificaciones.service.NotificacionService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import reactor.test.StepVerifier;

@SpringBootTest
class NotificacionServiceTest {

    @Autowired
    NotificacionService service;
    @Autowired
    CitaEventoListener listener;
    @Autowired
    NotificacionRepository repository;

    private CitaEvento evento(long pacienteId, String tipo) {
        return new CitaEvento(UUID.randomUUID().toString(), tipo, 10L, "CIT-TEST", pacienteId,
                "PENDIENTE", "CONFIRMADA", LocalDateTime.now(), "citas-service");
    }

    private long pacienteUnico() {
        return ThreadLocalRandom.current().nextLong(1_000_000L, 9_000_000L);
    }

    @Test
    void consumirEventoCitaConfirmadaGeneraNotificacion() {
        long paciente = pacienteUnico();
        listener.recibir(evento(paciente, "CitaConfirmada")); // simula la entrega desde RabbitMQ

        StepVerifier.create(repository.findByPacienteId(paciente))
                .assertNext(n -> {
                    org.assertj.core.api.Assertions.assertThat(n.getMensaje()).contains("CONFIRMADA");
                    org.assertj.core.api.Assertions.assertThat(n.getCanal()).isEqualTo("SIMULADO");
                })
                .verifyComplete();
    }

    @Test
    void elMismoEventoNoGeneraNotificacionesDuplicadas() {
        long paciente = pacienteUnico();
        CitaEvento ev = evento(paciente, "CitaCancelada");

        StepVerifier.create(service.registrar(ev).then(service.registrar(ev))).verifyComplete();

        StepVerifier.create(repository.findByPacienteId(paciente)).expectNextCount(1).verifyComplete();
    }
}
