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
