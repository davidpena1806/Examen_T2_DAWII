package com.cibertec.hospital.citas;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.UUID;

import com.cibertec.hospital.citas.client.PacienteClient;
import com.cibertec.hospital.citas.common.ApiException;
import com.cibertec.hospital.citas.dto.PacienteInfo;
import com.cibertec.hospital.citas.messaging.EventoPublisher;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.AutoConfigureWebTestClient;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.HttpStatus;
import org.springframework.test.web.reactive.server.WebTestClient;
import reactor.core.publisher.Mono;

/** Prueba de integracion: controlador + servicio + R2DBC (H2). Pacientes y RabbitMQ se simulan. */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureWebTestClient
class CitaControllerTest {

    @Autowired
    WebTestClient client;
    @MockBean
    PacienteClient pacienteClient;
    @MockBean
    EventoPublisher publisher;

    @BeforeEach
    void setUp() {
        when(pacienteClient.obtenerPaciente(1L)).thenReturn(Mono.just(new PacienteInfo(1L, "Ana", "Prueba", "ACTIVO")));
        when(pacienteClient.obtenerPaciente(404L))
                .thenReturn(Mono.error(new ApiException(HttpStatus.NOT_FOUND, "El paciente 404 no existe")));
        when(publisher.publicar(anyString(), any())).thenReturn(Mono.empty());
    }

    private Map<String, Object> body(long pacienteId, String agenda) {
        return Map.of("pacienteId", pacienteId, "especialidad", "Medicina General",
                "fechaHora", LocalDateTime.now().plusDays(10).withNano(0).toString(),
                "profesionalCodigo", agenda, "motivo", "Control administrativo");
    }

    private String agendaUnica() {
        return "AG-" + UUID.randomUUID().toString().substring(0, 8);
    }

    private Map<?, ?> crear(String agenda) {
        return client.post().uri("/api/citas").bodyValue(body(1L, agenda)).exchange()
                .expectStatus().isCreated()
                .expectBody(Map.class).returnResult().getResponseBody();
    }

    @Test
    void registraYConsultaCita() {
        Map<?, ?> creada = crear(agendaUnica());
        assertThat(creada.get("estado")).isEqualTo("PENDIENTE");

        client.get().uri("/api/citas/{id}", creada.get("id")).exchange()
                .expectStatus().isOk()
                .expectBody().jsonPath("$.pacienteId").isEqualTo(1);
    }

    @Test
    void rechazaCitaParaPacienteInexistente() {
        client.post().uri("/api/citas").bodyValue(body(404L, agendaUnica())).exchange()
                .expectStatus().isNotFound();
    }

    @Test
    void rechazaCitaDuplicadaEnLaMismaAgenda() {
        String agenda = agendaUnica();
        Map<String, Object> mismo = body(1L, agenda);
        client.post().uri("/api/citas").bodyValue(mismo).exchange().expectStatus().isCreated();
        client.post().uri("/api/citas").bodyValue(mismo).exchange().expectStatus().isEqualTo(409);
    }

    @Test
    void confirmaYLuegoRechazaTransicionInvalida() {
        Map<?, ?> creada = crear(agendaUnica());

        client.patch().uri("/api/citas/{id}/estado", creada.get("id")).bodyValue(Map.of("estado", "CONFIRMADA"))
                .exchange().expectStatus().isOk()
                .expectBody().jsonPath("$.estado").isEqualTo("CONFIRMADA");

        client.patch().uri("/api/citas/{id}/estado", creada.get("id")).bodyValue(Map.of("estado", "PENDIENTE"))
                .exchange().expectStatus().isEqualTo(409);
    }

    @Test
    void citaInexistenteDevuelve404() {
        client.get().uri("/api/citas/999999").exchange().expectStatus().isNotFound();
    }
}
