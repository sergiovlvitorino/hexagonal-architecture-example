package com.sergiovitorino.hexagonalarchitectureexample.ui.rest.test;

import com.atlassian.oai.validator.mockmvc.OpenApiValidationMatchers;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.sergiovitorino.hexagonalarchitectureexample.application.command.UserCommandHandler;
import com.sergiovitorino.hexagonalarchitectureexample.application.command.user.DeleteCommand;
import com.sergiovitorino.hexagonalarchitectureexample.application.command.user.ListCommand;
import com.sergiovitorino.hexagonalarchitectureexample.application.command.user.SaveCommand;
import com.sergiovitorino.hexagonalarchitectureexample.application.command.user.UpdateCommand;
import com.sergiovitorino.hexagonalarchitectureexample.application.validation.SafeHtmlValidator;
import com.sergiovitorino.hexagonalarchitectureexample.domain.exception.DomainValidationException;
import com.sergiovitorino.hexagonalarchitectureexample.domain.exception.UserNotFoundException;
import com.sergiovitorino.hexagonalarchitectureexample.domain.model.User;
import com.sergiovitorino.hexagonalarchitectureexample.infrastructure.exception.GlobalExceptionHandler;
import com.sergiovitorino.hexagonalarchitectureexample.ui.rest.UserRestController;
import com.sergiovitorino.hexagonalarchitectureexample.ui.rest.generated.dto.SaveUserRequest;
import com.sergiovitorino.hexagonalarchitectureexample.ui.rest.generated.dto.UpdateUserRequest;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Contract tests: validates every REST endpoint response against the OpenAPI spec
 * at {@code classpath:openapi/users.yaml}. If the implementation diverges from the
 * spec (status code, body shape, headers), the build fails.
 */
@WebMvcTest(UserRestController.class)
@Import({GlobalExceptionHandler.class, SafeHtmlValidator.class})
public class UserRestContractTest {

    private static final String SPEC = "openapi/users.yaml";

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper mapper;

    @MockitoBean
    private UserCommandHandler commandHandler;

    private User user(String name) {
        var u = new User(name);
        u.setId(UUID.randomUUID());
        return u;
    }

    @Test
    public void listUsers_200_matchesSpec() throws Exception {
        var page = new PageImpl<>(List.of(user("Alice"), user("Bob")), PageRequest.of(0, 20), 2L);
        when(commandHandler.handle(any(ListCommand.class))).thenReturn(page);

        mockMvc.perform(get("/rest/user")
                        .param("pageNumber", "0")
                        .param("pageSize", "20")
                        .param("orderBy", "name")
                        .param("asc", "true"))
                .andExpect(status().isOk())
                .andExpect(OpenApiValidationMatchers.openApi().isValid(SPEC));
    }

    @Test
    public void createUser_201_matchesSpec() throws Exception {
        var saved = user("AliceX");
        when(commandHandler.handle(any(SaveCommand.class))).thenReturn(saved);

        var body = mapper.writeValueAsString(new SaveUserRequest("AliceX"));

        mockMvc.perform(post("/rest/user")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isCreated())
                .andExpect(OpenApiValidationMatchers.openApi().isValid(SPEC));
    }

    @Test
    public void findUserById_200_matchesSpec() throws Exception {
        var u = user("Charlie");
        when(commandHandler.findById(u.getId())).thenReturn(u);

        mockMvc.perform(get("/rest/user/" + u.getId()))
                .andExpect(status().isOk())
                .andExpect(OpenApiValidationMatchers.openApi().isValid(SPEC));
    }

    @Test
    public void findUserById_404_matchesSpec() throws Exception {
        var id = UUID.randomUUID();
        when(commandHandler.findById(id)).thenThrow(new UserNotFoundException(id));

        mockMvc.perform(get("/rest/user/" + id))
                .andExpect(status().isNotFound())
                .andExpect(OpenApiValidationMatchers.openApi().isValid(SPEC));
    }

