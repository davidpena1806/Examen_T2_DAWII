package com.cibertec.hospital.citas.messaging;

import com.cibertec.hospital.citas.dto.CitaEvento;
import reactor.core.publisher.Mono;

public interface EventoPublisher {
    Mono<Void> publicar(String routingKey, CitaEvento evento);
}
