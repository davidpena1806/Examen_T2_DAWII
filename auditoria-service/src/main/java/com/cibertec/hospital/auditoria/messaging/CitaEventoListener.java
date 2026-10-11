package com.cibertec.hospital.auditoria.messaging;

import java.time.Duration;

import com.cibertec.hospital.auditoria.dto.CitaEvento;
import com.cibertec.hospital.auditoria.service.AuditoriaService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class CitaEventoListener {

    private final AuditoriaService service;

    @RabbitListener(queues = RabbitConfig.COLA)
    public void recibir(CitaEvento evento) {
        log.info("Evento recibido para auditoria tipo={} citaId={}", evento.tipo(), evento.citaId());
        service.registrar(evento).block(Duration.ofSeconds(10));
    }
}
