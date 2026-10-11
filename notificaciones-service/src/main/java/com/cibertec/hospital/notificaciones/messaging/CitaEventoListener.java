package com.cibertec.hospital.notificaciones.messaging;

import java.time.Duration;

import com.cibertec.hospital.notificaciones.dto.CitaEvento;
import com.cibertec.hospital.notificaciones.service.NotificacionService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class CitaEventoListener {

    private final NotificacionService service;

    @RabbitListener(queues = RabbitConfig.COLA)
    public void recibir(CitaEvento evento) {
        log.info("Evento recibido tipo={} citaId={}", evento.tipo(), evento.citaId());
        service.registrar(evento).block(Duration.ofSeconds(10));
    }
}
