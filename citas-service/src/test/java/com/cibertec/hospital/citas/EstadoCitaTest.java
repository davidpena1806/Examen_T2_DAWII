package com.cibertec.hospital.citas;

import static org.assertj.core.api.Assertions.assertThat;

import com.cibertec.hospital.citas.model.EstadoCita;
import org.junit.jupiter.api.Test;

class EstadoCitaTest {

    @Test
    void transicionesValidas() {
        assertThat(EstadoCita.PENDIENTE.puedePasarA(EstadoCita.CONFIRMADA)).isTrue();
        assertThat(EstadoCita.PENDIENTE.puedePasarA(EstadoCita.CANCELADA)).isTrue();
        assertThat(EstadoCita.CONFIRMADA.puedePasarA(EstadoCita.ATENDIDA)).isTrue();
        assertThat(EstadoCita.CONFIRMADA.puedePasarA(EstadoCita.AUSENTE)).isTrue();
    }

    @Test
    void transicionesNoPermitidas() {
        assertThat(EstadoCita.CANCELADA.puedePasarA(EstadoCita.CONFIRMADA)).isFalse();
        assertThat(EstadoCita.PENDIENTE.puedePasarA(EstadoCita.ATENDIDA)).isFalse(); // atendida requiere cita confirmada
        assertThat(EstadoCita.ATENDIDA.puedePasarA(EstadoCita.CANCELADA)).isFalse();
    }
}
