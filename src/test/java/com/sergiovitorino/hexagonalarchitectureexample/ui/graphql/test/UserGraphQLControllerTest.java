package com.sergiovitorino.hexagonalarchitectureexample.ui.graphql.test;

import com.fasterxml.jackson.databind.ObjectMapper;
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

import static org.junit.Assert.*;

@RunWith(SpringRunner.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class UserGraphQLControllerTest {

    @Autowired private ObjectMapper mapper;
    @Autowired private TestRestTemplate restTemplete;
    @LocalServerPort private Integer port;

    @Test
    public void testIfListCommandIsOk() throws Exception{
        String body = "{\"query\":\"query{\\n  findAll(pageNumber:0, pageSize:10000, orderBy: \\\"name\\\", asc: true){\\n    content{\\n      id,\\n      name\\n    },\\n    totalElements\\n  }\\n}\",\"variables\":null}";
        HttpHeaders headers = new HttpHeaders();
        headers.add("Content-Type", MediaType.APPLICATION_JSON_UTF8_VALUE);
        final HttpEntity<String> entity = new HttpEntity<>(body, headers);
        final ResponseEntity<String> responseEntity = this.restTemplete.exchange("http://localhost:" + port + "/graphql/user", HttpMethod.POST, entity, String.class);
        assertEquals(HttpStatus.OK, responseEntity.getStatusCode());
        final JSONObject jsonObject = new JSONObject(responseEntity.getBody());
        final JSONObject dataJSONObject = jsonObject.getJSONObject("data");
        final JSONObject findAllJSONObject = dataJSONObject.getJSONObject("findAll");
        final List<User> users = mapper.readValue(findAllJSONObject.getString("content"), mapper.getTypeFactory().constructParametricType(List.class, User.class));
        final Integer totalElements = findAllJSONObject.getInt("totalElements");
        assertNotNull(users);
        assertFalse(users.isEmpty());
        assertTrue(totalElements > 0);
    }

}
