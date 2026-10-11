package com.cibertec.hospital.auditoria.controller;

import com.cibertec.hospital.auditoria.model.RegistroAuditoria;
import com.cibertec.hospital.auditoria.service.AuditoriaService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Flux;

@RestController
@RequestMapping("/api/auditoria")
@RequiredArgsConstructor
public class AuditoriaController {

    private final AuditoriaService service;

    @GetMapping
    public Flux<RegistroAuditoria> listar() {
        return service.listar();
    }

    @GetMapping("/cita/{citaId}")
    public Flux<RegistroAuditoria> porCita(@PathVariable Long citaId) {
        return service.porCita(citaId);
    }
}
