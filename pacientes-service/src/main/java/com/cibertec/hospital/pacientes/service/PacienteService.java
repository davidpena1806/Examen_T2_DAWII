package com.cibertec.hospital.pacientes.service;

import java.time.LocalDateTime;

import com.cibertec.hospital.pacientes.common.ApiException;
import com.cibertec.hospital.pacientes.dto.PacienteRequest;
import com.cibertec.hospital.pacientes.model.EstadoPaciente;
import com.cibertec.hospital.pacientes.model.Paciente;
import com.cibertec.hospital.pacientes.repository.PacienteRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Service
@RequiredArgsConstructor
public class PacienteService {

    private final PacienteRepository repository;

    public Mono<Paciente> registrar(PacienteRequest r) {
        String tipo = r.tipoDocumento().toUpperCase();
        return repository.findByTipoDocumentoAndNumeroDocumento(tipo, r.numeroDocumento())
                .flatMap(existente -> Mono.<Paciente>error(new ApiException(HttpStatus.CONFLICT,
                        "Ya existe un paciente con documento " + tipo + " " + r.numeroDocumento())))
                .switchIfEmpty(Mono.defer(() -> repository.save(nuevo(r, tipo))));
    }

    public Mono<Paciente> obtener(Long id) {
        return repository.findById(id)
                .switchIfEmpty(Mono.error(() -> new ApiException(HttpStatus.NOT_FOUND,
                        "Paciente no encontrado: " + id)));
    }

    public Flux<Paciente> listar() {
        return repository.findAll();
    }

    public Mono<Paciente> actualizar(Long id, PacienteRequest r) {
        String tipo = r.tipoDocumento().toUpperCase();
        return obtener(id).flatMap(actual ->
                repository.findByTipoDocumentoAndNumeroDocumento(tipo, r.numeroDocumento())
                        .filter(otro -> !otro.getId().equals(id))
                        .flatMap(otro -> Mono.<Paciente>error(new ApiException(HttpStatus.CONFLICT,
                                "El documento ya pertenece a otro paciente")))
                        .switchIfEmpty(Mono.defer(() -> {
                            actual.setTipoDocumento(tipo);
                            actual.setNumeroDocumento(r.numeroDocumento());
                            actual.setNombres(r.nombres());
                            actual.setApellidos(r.apellidos());
                            actual.setFechaNacimiento(r.fechaNacimiento());
                            actual.setTelefono(r.telefono());
                            actual.setCorreo(r.correo());
                            actual.setDireccion(r.direccion());
                            return repository.save(actual);
                        })));
    }

    public Mono<Paciente> cambiarEstado(Long id, EstadoPaciente nuevoEstado) {
        return obtener(id).flatMap(p -> {
            p.setEstado(nuevoEstado);
            return repository.save(p);
        });
    }

    private Paciente nuevo(PacienteRequest r, String tipo) {
        return Paciente.builder()
                .tipoDocumento(tipo)
                .numeroDocumento(r.numeroDocumento())
                .nombres(r.nombres())
                .apellidos(r.apellidos())
                .fechaNacimiento(r.fechaNacimiento())
                .telefono(r.telefono())
                .correo(r.correo())
                .direccion(r.direccion())
                .fechaRegistro(LocalDateTime.now())
                .estado(EstadoPaciente.ACTIVO)
                .build();
    }
}
