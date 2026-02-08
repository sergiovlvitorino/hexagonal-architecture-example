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
                    "query": "{ findAll(pageNumber: 0, pageSize: 100, orderBy: \\"name\\", asc: true) { content { id name } totalElements } }"
                }
                """;

        final var jsonObject = executeGraphQL(query);
        final var findAllObject = jsonObject.getJSONObject("data").getJSONObject("findAll");
        final List<User> users = mapper.readValue(
                findAllObject.getJSONArray("content").toString(),
                mapper.getTypeFactory().constructParametricType(List.class, User.class));
        final var totalElements = findAllObject.getInt("totalElements");

        assertNotNull(users);
        assertFalse(users.isEmpty());
        assertTrue(totalElements > 0);
    }

    @Test
    public void testIfListCommandWithUserNameFilterIsOk() throws Exception {
        var query = """
                {
                    "query": "{ findAll(pageNumber: 0, pageSize: 100, orderBy: \\"name\\", asc: true, userName: \\"nonexistent\\") { content { id name } totalElements } }"
                }
                """;

        final var jsonObject = executeGraphQL(query);
        final var findAllObject = jsonObject.getJSONObject("data").getJSONObject("findAll");
        final var totalElements = findAllObject.getInt("totalElements");

        assertEquals(0, totalElements);
    }

    @Test
    public void testIfNegativePageNumberReturnsError() throws Exception {
        var query = """
                {
                    "query": "{ findAll(pageNumber: -1, pageSize: 10, orderBy: \\"name\\", asc: true) { content { id name } totalElements } }"
                }
                """;

        final var jsonObject = executeGraphQL(query);
        assertTrue(jsonObject.has("errors"));
        assertFalse(jsonObject.has("data") && !jsonObject.isNull("data"));
    }

    @Test
    public void testIfPageSizeExceedingLimitReturnsError() throws Exception {
        var query = """
                {
                    "query": "{ findAll(pageNumber: 0, pageSize: 5000, orderBy: \\"name\\", asc: true) { content { id name } totalElements } }"
                }
                """;

        final var jsonObject = executeGraphQL(query);
        assertTrue(jsonObject.has("errors"));
    }

    @Test
    public void testIfPageSizeZeroReturnsError() throws Exception {
        var query = """
                {
                    "query": "{ findAll(pageNumber: 0, pageSize: 0, orderBy: \\"name\\", asc: true) { content { id name } totalElements } }"
                }
                """;

        final var jsonObject = executeGraphQL(query);
        assertTrue(jsonObject.has("errors"));
    }

    @Test
    public void testIfInvalidOrderByReturnsError() throws Exception {
        var query = """
                {
                    "query": "{ findAll(pageNumber: 0, pageSize: 10, orderBy: \\"email\\", asc: true) { content { id name } totalElements } }"
                }
                """;

        final var jsonObject = executeGraphQL(query);
        assertTrue(jsonObject.has("errors"));
    }

    @Test
    public void testIfOrderByIdIsOk() throws Exception {
        var query = """
                {
                    "query": "{ findAll(pageNumber: 0, pageSize: 10, orderBy: \\"id\\", asc: true) { content { id name } totalElements } }"
                }
                """;

        final var jsonObject = executeGraphQL(query);
        final var findAllObject = jsonObject.getJSONObject("data").getJSONObject("findAll");
        assertTrue(findAllObject.getInt("totalElements") > 0);
    }

    private JSONObject executeGraphQL(String query) throws Exception {
        var headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        final var entity = new HttpEntity<>(query, headers);
        final var responseEntity = this.restTemplate.exchange(
                "http://localhost:" + port + "/graphql", HttpMethod.POST, entity, String.class);

        assertEquals(HttpStatus.OK, responseEntity.getStatusCode());
        return new JSONObject(responseEntity.getBody());
    }

}
