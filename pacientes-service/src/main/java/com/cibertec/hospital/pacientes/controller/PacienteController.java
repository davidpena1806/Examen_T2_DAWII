package com.cibertec.hospital.pacientes.controller;

import com.cibertec.hospital.pacientes.dto.CambioEstadoRequest;
import com.cibertec.hospital.pacientes.dto.PacienteRequest;
import com.cibertec.hospital.pacientes.model.Paciente;
import com.cibertec.hospital.pacientes.service.PacienteService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@RestController
@RequestMapping("/api/pacientes")
@RequiredArgsConstructor
public class PacienteController {

    private final PacienteService service;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public Mono<Paciente> registrar(@Valid @RequestBody PacienteRequest request) {
        return service.registrar(request);
    }

    @GetMapping("/{id}")
    public Mono<Paciente> obtener(@PathVariable Long id) {
        return service.obtener(id);
    }

    @GetMapping
    public Flux<Paciente> listar() {
        return service.listar();
    }

    @PutMapping("/{id}")
    public Mono<Paciente> actualizar(@PathVariable Long id, @Valid @RequestBody PacienteRequest request) {
        return service.actualizar(id, request);
    }

    @PatchMapping("/{id}/estado")
    public Mono<Paciente> cambiarEstado(@PathVariable Long id, @Valid @RequestBody CambioEstadoRequest request) {
        return service.cambiarEstado(id, request.estado());
    }
}
