package com.cibertec.hospital.citas.messaging;

import com.cibertec.hospital.citas.dto.CitaEvento;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

@Slf4j
@Component
@RequiredArgsConstructor
public class RabbitEventoPublisher implements EventoPublisher {

    private final RabbitTemplate rabbitTemplate;

    @Override
    public Mono<Void> publicar(String routingKey, CitaEvento evento) {
        return Mono.fromRunnable(() -> {
                    rabbitTemplate.convertAndSend(RabbitConfig.EXCHANGE, routingKey, evento);
                    log.info("Evento publicado exchange={} rk={} tipo={} citaId={}",
                            RabbitConfig.EXCHANGE, routingKey, evento.tipo(), evento.citaId());
                })
                .subscribeOn(Schedulers.boundedElastic())
                .onErrorResume(e -> {
                    log.error("No se pudo publicar el evento {}: {}", evento.tipo(), e.toString());
                    return Mono.empty();
                })
                .then();
    }
}
