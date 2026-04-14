package com.sergiovitorino.hexagonalarchitectureexample.infrastructure.test;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.http.*;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class WebConfigCorsTest {

    @Autowired
    private TestRestTemplate restTemplate;

    @LocalServerPort
    private Integer port;

    private String baseUrl() {
        return "http://localhost:" + port + "/rest/user?pageNumber=0&pageSize=10&orderBy=name&asc=true";
    }

    @Test
    public void testPreflightRequest_allowedOrigin_returnsCorsHeaders() {
        // Arrange: preflight OPTIONS com origin permitida
        var headers = new HttpHeaders();
        headers.add("Origin", "http://localhost:8080");
        headers.add("Access-Control-Request-Method", "GET");
        headers.add("Access-Control-Request-Headers", "Content-Type");

        var entity = new HttpEntity<String>(null, headers);

        // Act
        var response = restTemplate.exchange(baseUrl(), HttpMethod.OPTIONS, entity, String.class);

        // Assert: Spring responde 200 (ou 204) e inclui o header ACAO com a origin permitida
        assertTrue(
            response.getStatusCode() == HttpStatus.OK || response.getStatusCode() == HttpStatus.NO_CONTENT,
            "Preflight deve retornar 200 ou 204, recebeu: " + response.getStatusCode()
        );
        var acaoHeader = response.getHeaders().getFirst("Access-Control-Allow-Origin");
        assertNotNull(acaoHeader, "Access-Control-Allow-Origin deve estar presente para origin permitida");
        assertEquals("http://localhost:8080", acaoHeader);
    }

    @Test
    public void testGetRequest_allowedOrigin_returnsCorsHeader() {
        // Arrange: GET real com origin permitida
        var headers = new HttpHeaders();
        headers.add("Origin", "http://localhost:8080");

        var entity = new HttpEntity<String>(null, headers);

        // Act
        var response = restTemplate.exchange(baseUrl(), HttpMethod.GET, entity, String.class);

        // Assert: resposta OK e header ACAO presente
        assertEquals(HttpStatus.OK, response.getStatusCode());
        var acaoHeader = response.getHeaders().getFirst("Access-Control-Allow-Origin");
        assertNotNull(acaoHeader, "Access-Control-Allow-Origin deve estar presente para origin permitida");
        assertEquals("http://localhost:8080", acaoHeader);
    }

    @Test
    public void testGetRequest_disallowedOrigin_noCorsHeader() {
        // Arrange: GET com origin nao permitida
        var headers = new HttpHeaders();
        headers.add("Origin", "http://evil.com");

        var entity = new HttpEntity<String>(null, headers);

        // Act
        var response = restTemplate.exchange(baseUrl(), HttpMethod.GET, entity, String.class);

        // Assert: requisicao pode retornar 200 (Spring nao bloqueia no servidor),
        // mas o header Access-Control-Allow-Origin NAO deve estar presente
        var acaoHeader = response.getHeaders().getFirst("Access-Control-Allow-Origin");
        assertNull(acaoHeader, "Access-Control-Allow-Origin NAO deve estar presente para origin nao permitida");
    }

}
