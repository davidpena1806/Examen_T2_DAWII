package com.cibertec.hospital.notificaciones.controller;

import com.cibertec.hospital.notificaciones.model.Notificacion;
import com.cibertec.hospital.notificaciones.service.NotificacionService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Flux;

@RestController
@RequestMapping("/api/notificaciones")
@RequiredArgsConstructor
public class NotificacionController {

    private final NotificacionService service;

    @GetMapping
    public Flux<Notificacion> listar() {
        return service.listar();
    }

    @GetMapping("/paciente/{pacienteId}")
    public Flux<Notificacion> porPaciente(@PathVariable Long pacienteId) {
        return service.listarPorPaciente(pacienteId);
    }
}
