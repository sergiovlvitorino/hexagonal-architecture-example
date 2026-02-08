package com.sergiovitorino.hexagonalarchitectureexample.ui.graphql.test;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.sergiovitorino.hexagonalarchitectureexample.domain.model.User;
import org.json.JSONObject;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.http.*;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class UserGraphQLControllerTest {

    @Autowired private ObjectMapper mapper;
    @Autowired private TestRestTemplate restTemplate;
    @LocalServerPort private Integer port;

    @Test
    public void testIfListCommandIsOk() throws Exception {
        var query = """
                {
                    "query": "{ findAll(pageNumber: 0, pageSize: 10000, orderBy: \\"name\\", asc: true) { content { id name } totalElements } }"
                }
                """;

        var headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        final var entity = new HttpEntity<>(query, headers);
        final var responseEntity = this.restTemplate.exchange(
                "http://localhost:" + port + "/graphql", HttpMethod.POST, entity, String.class);

        assertEquals(HttpStatus.OK, responseEntity.getStatusCode());

        final var jsonObject = new JSONObject(responseEntity.getBody());
        final var dataObject = jsonObject.getJSONObject("data");
        final var findAllObject = dataObject.getJSONObject("findAll");
        final List<User> users = mapper.readValue(
                findAllObject.getJSONArray("content").toString(),
                mapper.getTypeFactory().constructParametricType(List.class, User.class));
        final var totalElements = findAllObject.getInt("totalElements");

        assertNotNull(users);
        assertFalse(users.isEmpty());
        assertTrue(totalElements > 0);
    }

}