    @Test
    public void updateUser_200_matchesSpec() throws Exception {
        var id = UUID.randomUUID();
        when(commandHandler.handle(any(UpdateCommand.class))).thenReturn(new User(id, "RenamedX"));

        var body = mapper.writeValueAsString(new UpdateUserRequest("RenamedX"));

        mockMvc.perform(put("/rest/user/" + id)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isOk())
                .andExpect(OpenApiValidationMatchers.openApi().isValid(SPEC));
    }

    @Test
    public void updateUser_404_matchesSpec() throws Exception {
        var id = UUID.randomUUID();
        when(commandHandler.handle(any(UpdateCommand.class))).thenThrow(new UserNotFoundException(id));

        var body = mapper.writeValueAsString(new UpdateUserRequest("RenamedX"));

        mockMvc.perform(put("/rest/user/" + id)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isNotFound())
                .andExpect(OpenApiValidationMatchers.openApi().isValid(SPEC));
    }

    @Test
    public void deleteUser_204_matchesSpec() throws Exception {
        var id = UUID.randomUUID();

        mockMvc.perform(delete("/rest/user/" + id))
                .andExpect(status().isNoContent())
                .andExpect(OpenApiValidationMatchers.openApi().isValid(SPEC));
    }

    @Test
    public void deleteUser_404_matchesSpec() throws Exception {
        var id = UUID.randomUUID();
        doThrow(new UserNotFoundException(id)).when(commandHandler).handle(any(DeleteCommand.class));

        mockMvc.perform(delete("/rest/user/" + id))
                .andExpect(status().isNotFound())
                .andExpect(OpenApiValidationMatchers.openApi().isValid(SPEC));
    }

    /**
     * Contract test for POST 400 with HTML payload.
     *
     * Decision: {@code pattern '^[^<>]*$'} in the generated {@code SaveUserRequest} DTO rejects
     * {@code <html>} at the Spring MVC validation layer ({@code @Valid} on the generated interface),
     * before the request ever reaches the mock handler. This surfaces as {@code MethodArgumentNotValidException}
     * → 400 with {@code ValidationErrors} shape. The mock is not invoked.
     * <p>
     * This behaviour is by design: the generated DTO enforces the spec constraint server-side (defense-in-depth),
     * and the response shape is validated via jsonPath against the {@code ValidationErrors} variant of
     * {@code ErrorResponse} (one of the {@code oneOf} branches in {@code users.yaml}).
     * See ADR-0007 for full rationale.
     */
    @Test
    public void post_400_withHtmlPayload_matchesSpec() throws Exception {
        mockMvc.perform(post("/rest/user")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\":\"<html>x</html>\"}"))
                .andExpect(status().isBadRequest())
                // MethodArgumentNotValidException from pattern constraint fires before the mock handler.
                // Validates ValidationErrors shape: {"errors":[{"field":"...","message":"..."}]}
                // — the ValidationErrors variant of ErrorResponse in users.yaml.
                .andExpect(jsonPath("$.errors").isArray())
                .andExpect(jsonPath("$.errors[0].field").exists())
                .andExpect(jsonPath("$.errors[0].message").exists())
                .andExpect(jsonPath("$.error").doesNotExist());
    }

    @Test
    public void listUsers_400_matchesSpec() throws Exception {
        // Spec-valid request (all params conform to schema), but the server rejects it
        // with a DomainValidationException -> 400. Validates the 400 body shape.
        when(commandHandler.handle(any(ListCommand.class)))
                .thenThrow(new DomainValidationException("orderBy must be one of: [id, name]"));

        mockMvc.perform(get("/rest/user")
                        .param("pageNumber", "0")
                        .param("pageSize", "20")
                        .param("orderBy", "name")
                        .param("asc", "true"))
                .andExpect(status().isBadRequest())
                .andExpect(OpenApiValidationMatchers.openApi().isValid(SPEC));
    }
}
