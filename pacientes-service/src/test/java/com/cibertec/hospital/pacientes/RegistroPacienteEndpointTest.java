package com.cibertec.hospital.pacientes;

import java.util.Map;
import java.util.concurrent.ThreadLocalRandom;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.AutoConfigureWebTestClient;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.reactive.server.WebTestClient;

/**
 * Pruebas del UNICO endpoint cubierto por la integracion continua: POST /api/pacientes.
 * El workflow de GitHub Actions ejecuta solo esta clase.
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureWebTestClient
class RegistroPacienteEndpointTest {

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
    void registroValidoDevuelve201YPacienteActivo() {
        client.post().uri("/api/pacientes").bodyValue(body(docUnico())).exchange()
                .expectStatus().isCreated()
                .expectBody()
                .jsonPath("$.id").isNotEmpty()
                .jsonPath("$.estado").isEqualTo("ACTIVO");
    }

    @Test
    void registroDuplicadoDevuelve409() {
        String doc = docUnico();
        client.post().uri("/api/pacientes").bodyValue(body(doc)).exchange().expectStatus().isCreated();
        client.post().uri("/api/pacientes").bodyValue(body(doc)).exchange()
                .expectStatus().isEqualTo(409)
                .expectBody().jsonPath("$.status").isEqualTo(409);
    }

    @Test
    void registroConDatosInvalidosDevuelve400() {
        client.post().uri("/api/pacientes").bodyValue(Map.of("tipoDocumento", "DNI")).exchange()
                .expectStatus().isBadRequest();
    }
}
