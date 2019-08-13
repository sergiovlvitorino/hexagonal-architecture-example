package com.sergiovitorino.hexagonalarchitectureexample.ui.rest.test;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.sergiovitorino.hexagonalarchitectureexample.application.command.user.SaveCommand;
import com.sergiovitorino.hexagonalarchitectureexample.domain.model.User;
import org.json.JSONObject;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.web.server.LocalServerPort;
import org.springframework.http.*;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.List;
import java.util.UUID;

import static org.junit.Assert.*;

@RunWith(SpringRunner.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class UserRestControllerTest {

    @Autowired private ObjectMapper mapper;
    @Autowired private TestRestTemplate restTemplete;
    @LocalServerPort private Integer port;

    @Test
    public void testIfListCommandReturnsOk() throws Exception {
        final HttpEntity<String> entity = new HttpEntity<String>(null, null);
        final ResponseEntity<String> responseEntity = this.restTemplete.exchange("http://localhost:" + port + "/rest/user?pageNumber=0&pageSize=10000&orderBy=name&asc=true", HttpMethod.GET, entity, String.class);
        final JSONObject jsonObject = new JSONObject(responseEntity.getBody());
        final List<User> users = mapper.readValue(jsonObject.getString("content"), mapper.getTypeFactory().constructParametricType(List.class, User.class));
        assertEquals(HttpStatus.OK, responseEntity.getStatusCode());
        assertNotNull(users);
        assertFalse(users.isEmpty());
    }

    @Test
    public void testIfListCommandReturnsOk2() throws Exception {
        final HttpEntity<String> entity = new HttpEntity<String>(null, null);
        final ResponseEntity<String> responseEntity = this.restTemplete.exchange("http://localhost:" + port + "/rest/user?pageNumber=0&pageSize=10000&orderBy=name&asc=false", HttpMethod.GET, entity, String.class);
        final JSONObject jsonObject = new JSONObject(responseEntity.getBody());
        final List<User> users = mapper.readValue(jsonObject.getString("content"), mapper.getTypeFactory().constructParametricType(List.class, User.class));
        assertEquals(HttpStatus.OK, responseEntity.getStatusCode());
        assertNotNull(users);
        assertFalse(users.isEmpty());
    }

    @Test
    public void testIfListCommandReturnsOk3() throws Exception {
        final HttpEntity<String> entity = new HttpEntity<String>(null, null);
        final ResponseEntity<String> responseEntity = this.restTemplete.exchange("http://localhost:" + port + "/rest/user?pageNumber=0&pageSize=10000&orderBy=name&asc=true&user.name=111", HttpMethod.GET, entity, String.class);
        final JSONObject jsonObject = new JSONObject(responseEntity.getBody());
        final List<User> users = mapper.readValue(jsonObject.getString("content"), mapper.getTypeFactory().constructParametricType(List.class, User.class));
        assertEquals(HttpStatus.OK, responseEntity.getStatusCode());
        assertNotNull(users);
        assertTrue(users.isEmpty());
    }

    @Test
    public void testIfSaveCommandIsOk() throws Exception {
        final SaveCommand command = new SaveCommand();
        command.setName("My First Name");

        final HttpHeaders headers = new HttpHeaders();
        headers.add("Content-Type", MediaType.APPLICATION_JSON_UTF8_VALUE);

        final HttpEntity<String> entity = new HttpEntity<String>(mapper.writeValueAsString(command), headers);
        final ResponseEntity<String> responseEntity = this.restTemplete.exchange("http://localhost:" + port + "/rest/user", HttpMethod.POST, entity, String.class);
        assertEquals(HttpStatus.CREATED, responseEntity.getStatusCode());
    }

    @Test
    public void testIfSaveCommandReturnsBadRequest() throws Exception {
        final SaveCommand command = new SaveCommand();
        command.setName("<html>test</html>");

        final HttpHeaders headers = new HttpHeaders();
        headers.add("Content-Type", MediaType.APPLICATION_JSON_UTF8_VALUE);

        final HttpEntity<String> httpEntity = new HttpEntity<String>(mapper.writeValueAsString(command), headers);
        final ResponseEntity<String> responseEntity = this.restTemplete.exchange("http://localhost:" + port + "/rest/user", HttpMethod.POST, httpEntity, String.class);
        assertEquals(HttpStatus.BAD_REQUEST, responseEntity.getStatusCode());
    }

}
