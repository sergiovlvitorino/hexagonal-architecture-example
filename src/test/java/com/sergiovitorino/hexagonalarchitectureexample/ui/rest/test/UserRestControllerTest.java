package com.sergiovitorino.hexagonalarchitectureexample.ui.rest.test;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.sergiovitorino.hexagonalarchitectureexample.application.command.UserCommandHandler;
import com.sergiovitorino.hexagonalarchitectureexample.application.command.user.DeleteCommand;
import com.sergiovitorino.hexagonalarchitectureexample.application.command.user.ListCommand;
import com.sergiovitorino.hexagonalarchitectureexample.application.command.user.SaveCommand;
import com.sergiovitorino.hexagonalarchitectureexample.application.command.user.UpdateCommand;
import com.sergiovitorino.hexagonalarchitectureexample.domain.exception.DomainValidationException;
import com.sergiovitorino.hexagonalarchitectureexample.domain.exception.UserNotFoundException;
import com.sergiovitorino.hexagonalarchitectureexample.domain.model.User;
import com.sergiovitorino.hexagonalarchitectureexample.infrastructure.exception.GlobalExceptionHandler;
import com.sergiovitorino.hexagonalarchitectureexample.application.validation.SafeHtmlValidator;
import com.sergiovitorino.hexagonalarchitectureexample.ui.rest.UserRestController;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.data.domain.Page;
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
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(UserRestController.class)
@Import({GlobalExceptionHandler.class, SafeHtmlValidator.class})
public class UserRestControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper mapper;

    @MockitoBean
    private UserCommandHandler commandHandler;

    // Cria um User com id definido para simular retorno do repositório
    private User createUser(String name) {
        var user = new User(name);
        user.setId(UUID.randomUUID());
        return user;
    }

    @Test
    public void testIfListCommandReturnsOk() throws Exception {
        var users = List.of(createUser("Alice"), createUser("Bob"));
        when(commandHandler.handle(any(ListCommand.class)))
                .thenReturn(new PageImpl<>(users));

        mockMvc.perform(get("/rest/user")
                        .param("pageNumber", "0")
                        .param("pageSize", "100")
                        .param("orderBy", "name")
                        .param("asc", "true"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isArray())
                .andExpect(jsonPath("$.content").isNotEmpty());
    }

    @Test
    public void testIfListCommandWithDescendingSortReturnsOk() throws Exception {
        var users = List.of(createUser("Bob"), createUser("Alice"));
        when(commandHandler.handle(any(ListCommand.class)))
                .thenReturn(new PageImpl<>(users));

        mockMvc.perform(get("/rest/user")
                        .param("pageNumber", "0")
                        .param("pageSize", "100")
                        .param("orderBy", "name")
                        .param("asc", "false"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isArray())
                .andExpect(jsonPath("$.content").isNotEmpty());
    }

    @Test
    public void testIfListCommandWithNonExistentFilterReturnsEmptyList() throws Exception {
        when(commandHandler.handle(any(ListCommand.class)))
                .thenReturn(Page.empty());

        mockMvc.perform(get("/rest/user")
                        .param("pageNumber", "0")
                        .param("pageSize", "100")
                        .param("orderBy", "name")
                        .param("asc", "true")
                        .param("userName", "111"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isArray())
                .andExpect(jsonPath("$.content").isEmpty());
    }

    @Test
    public void testIfPaginationWithPageNumberOneIsOk() throws Exception {
        // Retorna página com 2 itens de um total de 6, pageSize=2
        var users = List.of(createUser("Alice"), createUser("Bob"));
        var pageable = PageRequest.of(0, 2);
        when(commandHandler.handle(any(ListCommand.class)))
                .thenReturn(new PageImpl<>(users, pageable, 6L));

        mockMvc.perform(get("/rest/user")
                        .param("pageNumber", "0")
                        .param("pageSize", "2")
                        .param("orderBy", "name")
                        .param("asc", "true"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.size").value(2))
                .andExpect(jsonPath("$.totalPages").value(3));
    }

    @Test
    public void testIfListCommandWithInvalidOrderByReturnsBadRequest() throws Exception {
        when(commandHandler.handle(any(ListCommand.class)))
                .thenThrow(new DomainValidationException("orderBy must be one of: [id, name]"));

        mockMvc.perform(get("/rest/user")
                        .param("pageNumber", "0")
                        .param("pageSize", "10")
                        .param("orderBy", "email")
                        .param("asc", "true"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").exists())
                .andExpect(jsonPath("$.error").value("orderBy must be one of: [id, name]"));
    }

    @Test
    public void testIfListCommandWithOrderByIdReturnsOk() throws Exception {
        var users = List.of(createUser("Alice"), createUser("Bob"));
        when(commandHandler.handle(any(ListCommand.class)))
                .thenReturn(new PageImpl<>(users));

        mockMvc.perform(get("/rest/user")
                        .param("pageNumber", "0")
                        .param("pageSize", "100")
                        .param("orderBy", "id")
                        .param("asc", "true"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isArray());
    }

    @Test
    public void testPaginationMetadataIsCorrect() throws Exception {
        var users = List.of(createUser("Alice"), createUser("Bob"));
        var pageable = PageRequest.of(0, 2);
        when(commandHandler.handle(any(ListCommand.class)))
                .thenReturn(new PageImpl<>(users, pageable, 8L));

        mockMvc.perform(get("/rest/user")
                        .param("pageNumber", "0")
                        .param("pageSize", "2")
                        .param("orderBy", "name")
                        .param("asc", "true"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.number").value(0))
                .andExpect(jsonPath("$.size").value(2))
                .andExpect(jsonPath("$.totalPages").value(4))
                .andExpect(jsonPath("$.totalElements").value(8));
    }

    @Test
    public void testIfSaveCommandIsOk() throws Exception {
        var user = createUser("My First Name");
        when(commandHandler.handle(any(SaveCommand.class)))
                .thenReturn(user);

        var body = mapper.writeValueAsString(new SaveCommand("My First Name"));

        mockMvc.perform(post("/rest/user")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").exists())
                .andExpect(jsonPath("$.name").value("My First Name"));
    }

    @Test
    public void testIfSaveCommandReturnsBadRequest() throws Exception {
        // Bean Validation rejeita antes de chegar ao commandHandler — não configura mock
        var body = mapper.writeValueAsString(new SaveCommand("<html>test</html>"));

        mockMvc.perform(post("/rest/user")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors").exists());
    }

    @Test
    public void testIfSaveCommandWithShortNameReturnsBadRequest() throws Exception {
        var body = mapper.writeValueAsString(new SaveCommand("ab"));

        mockMvc.perform(post("/rest/user")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors").exists());
    }

    @Test
    public void testIfSaveCommandWithImgTagReturnsBadRequest() throws Exception {
        var body = mapper.writeValueAsString(new SaveCommand("<img src=x onerror=alert(1)>"));

        mockMvc.perform(post("/rest/user")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isBadRequest());
    }

    @Test
    public void testIfSaveCommandWithNullNameReturnsBadRequest() throws Exception {
        mockMvc.perform(post("/rest/user")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\": null}"))
                .andExpect(status().isBadRequest());
    }

    @Test
    public void testIfSaveCommandWithLongNameReturnsBadRequest() throws Exception {
        var body = mapper.writeValueAsString(new SaveCommand("A".repeat(101)));

        mockMvc.perform(post("/rest/user")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors").exists());
    }

    @Test
    public void testIfSaveCommandWithExactMinLengthNameReturnsCreated() throws Exception {
        var user = createUser("Abcde");
        when(commandHandler.handle(any(SaveCommand.class)))
                .thenReturn(user);

        var body = mapper.writeValueAsString(new SaveCommand("Abcde"));

        mockMvc.perform(post("/rest/user")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.name").value("Abcde"));
    }

    @Test
    public void testIfSaveCommandWithExactMaxLengthNameReturnsCreated() throws Exception {
        var name = "A".repeat(100);
        var user = createUser(name);
        when(commandHandler.handle(any(SaveCommand.class)))
                .thenReturn(user);

        var body = mapper.writeValueAsString(new SaveCommand(name));

        mockMvc.perform(post("/rest/user")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.name").value(name));
    }

    // --- Testes de segurança HTTP ---

    // --- findById ---

    @Test
    public void findById_existing_returns200WithUserResponse() throws Exception {
        var user = createUser("Alice");
        when(commandHandler.findById(user.getId())).thenReturn(user);

        mockMvc.perform(get("/rest/user/" + user.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(user.getId().toString()))
                .andExpect(jsonPath("$.name").value("Alice"));
    }

    @Test
    public void findById_nonExisting_returns404() throws Exception {
        var id = UUID.randomUUID();
        when(commandHandler.findById(id)).thenThrow(new UserNotFoundException(id));

        mockMvc.perform(get("/rest/user/" + id))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.error").value("User not found: " + id));
    }

    @Test
    public void findById_invalidUuid_returns400() throws Exception {
        mockMvc.perform(get("/rest/user/not-a-uuid"))
                .andExpect(status().isBadRequest());
    }

    // --- update ---

    @Test
    public void update_existing_returns200WithUpdatedUser() throws Exception {
        var id = UUID.randomUUID();
        var updated = new User(id, "NewName");
        when(commandHandler.handle(any(UpdateCommand.class))).thenReturn(updated);

        var body = mapper.writeValueAsString(new SaveCommand("NewName"));
        mockMvc.perform(put("/rest/user/" + id)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("NewName"));
    }

    @Test
    public void update_nonExisting_returns404() throws Exception {
        var id = UUID.randomUUID();
        when(commandHandler.handle(any(UpdateCommand.class))).thenThrow(new UserNotFoundException(id));

        var body = mapper.writeValueAsString(new SaveCommand("NewName"));
        mockMvc.perform(put("/rest/user/" + id)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isNotFound());
    }

    @Test
    public void update_invalidName_returns400() throws Exception {
        var id = UUID.randomUUID();
        var body = mapper.writeValueAsString(new SaveCommand("ab"));

        mockMvc.perform(put("/rest/user/" + id)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isBadRequest());
    }

    @Test
    public void update_fourCharName_returns400() throws Exception {
        var id = UUID.randomUUID();

        mockMvc.perform(put("/rest/user/" + id)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\":\"abcd\"}"))
                .andExpect(status().isBadRequest());
    }

    @Test
    public void update_invalidUuid_returns400() throws Exception {
        var body = mapper.writeValueAsString(new SaveCommand("ValidName"));
        mockMvc.perform(put("/rest/user/not-a-uuid")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isBadRequest());
    }

    // --- delete ---

    @Test
    public void delete_existing_returns204() throws Exception {
        var id = UUID.randomUUID();
        // handle(DeleteCommand) retorna void — sem mock necessário

        mockMvc.perform(delete("/rest/user/" + id))
                .andExpect(status().isNoContent());
    }

    @Test
    public void delete_nonExisting_returns404() throws Exception {
        var id = UUID.randomUUID();
        doThrow(new UserNotFoundException(id)).when(commandHandler).handle(any(DeleteCommand.class));

        mockMvc.perform(delete("/rest/user/" + id))
                .andExpect(status().isNotFound());
    }

    @Test
    public void delete_invalidUuid_returns400() throws Exception {
        mockMvc.perform(delete("/rest/user/not-a-uuid"))
                .andExpect(status().isBadRequest());
    }

    @Test
    public void testPutOnUserEndpointReturnsMethodNotAllowed() throws Exception {
        mockMvc.perform(put("/rest/user")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isMethodNotAllowed());
    }

    @Test
    public void testDeleteOnUserEndpointReturnsMethodNotAllowed() throws Exception {
        mockMvc.perform(delete("/rest/user"))
                .andExpect(status().isMethodNotAllowed());
    }

    @Test
    public void testPatchOnUserEndpointReturnsMethodNotAllowed() throws Exception {
        mockMvc.perform(patch("/rest/user")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isMethodNotAllowed());
    }

    @Test
    public void testSqlInjectionInUserNameParamDoesNotCauseServerError() throws Exception {
        // Injection attempt no parâmetro userName — deve retornar 200 (filtro tratado como string normal)
        when(commandHandler.handle(any(ListCommand.class)))
                .thenReturn(Page.empty());

        mockMvc.perform(get("/rest/user")
                        .param("pageNumber", "0")
                        .param("pageSize", "10")
                        .param("orderBy", "name")
                        .param("asc", "true")
                        .param("userName", "' OR 1=1--"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isArray())
                .andExpect(jsonPath("$.content").isEmpty());
    }

}
