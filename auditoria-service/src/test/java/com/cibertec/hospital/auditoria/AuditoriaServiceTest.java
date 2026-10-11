package com.cibertec.hospital.auditoria;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.LocalDateTime;
import java.util.UUID;
import java.util.concurrent.ThreadLocalRandom;

import com.cibertec.hospital.auditoria.dto.CitaEvento;
import com.cibertec.hospital.auditoria.messaging.CitaEventoListener;
import com.cibertec.hospital.auditoria.service.AuditoriaService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import reactor.test.StepVerifier;

@SpringBootTest
class AuditoriaServiceTest {

    @Autowired
    AuditoriaService service;
    @Autowired
    CitaEventoListener listener;

    private CitaEvento evento(long citaId, String anterior, String nuevo) {
        return new CitaEvento(UUID.randomUUID().toString(), "CitaConfirmada", citaId, "CIT-TEST", 1L,
                anterior, nuevo, LocalDateTime.now(), "citas-service");
    }

    @Test
    void registraEstadoAnteriorNuevoYOrigenDelCambio() {
        long citaId = ThreadLocalRandom.current().nextLong(1_000_000L, 9_000_000L);
        listener.recibir(evento(citaId, "PENDIENTE", "CONFIRMADA")); // simula consumo desde RabbitMQ

        StepVerifier.create(service.porCita(citaId))
                .assertNext(r -> {
                    assertThat(r.getEstadoAnterior()).isEqualTo("PENDIENTE");
                    assertThat(r.getEstadoNuevo()).isEqualTo("CONFIRMADA");
                    assertThat(r.getOrigen()).isEqualTo("citas-service");
                    assertThat(r.getFechaRegistro()).isNotNull();
                })
                .verifyComplete();
    }

    @Test
    void esIdempotenteAnteEventosRepetidos() {
        long citaId = ThreadLocalRandom.current().nextLong(1_000_000L, 9_000_000L);
        CitaEvento ev = evento(citaId, "PENDIENTE", "CONFIRMADA");

        StepVerifier.create(service.registrar(ev).then(service.registrar(ev))).verifyComplete();
        StepVerifier.create(service.porCita(citaId)).expectNextCount(1).verifyComplete();
    }
}
