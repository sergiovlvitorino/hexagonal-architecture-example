package com.sergiovitorino.hexagonalarchitectureexample.infrastructure.graphql;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.*;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class GraphQlExceptionResolverIntegrationTest {

    @Autowired
    private TestRestTemplate restTemplate;

    @Test
    public void findById_nonExisting_returnsNotFoundClassification() {
        var id = UUID.randomUUID();
        var query = "{\"query\":\"{ findById(id: \\\"%s\\\") { id name } }\"}".formatted(id);

        var headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        var entity = new HttpEntity<>(query, headers);

        var response = restTemplate.exchange("/graphql", HttpMethod.POST, entity, String.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).contains("NOT_FOUND");
    }
}
