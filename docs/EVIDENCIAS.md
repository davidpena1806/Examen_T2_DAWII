# Guía de evidencias (capturas a adjuntar en el documento)

| #   | Evidencia                        | Cómo obtenerla                                                                                                                                                                  |
| --- | -------------------------------- | ------------------------------------------------------------------------------------------------------------------------------------------------------------------------------- |
| 1   | Eureka con servicios registrados | http://localhost:8761 → deben verse `API-GATEWAY`, `PACIENTES-SERVICE`, `CITAS-SERVICE`, `NOTIFICACIONES-SERVICE`, `AUDITORIA-SERVICE`                                          |
| 2   | Gateway enruta                   | Los `curl` del README usan solo el puerto 8080; capture la respuesta del paso 1 y 4                                                                                             |
| 3   | Reglas de negocio                | Capturas de: duplicado (409), paciente inexistente (404), transición inválida (409), validación (400)                                                                           |
| 4   | Persistencia SQL + R2DBC         | `docker exec -it <postgres> psql -U $DB_USER -d citas_db -c "select * from cita;"` y lo mismo con `pacientes_db`                                                                |
| 5   | RabbitMQ publicado/consumido     | http://localhost:15672 → _Exchanges_ → `hospital.eventos`; _Queues_ → `notificaciones.cola` y `auditoria.cola`; y los logs "Evento publicado…" / "NOTIFICACION…" / "AUDITORIA…" |
| 6   | Pruebas automatizadas            | Terminal con `mvn verify` mostrando `BUILD SUCCESS` y `Tests run: …, Failures: 0`                                                                                               |
| 7   | GitHub Actions exitoso           | Pestaña _Actions_ → ejecución verde (adjuntar log del paso "Compilar y ejecutar pruebas")                                                                                       |
| 8   | Fallo controlado + corrección    | Ver procedimiento abajo                                                                                                                                                         |

## Ciclo de CI: fallo controlado (endpoint POST /api/pacientes)

```bash
git checkout -b prueba/fallo-ci
# Rompa la regla del endpoint a proposito: en
# pacientes-service/.../service/PacienteService.java, metodo registrar(),
# cambie HttpStatus.CONFLICT por HttpStatus.BAD_REQUEST
git commit -am "test: provocar fallo controlado de CI"
git push -u origin prueba/fallo-ci
# Abra un Pull Request hacia main -> Actions queda en ROJO
# (falla RegistroPacienteEndpointTest.registroDuplicadoDevuelve409: esperaba 409 y recibio 400)
# Corrija: vuelva a HttpStatus.CONFLICT
git commit -am "fix: restaurar 409 en paciente duplicado"
git push          # Actions queda en VERDE
```
