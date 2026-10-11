package com.cibertec.hospital.pacientes;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Map;
import java.util.concurrent.ThreadLocalRandom;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.AutoConfigureWebTestClient;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.reactive.server.WebTestClient;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureWebTestClient
class PacienteControllerTest {

    @Autowired
    WebTestClient client;

    private Map<String, Object> body(String doc) {
        return Map.of("tipoDocumento", "DNI", "numeroDocumento", doc, "nombres", "Ana",
                "apellidos", "Ficticia Prueba", "fechaNacimiento", "1990-05-20",
                "telefono", "999000111", "correo", "ana@example.test");
    }

    private String docUnico() {
        return String.valueOf(ThreadLocalRandom.current().nextInt(10_000_000, 99_999_999));
    }

    @Test
    void registraYConsultaPacientePorId() {
        Map<?, ?> creado = client.post().uri("/api/pacientes").bodyValue(body(docUnico()))
                .exchange()
                .expectStatus().isCreated()
                .expectBody(Map.class)
                .returnResult().getResponseBody();
        assertThat(creado).isNotNull();
        assertThat(creado.get("estado")).isEqualTo("ACTIVO");

        client.get().uri("/api/pacientes/{id}", creado.get("id")).exchange()
                .expectStatus().isOk()
                .expectBody().jsonPath("$.nombres").isEqualTo("Ana");
    }

    @Test
    void rechazaPacienteDuplicado() {
        String doc = docUnico();
        client.post().uri("/api/pacientes").bodyValue(body(doc)).exchange().expectStatus().isCreated();
        client.post().uri("/api/pacientes").bodyValue(body(doc)).exchange()
                .expectStatus().isEqualTo(409)
                .expectBody().jsonPath("$.status").isEqualTo(409);
    }

    @Test
    void recursoInexistenteDevuelve404() {
        client.get().uri("/api/pacientes/999999").exchange()
                .expectStatus().isNotFound()
                .expectBody().jsonPath("$.status").isEqualTo(404);
    }

    @Test
    void datosInvalidosDevuelven400() {
        client.post().uri("/api/pacientes").bodyValue(Map.of("tipoDocumento", "DNI")).exchange()
                .expectStatus().isBadRequest();
    }
}
