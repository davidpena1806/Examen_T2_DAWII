package com.cibertec.hospital.pacientes;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.LocalDate;

import com.cibertec.hospital.pacientes.common.ApiException;
import com.cibertec.hospital.pacientes.dto.PacienteRequest;
import com.cibertec.hospital.pacientes.model.EstadoPaciente;
import com.cibertec.hospital.pacientes.model.Paciente;
import com.cibertec.hospital.pacientes.repository.PacienteRepository;
import com.cibertec.hospital.pacientes.service.PacienteService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

@ExtendWith(MockitoExtension.class)
class PacienteServiceTest {

    @Mock
    PacienteRepository repository;
    @InjectMocks
    PacienteService service;

    private PacienteRequest request() {
        return new PacienteRequest("DNI", "00000001", "Ana", "Ficticia Prueba",
                LocalDate.of(1990, 5, 20), "999000111", "ana@example.test", null);
    }

    @Test
    void registraPacienteValidoComoActivo() {
        when(repository.findByTipoDocumentoAndNumeroDocumento("DNI", "00000001")).thenReturn(Mono.empty());
        when(repository.save(any(Paciente.class))).thenAnswer(i -> {
            Paciente p = i.getArgument(0);
            p.setId(1L);
            return Mono.just(p);
        });

        StepVerifier.create(service.registrar(request()))
                .assertNext(p -> {
                    assertThat(p.getId()).isEqualTo(1L);
                    assertThat(p.getEstado()).isEqualTo(EstadoPaciente.ACTIVO);
                    assertThat(p.getFechaRegistro()).isNotNull();
                })
                .verifyComplete();
    }

    @Test
    void rechazaPacienteDuplicadoConConflicto() {
        when(repository.findByTipoDocumentoAndNumeroDocumento("DNI", "00000001"))
                .thenReturn(Mono.just(new Paciente()));

        StepVerifier.create(service.registrar(request()))
                .expectErrorSatisfies(e -> {
                    assertThat(e).isInstanceOf(ApiException.class);
                    assertThat(((ApiException) e).getStatus()).isEqualTo(HttpStatus.CONFLICT);
                })
                .verify();
        verify(repository, never()).save(any());
    }

    @Test
    void consultaDePacienteInexistenteDevuelve404() {
        when(repository.findById(99L)).thenReturn(Mono.empty());

        StepVerifier.create(service.obtener(99L))
                .expectErrorSatisfies(e -> assertThat(((ApiException) e).getStatus()).isEqualTo(HttpStatus.NOT_FOUND))
                .verify();
    }
}
