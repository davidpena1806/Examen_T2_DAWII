package com.cibertec.hospital.gateway;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cloud.gateway.route.Route;
import org.springframework.cloud.gateway.route.RouteLocator;

@SpringBootTest(properties = {"eureka.client.enabled=false", "spring.cloud.discovery.enabled=false"})
class GatewayRoutesTests {

    @Autowired
    RouteLocator routeLocator;

    @Test
    void estanDefinidasLasRutasHaciaLosMicroservicios() {
        List<Route> rutas = routeLocator.getRoutes().collectList().block();
        assertThat(rutas).extracting(Route::getId)
                .contains("pacientes", "citas", "notificaciones", "auditoria");
        assertThat(rutas).filteredOn(r -> r.getId().equals("citas"))
                .allSatisfy(r -> assertThat(r.getUri().toString()).isEqualTo("lb://citas-service"));
    }
}
