package com.sergiovitorino.hexagonalarchitectureexample.infrastructure.test;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.http.HttpStatus;
import static org.junit.jupiter.api.Assertions.assertEquals;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class OpenApiIntegrationTest {

    @LocalServerPort
    private int port;

    @Autowired
    private TestRestTemplate restTemplate;

    /**
     * Verifies that the static OpenAPI spec is accessible at /openapi/users.yaml.
     * springdoc api-docs generation is disabled (see ADR-0006); the spec file is served
     * as a static resource and Swagger UI is configured to load it directly.
     */
    @Test
    public void openApiSpec_servedAsStaticResource() {
        var response = restTemplate.getForEntity(
                "http://localhost:" + port + "/openapi/users.yaml", String.class);
        assertEquals(HttpStatus.OK, response.getStatusCode());
    }
}
