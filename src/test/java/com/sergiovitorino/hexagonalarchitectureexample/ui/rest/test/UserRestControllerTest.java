package com.sergiovitorino.hexagonalarchitectureexample.ui.rest.test;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.sergiovitorino.hexagonalarchitectureexample.application.command.user.SaveCommand;
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
public class UserRestControllerTest {

    @Autowired private ObjectMapper mapper;
    @Autowired private TestRestTemplate restTemplate;
    @LocalServerPort private Integer port;

    @Test
    public void testIfListCommandReturnsOk() throws Exception {
        final var entity = new HttpEntity<String>(null, null);
        final var responseEntity = this.restTemplate.exchange("http://localhost:" + port + "/rest/user?pageNumber=0&pageSize=100&orderBy=name&asc=true", HttpMethod.GET, entity, String.class);
        final var jsonObject = new JSONObject(responseEntity.getBody());
        final List<User> users = mapper.readValue(jsonObject.getString("content"), mapper.getTypeFactory().constructParametricType(List.class, User.class));
        assertEquals(HttpStatus.OK, responseEntity.getStatusCode());
        assertNotNull(users);
        assertFalse(users.isEmpty());
    }

    @Test
    public void testIfListCommandWithDescendingSortReturnsOk() throws Exception {
        final var entity = new HttpEntity<String>(null, null);
        final var responseEntity = this.restTemplate.exchange("http://localhost:" + port + "/rest/user?pageNumber=0&pageSize=100&orderBy=name&asc=false", HttpMethod.GET, entity, String.class);
        final var jsonObject = new JSONObject(responseEntity.getBody());
        final List<User> users = mapper.readValue(jsonObject.getString("content"), mapper.getTypeFactory().constructParametricType(List.class, User.class));
        assertEquals(HttpStatus.OK, responseEntity.getStatusCode());
        assertNotNull(users);
        assertFalse(users.isEmpty());
    }

    @Test
    public void testIfListCommandWithNonExistentFilterReturnsEmptyList() throws Exception {
        final var entity = new HttpEntity<String>(null, null);
        final var responseEntity = this.restTemplate.exchange("http://localhost:" + port + "/rest/user?pageNumber=0&pageSize=100&orderBy=name&asc=true&user.name=111", HttpMethod.GET, entity, String.class);
        final var jsonObject = new JSONObject(responseEntity.getBody());
        final List<User> users = mapper.readValue(jsonObject.getString("content"), mapper.getTypeFactory().constructParametricType(List.class, User.class));
        assertEquals(HttpStatus.OK, responseEntity.getStatusCode());
        assertNotNull(users);
        assertTrue(users.isEmpty());
    }

    @Test
    public void testIfSaveCommandIsOk() throws Exception {
        final var command = new SaveCommand("My First Name");

        final var headers = new HttpHeaders();
        headers.add("Content-Type", MediaType.APPLICATION_JSON_VALUE);

        final var entity = new HttpEntity<>(mapper.writeValueAsString(command), headers);
        final var responseEntity = this.restTemplate.exchange("http://localhost:" + port + "/rest/user", HttpMethod.POST, entity, String.class);
        assertEquals(HttpStatus.CREATED, responseEntity.getStatusCode());
    }

    @Test
    public void testIfSaveCommandReturnsBadRequest() throws Exception {
        final var command = new SaveCommand("<html>test</html>");

        final var headers = new HttpHeaders();
        headers.add("Content-Type", MediaType.APPLICATION_JSON_VALUE);

        final var httpEntity = new HttpEntity<>(mapper.writeValueAsString(command), headers);
        final var responseEntity = this.restTemplate.exchange("http://localhost:" + port + "/rest/user", HttpMethod.POST, httpEntity, String.class);
        assertEquals(HttpStatus.BAD_REQUEST, responseEntity.getStatusCode());

        final var jsonObject = new JSONObject(responseEntity.getBody());
        assertTrue(jsonObject.has("errors"));
    }

    @Test
    public void testIfSaveCommandWithShortNameReturnsBadRequest() throws Exception {
        final var command = new SaveCommand("ab");

        final var headers = new HttpHeaders();
        headers.add("Content-Type", MediaType.APPLICATION_JSON_VALUE);

        final var httpEntity = new HttpEntity<>(mapper.writeValueAsString(command), headers);
        final var responseEntity = this.restTemplate.exchange("http://localhost:" + port + "/rest/user", HttpMethod.POST, httpEntity, String.class);
        assertEquals(HttpStatus.BAD_REQUEST, responseEntity.getStatusCode());

        final var jsonObject = new JSONObject(responseEntity.getBody());
        assertTrue(jsonObject.has("errors"));
    }

    @Test
    public void testIfSaveCommandWithImgTagReturnsBadRequest() throws Exception {
        final var command = new SaveCommand("<img src=x onerror=alert(1)>");

        final var headers = new HttpHeaders();
        headers.add("Content-Type", MediaType.APPLICATION_JSON_VALUE);

        final var httpEntity = new HttpEntity<>(mapper.writeValueAsString(command), headers);
        final var responseEntity = this.restTemplate.exchange("http://localhost:" + port + "/rest/user", HttpMethod.POST, httpEntity, String.class);
        assertEquals(HttpStatus.BAD_REQUEST, responseEntity.getStatusCode());
    }

