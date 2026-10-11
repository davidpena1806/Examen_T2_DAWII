# Hospital Vida Salud – Microservicios reactivos (Evaluación T2)

## Requisitos

JDK 21, Maven 3.9+ (o Maven Wrapper), Docker (PostgreSQL + RabbitMQ).

## Ejecución

**Arrancar en este orden** (cada uno en su terminal):

```bash
mvn -pl eureka-server spring-boot:run
mvn -pl pacientes-service spring-boot:run
mvn -pl citas-service spring-boot:run
mvn -pl notificaciones-service spring-boot:run
mvn -pl auditoria-service spring-boot:run
mvn -pl api-gateway spring-boot:run
```

Eureka: http://localhost:8761 · RabbitMQ: http://localhost:15672

## Pruebas automatizadas

```bash
mvn verify          # o ./mvnw verify si se genera el wrapper (mvn wrapper:wrapper)
```

## Prueba funcional end-to-end (siempre por el Gateway :8080)

```bash
# 1. Registrar paciente
curl -s -X POST localhost:8080/api/pacientes -H 'Content-Type: application/json' -d '{
 "tipoDocumento":"DNI","numeroDocumento":"00000001","nombres":"Ana","apellidos":"Ficticia Prueba",
 "fechaNacimiento":"1990-05-20","telefono":"999000111","correo":"ana@example.test"}'
# 2. Duplicado -> 409
#    (repetir el comando anterior)
# 3. Consultar
curl -s localhost:8080/api/pacientes/1
# 4. Registrar cita (use una fecha futura)  -> llama sincrónicamente a pacientes-service
curl -s -X POST localhost:8080/api/citas -H 'Content-Type: application/json' -d '{
 "pacienteId":1,"especialidad":"Medicina General","fechaHora":"2027-03-15T10:00:00",
 "profesionalCodigo":"AG-001","motivo":"Control administrativo"}'
# 5. Confirmar -> publica CitaConfirmada en RabbitMQ
curl -s -X PATCH localhost:8080/api/citas/1/estado -H 'Content-Type: application/json' -d '{"estado":"CONFIRMADA"}'
# 6. Transición inválida (CONFIRMADA -> PENDIENTE) -> 409
curl -s -X PATCH localhost:8080/api/citas/1/estado -H 'Content-Type: application/json' -d '{"estado":"PENDIENTE"}'
# 7. Verificar consumidores
curl -s localhost:8080/api/notificaciones
curl -s localhost:8080/api/auditoria/cita/1
# 8. Paciente inexistente -> 404 ; con pacientes-service detenido -> 503
curl -s -X POST localhost:8080/api/citas -H 'Content-Type: application/json' -d '{"pacienteId":9999,"especialidad":"X","fechaHora":"2027-03-15T11:00:00","profesionalCodigo":"AG-001","motivo":"x"}'
```

## Integración continua (alcance: un endpoint)

La CI cubre **un solo endpoint: `POST /api/pacientes`** (registro de paciente, `pacientes-service`).
`.github/workflows/ci.yml` se ejecuta en **push** y **pull_request** hacia `main`: checkout → JDK 21 + caché Maven → compila solo `pacientes-service` y ejecuta `RegistroPacienteEndpointTest` (registro válido 201, duplicado 409, datos inválidos 400). Si algo falla el job queda en rojo.

Ejecutar lo mismo en local:

```bash
mvn -pl pacientes-service -am verify -Dtest=RegistroPacienteEndpointTest -Dsurefire.failIfNoSpecifiedTests=false
```

El resto de módulos y pruebas siguen disponibles con `mvn verify`, pero no forman parte de la CI. Ver [`docs/EVIDENCIAS.md`](docs/EVIDENCIAS.md) para el ciclo rojo→verde.
