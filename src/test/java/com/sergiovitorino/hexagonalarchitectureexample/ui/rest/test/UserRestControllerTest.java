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
    @Autowired private TestRestTemplate restTemplete;
    @LocalServerPort private Integer port;

    @Test
    public void testIfListCommandReturnsOk() throws Exception {
        final var entity = new HttpEntity<String>(null, null);
        final var responseEntity = this.restTemplete.exchange("http://localhost:" + port + "/rest/user?pageNumber=0&pageSize=10000&orderBy=name&asc=true", HttpMethod.GET, entity, String.class);
        final var jsonObject = new JSONObject(responseEntity.getBody());
        final List<User> users = mapper.readValue(jsonObject.getString("content"), mapper.getTypeFactory().constructParametricType(List.class, User.class));
        assertEquals(HttpStatus.OK, responseEntity.getStatusCode());
        assertNotNull(users);
        assertFalse(users.isEmpty());
    }

    @Test
    public void testIfListCommandReturnsOk2() throws Exception {
        final var entity = new HttpEntity<String>(null, null);
        final var responseEntity = this.restTemplete.exchange("http://localhost:" + port + "/rest/user?pageNumber=0&pageSize=10000&orderBy=name&asc=false", HttpMethod.GET, entity, String.class);
        final var jsonObject = new JSONObject(responseEntity.getBody());
        final List<User> users = mapper.readValue(jsonObject.getString("content"), mapper.getTypeFactory().constructParametricType(List.class, User.class));
        assertEquals(HttpStatus.OK, responseEntity.getStatusCode());
        assertNotNull(users);
        assertFalse(users.isEmpty());
    }

    @Test
    public void testIfListCommandReturnsOk3() throws Exception {
        final var entity = new HttpEntity<String>(null, null);
        final var responseEntity = this.restTemplete.exchange("http://localhost:" + port + "/rest/user?pageNumber=0&pageSize=10000&orderBy=name&asc=true&user.name=111", HttpMethod.GET, entity, String.class);
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
        final var responseEntity = this.restTemplete.exchange("http://localhost:" + port + "/rest/user", HttpMethod.POST, entity, String.class);
        assertEquals(HttpStatus.CREATED, responseEntity.getStatusCode());
    }

    @Test
    public void testIfSaveCommandReturnsBadRequest() throws Exception {
        final var command = new SaveCommand("<html>test</html>");

        final var headers = new HttpHeaders();
        headers.add("Content-Type", MediaType.APPLICATION_JSON_VALUE);

        final var httpEntity = new HttpEntity<>(mapper.writeValueAsString(command), headers);
        final var responseEntity = this.restTemplete.exchange("http://localhost:" + port + "/rest/user", HttpMethod.POST, httpEntity, String.class);
        assertEquals(HttpStatus.BAD_REQUEST, responseEntity.getStatusCode());
    }

}