    @Test
    public void testIfSaveCommandWithNullNameReturnsBadRequest() throws Exception {
        final var headers = new HttpHeaders();
        headers.add("Content-Type", MediaType.APPLICATION_JSON_VALUE);

        final var httpEntity = new HttpEntity<>("{\"name\": null}", headers);
        final var responseEntity = this.restTemplate.exchange("http://localhost:" + port + "/rest/user", HttpMethod.POST, httpEntity, String.class);
        assertEquals(HttpStatus.BAD_REQUEST, responseEntity.getStatusCode());
    }

    @Test
    public void testIfSaveCommandWithLongNameReturnsBadRequest() throws Exception {
        final var command = new SaveCommand("A".repeat(101));

        final var headers = new HttpHeaders();
        headers.add("Content-Type", MediaType.APPLICATION_JSON_VALUE);

        final var httpEntity = new HttpEntity<>(mapper.writeValueAsString(command), headers);
        final var responseEntity = this.restTemplate.exchange("http://localhost:" + port + "/rest/user", HttpMethod.POST, httpEntity, String.class);
        assertEquals(HttpStatus.BAD_REQUEST, responseEntity.getStatusCode());
    }

    @Test
    public void testIfPaginationWithPageNumberOneIsOk() throws Exception {
        final var entity = new HttpEntity<String>(null, null);
        final var responseEntity = this.restTemplate.exchange("http://localhost:" + port + "/rest/user?pageNumber=0&pageSize=2&orderBy=name&asc=true", HttpMethod.GET, entity, String.class);
        final var jsonObject = new JSONObject(responseEntity.getBody());
        assertEquals(HttpStatus.OK, responseEntity.getStatusCode());
        assertEquals(2, jsonObject.getInt("size"));
        assertTrue(jsonObject.getInt("totalPages") > 1);
    }

    @Test
    public void testIfListCommandWithInvalidOrderByReturnsBadRequest() throws Exception {
        final var entity = new HttpEntity<String>(null, null);
        final var responseEntity = this.restTemplate.exchange("http://localhost:" + port + "/rest/user?pageNumber=0&pageSize=10&orderBy=email&asc=true", HttpMethod.GET, entity, String.class);
        assertEquals(HttpStatus.BAD_REQUEST, responseEntity.getStatusCode());

        final var jsonObject = new JSONObject(responseEntity.getBody());
        assertTrue(jsonObject.has("error"));
        assertTrue(jsonObject.getString("error").contains("orderBy must be one of"));
    }

    @Test
    public void testIfListCommandWithOrderByIdReturnsOk() throws Exception {
        final var entity = new HttpEntity<String>(null, null);
        final var responseEntity = this.restTemplate.exchange("http://localhost:" + port + "/rest/user?pageNumber=0&pageSize=100&orderBy=id&asc=true", HttpMethod.GET, entity, String.class);
        final var jsonObject = new JSONObject(responseEntity.getBody());
        assertEquals(HttpStatus.OK, responseEntity.getStatusCode());
        assertNotNull(jsonObject.getJSONArray("content"));
    }

    @Test
    public void testIfSaveCommandWithExactMinLengthNameReturnsCreated() throws Exception {
        final var command = new SaveCommand("Abcde");

        final var headers = new HttpHeaders();
        headers.add("Content-Type", MediaType.APPLICATION_JSON_VALUE);

        final var entity = new HttpEntity<>(mapper.writeValueAsString(command), headers);
        final var responseEntity = this.restTemplate.exchange("http://localhost:" + port + "/rest/user", HttpMethod.POST, entity, String.class);
        assertEquals(HttpStatus.CREATED, responseEntity.getStatusCode());
    }

    @Test
    public void testIfSaveCommandWithExactMaxLengthNameReturnsCreated() throws Exception {
        final var command = new SaveCommand("A".repeat(100));

        final var headers = new HttpHeaders();
        headers.add("Content-Type", MediaType.APPLICATION_JSON_VALUE);

        final var entity = new HttpEntity<>(mapper.writeValueAsString(command), headers);
        final var responseEntity = this.restTemplate.exchange("http://localhost:" + port + "/rest/user", HttpMethod.POST, entity, String.class);
        assertEquals(HttpStatus.CREATED, responseEntity.getStatusCode());
    }

    @Test
    public void testPaginationMetadataIsCorrect() throws Exception {
        final var entity = new HttpEntity<String>(null, null);
        final var responseEntity = this.restTemplate.exchange("http://localhost:" + port + "/rest/user?pageNumber=0&pageSize=2&orderBy=name&asc=true", HttpMethod.GET, entity, String.class);
        final var jsonObject = new JSONObject(responseEntity.getBody());

        assertEquals(HttpStatus.OK, responseEntity.getStatusCode());
        assertEquals(0, jsonObject.getInt("number"));
        assertEquals(2, jsonObject.getInt("size"));
        assertTrue(jsonObject.getInt("totalPages") >= 3);
        assertTrue(jsonObject.getInt("totalElements") >= 6);
    }

}
