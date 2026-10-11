package com.cibertec.hospital.citas.client;

import java.time.Duration;

import com.cibertec.hospital.citas.common.ApiException;
import com.cibertec.hospital.citas.dto.PacienteInfo;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

@Slf4j
@Component
public class WebClientPacienteClient implements PacienteClient {

    private final WebClient webClient;
    private final Duration timeout;

    public WebClientPacienteClient(WebClient.Builder builder,
                                   @Value("${clients.pacientes.url:http://pacientes-service}") String baseUrl,
                                   @Value("${clients.pacientes.timeout-seconds:3}") long timeoutSeconds) {
        this.webClient = builder.baseUrl(baseUrl).build();
        this.timeout = Duration.ofSeconds(timeoutSeconds);
    }

    @Override
    public Mono<PacienteInfo> obtenerPaciente(Long id) {
        return webClient.get().uri("/api/pacientes/{id}", id)
                .retrieve()
                .onStatus(s -> s.value() == 404,
                        r -> Mono.error(new ApiException(HttpStatus.NOT_FOUND, "El paciente " + id + " no existe")))
                .bodyToMono(PacienteInfo.class)
                .timeout(timeout)
                .onErrorMap(e -> !(e instanceof ApiException), e -> {
                    log.warn("Fallo al consultar pacientes-service: {}", e.toString());
                    return new ApiException(HttpStatus.SERVICE_UNAVAILABLE,
                            "El servicio de pacientes no esta disponible. Intente nuevamente.");
                });
    }
}
