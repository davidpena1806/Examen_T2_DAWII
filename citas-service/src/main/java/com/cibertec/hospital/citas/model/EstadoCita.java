package com.cibertec.hospital.citas.model;

/** Estados de la cita y transiciones permitidas (maquina de estados). */
public enum EstadoCita {
    PENDIENTE, CONFIRMADA, ATENDIDA, CANCELADA, AUSENTE;

    public boolean puedePasarA(EstadoCita nuevo) {
        return switch (this) {
            case PENDIENTE -> nuevo == CONFIRMADA || nuevo == CANCELADA;
            case CONFIRMADA -> nuevo == ATENDIDA || nuevo == CANCELADA || nuevo == AUSENTE;
            case ATENDIDA, CANCELADA, AUSENTE -> false; // estados finales
        };
    }
}
