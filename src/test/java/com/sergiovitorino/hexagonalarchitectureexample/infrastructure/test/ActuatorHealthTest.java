package com.sergiovitorino.hexagonalarchitectureexample.infrastructure.test;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.http.HttpStatus;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class ActuatorHealthTest {

    @Autowired
    private TestRestTemplate restTemplate;

    @LocalServerPort
    private Integer port;

    @Test
    public void testHealthEndpointReturnsUp() {
        var responseEntity = restTemplate.getForEntity(
                "http://localhost:" + port + "/actuator/health", String.class);
        assertEquals(HttpStatus.OK, responseEntity.getStatusCode());
        assertTrue(responseEntity.getBody().contains("UP"));
    }

    @Test
    public void testNonExposedEndpointReturns404() {
        var responseEntity = restTemplate.getForEntity(
                "http://localhost:" + port + "/actuator/env", String.class);
        assertEquals(HttpStatus.NOT_FOUND, responseEntity.getStatusCode());
    }
}
