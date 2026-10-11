package com.cibertec.hospital.citas;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.LocalDateTime;

import com.cibertec.hospital.citas.client.PacienteClient;
import com.cibertec.hospital.citas.common.ApiException;
import com.cibertec.hospital.citas.dto.CitaEvento;
import com.cibertec.hospital.citas.dto.CitaRequest;
import com.cibertec.hospital.citas.dto.PacienteInfo;
import com.cibertec.hospital.citas.messaging.EventoPublisher;
import com.cibertec.hospital.citas.model.Cita;
import com.cibertec.hospital.citas.model.EstadoCita;
import com.cibertec.hospital.citas.repository.CitaRepository;
import com.cibertec.hospital.citas.service.CitaService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

@ExtendWith(MockitoExtension.class)
class CitaServiceTest {

    @Mock
    CitaRepository repository;
    @Mock
    PacienteClient pacienteClient;
    @Mock
    EventoPublisher publisher;
    @InjectMocks
    CitaService service;

    private CitaRequest request() {
        return new CitaRequest(1L, "Medicina General", LocalDateTime.now().plusDays(5), "AG-001", "Control administrativo");
    }

    private Cita cita(EstadoCita estado) {
        return Cita.builder().id(10L).codigo("CIT-TEST").pacienteId(1L).estado(estado).build();
    }

    private void assertEstado(Throwable e, HttpStatus esperado) {
        assertThat(e).isInstanceOf(ApiException.class);
        assertThat(((ApiException) e).getStatus()).isEqualTo(esperado);
    }

    @Test
    void creaCitaParaPacienteValidoYPublicaEventoRegistrada() {
        when(pacienteClient.obtenerPaciente(1L)).thenReturn(Mono.just(new PacienteInfo(1L, "Ana", "Prueba", "ACTIVO")));
        when(repository.existsByProfesionalCodigoAndFechaHoraAndEstadoNot(any(), any(), any())).thenReturn(Mono.just(false));
        when(repository.save(any(Cita.class))).thenAnswer(i -> {
            Cita c = i.getArgument(0);
            c.setId(10L);
            return Mono.just(c);
        });
        when(publisher.publicar(anyString(), any())).thenReturn(Mono.empty());

        StepVerifier.create(service.crear(request()))
                .assertNext(c -> {
                    assertThat(c.getEstado()).isEqualTo(EstadoCita.PENDIENTE);
                    assertThat(c.getCodigo()).startsWith("CIT-");
                })
                .verifyComplete();
        verify(publisher).publicar(eq("cita.registrada"), any(CitaEvento.class));
    }

    @Test
    void rechazaCitaSiElPacienteNoExiste() {
        when(pacienteClient.obtenerPaciente(1L))
                .thenReturn(Mono.error(new ApiException(HttpStatus.NOT_FOUND, "El paciente 1 no existe")));

        StepVerifier.create(service.crear(request()))
                .expectErrorSatisfies(e -> assertEstado(e, HttpStatus.NOT_FOUND))
                .verify();
        verify(repository, never()).save(any());
    }

    @Test
    void rechazaCitaSiElPacienteEstaInactivo() {
        when(pacienteClient.obtenerPaciente(1L)).thenReturn(Mono.just(new PacienteInfo(1L, "Ana", "Prueba", "INACTIVO")));

        StepVerifier.create(service.crear(request()))
                .expectErrorSatisfies(e -> assertEstado(e, HttpStatus.UNPROCESSABLE_ENTITY))
                .verify();
    }

    @Test
    void rechazaCitaDuplicadaEnLaAgendaDelProfesional() {
        when(pacienteClient.obtenerPaciente(1L)).thenReturn(Mono.just(new PacienteInfo(1L, "Ana", "Prueba", "ACTIVO")));
        when(repository.existsByProfesionalCodigoAndFechaHoraAndEstadoNot(any(), any(), any())).thenReturn(Mono.just(true));

        StepVerifier.create(service.crear(request()))
                .expectErrorSatisfies(e -> assertEstado(e, HttpStatus.CONFLICT))
                .verify();
        verify(repository, never()).save(any());
    }

    @Test
    void noPermiteConfirmarUnaCitaCancelada() {
        when(repository.findById(10L)).thenReturn(Mono.just(cita(EstadoCita.CANCELADA)));

        StepVerifier.create(service.cambiarEstado(10L, EstadoCita.CONFIRMADA))
                .expectErrorSatisfies(e -> assertEstado(e, HttpStatus.CONFLICT))
                .verify();
        verify(repository, never()).save(any());
        verify(publisher, never()).publicar(anyString(), any());
    }

    @Test
    void noPermiteMarcarAtendidaUnaCitaPendiente() {
        when(repository.findById(10L)).thenReturn(Mono.just(cita(EstadoCita.PENDIENTE)));

        StepVerifier.create(service.cambiarEstado(10L, EstadoCita.ATENDIDA))
                .expectErrorSatisfies(e -> assertEstado(e, HttpStatus.CONFLICT))
                .verify();
    }

    @Test
    void confirmarCitaPublicaEventoCitaConfirmada() {
        when(repository.findById(10L)).thenReturn(Mono.just(cita(EstadoCita.PENDIENTE)));
        when(repository.save(any(Cita.class))).thenAnswer(i -> Mono.just(i.getArgument(0)));
        when(publisher.publicar(anyString(), any())).thenReturn(Mono.empty());

        StepVerifier.create(service.cambiarEstado(10L, EstadoCita.CONFIRMADA))
                .assertNext(c -> assertThat(c.getEstado()).isEqualTo(EstadoCita.CONFIRMADA))
                .verifyComplete();

        ArgumentCaptor<CitaEvento> captor = ArgumentCaptor.forClass(CitaEvento.class);
        verify(publisher).publicar(eq("cita.confirmada"), captor.capture());
        CitaEvento ev = captor.getValue();
        assertThat(ev.tipo()).isEqualTo("CitaConfirmada");
        assertThat(ev.estadoAnterior()).isEqualTo("PENDIENTE");
        assertThat(ev.estadoNuevo()).isEqualTo("CONFIRMADA");
        assertThat(ev.pacienteId()).isEqualTo(1L);
    }

    @Test
    void citaInexistenteDevuelve404() {
        when(repository.findById(99L)).thenReturn(Mono.empty());

        StepVerifier.create(service.obtener(99L))
                .expectErrorSatisfies(e -> assertEstado(e, HttpStatus.NOT_FOUND))
                .verify();
    }
}
