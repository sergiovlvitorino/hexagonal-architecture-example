package com.sergiovitorino.hexagonalarchitectureexample.infrastructure.test;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.http.HttpStatus;
import org.springframework.test.context.ActiveProfiles;

import static org.junit.jupiter.api.Assertions.assertEquals;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("prod")
public class OpenApiProdIntegrationTest {

    @LocalServerPort
    private int port;

    @Autowired
    private TestRestTemplate restTemplate;

    @Test
    public void apiDocs_returns404InProdProfile() {
        var response = restTemplate.getForEntity(
                "http://localhost:" + port + "/v3/api-docs", String.class);
        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
    }
}
