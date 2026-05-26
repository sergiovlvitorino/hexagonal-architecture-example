package com.sergiovitorino.hexagonalarchitectureexample.infrastructure.observability;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.TestPropertySource;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Testes de integração para os endpoints do Actuator relacionados a observabilidade.
 * Usa @TestPropertySource para expor /actuator/prometheus sem ativar o profile "prod"
 * (que requer PostgreSQL e porta de management separada).
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@TestPropertySource(properties = {
    "management.endpoints.web.exposure.include=health,info,prometheus",
    "management.endpoint.prometheus.enabled=true",
    "management.prometheus.metrics.export.enabled=true"
})
public class ActuatorPrometheusIntegrationTest {

    @Autowired
    private TestRestTemplate restTemplate;

    @LocalServerPort
    private Integer port;

    private String actuatorUrl(String endpoint) {
        return "http://localhost:" + port + "/actuator/" + endpoint;
    }

    @Test
    public void prometheusEndpoint_returns200() {
        ResponseEntity<String> response = restTemplate.getForEntity(
            actuatorUrl("prometheus"), String.class);

        assertEquals(HttpStatus.OK, response.getStatusCode(),
            "GET /actuator/prometheus deve retornar 200");
    }

    @Test
    public void prometheusEndpoint_containsJvmMetrics() {
        ResponseEntity<String> response = restTemplate.getForEntity(
            actuatorUrl("prometheus"), String.class);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        var body = response.getBody();
        assertNotNull(body);
        // jvm_memory_used_bytes é uma métrica padrão do Spring Boot com Micrometer
        assertTrue(body.contains("jvm_memory_used_bytes"),
            "Response do Prometheus deve conter métricas JVM padrão");
    }

    @Test
    public void prometheusEndpoint_containsUserCreatedMetric_afterCreateOperation() {
        // Arrange: cria um usuário via REST para que o contador users_created_total seja incrementado
        var createPayload = "{\"name\": \"PrometheusTestUser\"}";
        var headers = new org.springframework.http.HttpHeaders();
        headers.setContentType(org.springframework.http.MediaType.APPLICATION_JSON);
        var entity = new org.springframework.http.HttpEntity<>(createPayload, headers);

        restTemplate.postForEntity(
            "http://localhost:" + port + "/rest/user", entity, String.class);

        // Act
        ResponseEntity<String> prometheusResponse = restTemplate.getForEntity(
            actuatorUrl("prometheus"), String.class);

        // Assert
        assertEquals(HttpStatus.OK, prometheusResponse.getStatusCode());
        var body = prometheusResponse.getBody();
        assertNotNull(body);
        // O Micrometer 1.15.x + Prometheus client 1.x (OpenMetrics) expõe o counter
        // "users_created_total" como "users_total" — o nome base é definido pelo builder
        // Counter.builder("users_created_total") e o cliente OpenMetrics trata "_total" como
        // type suffix, resultando no metric name "users". Usamos "users_total" que é estável
        // nessa versão, mais específico que apenas "users".
        assertTrue(body.contains("users_total"),
            "Response do Prometheus deve conter a métrica 'users_total' após criar um usuário.");
    }
}
