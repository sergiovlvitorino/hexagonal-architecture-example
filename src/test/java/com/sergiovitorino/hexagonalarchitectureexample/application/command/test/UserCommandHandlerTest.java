package com.sergiovitorino.hexagonalarchitectureexample.application.command.test;

import com.sergiovitorino.hexagonalarchitectureexample.application.command.UserCommandHandler;
import com.sergiovitorino.hexagonalarchitectureexample.application.command.user.ListCommand;
import com.sergiovitorino.hexagonalarchitectureexample.application.command.user.SaveCommand;
import com.sergiovitorino.hexagonalarchitectureexample.application.service.UserService;
import com.sergiovitorino.hexagonalarchitectureexample.domain.model.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class UserCommandHandlerTest {

    private static final int MAX_PAGE_SIZE = 1000;

    @Mock
    private UserService service;

    private UserCommandHandler handler;

    @BeforeEach
    void setUp() {
        handler = new UserCommandHandler(service, MAX_PAGE_SIZE);
    }

    @Test
    public void testHandleListCommandDelegatesToService() {
        var command = new ListCommand(0, 10, "name", true, "Alice");

        when(service.findAll(anyInt(), anyInt(), anyString(), anyBoolean(), anyString())).thenReturn(Page.empty());

        handler.handle(command);

        verify(service).findAll(eq(0), eq(10), eq("name"), eq(true), eq("Alice"));
    }

    @Test
    public void testHandleListCommandWithNullUserCreatesEmptyUser() {
        var command = new ListCommand(0, 10, "name", true, null);

        when(service.findAll(anyInt(), anyInt(), anyString(), anyBoolean(), isNull())).thenReturn(Page.empty());

        handler.handle(command);

        verify(service).findAll(eq(0), eq(10), eq("name"), eq(true), isNull());
    }

    @Test
    public void testHandleListCommandWithInvalidOrderByThrowsException() {
        var command = new ListCommand(0, 10, "email", true, null);

        var exception = assertThrows(IllegalArgumentException.class, () -> handler.handle(command));
        assertTrue(exception.getMessage().contains("orderBy must be one of"));
    }

    @Test
    public void testHandleListCommandWithValidOrderByIdSucceeds() {
        var command = new ListCommand(0, 10, "id", true, null);

        when(service.findAll(anyInt(), anyInt(), anyString(), anyBoolean(), isNull())).thenReturn(Page.empty());

        var result = handler.handle(command);

        assertNotNull(result);
        verify(service).findAll(eq(0), eq(10), eq("id"), eq(true), isNull());
    }

    @Test
    public void testHandleSaveCommandCreatesUserWithName() {
        var command = new SaveCommand("TestName");
        var savedUser = new User("TestName");

        when(service.save(any(User.class))).thenReturn(savedUser);

        handler.handle(command);

        var userCaptor = ArgumentCaptor.forClass(User.class);
        verify(service).save(userCaptor.capture());

        assertEquals("TestName", userCaptor.getValue().getName());
    }

    @Test
    public void testHandleListCommandWithNegativePageNumberThrowsException() {
        var command = new ListCommand(-1, 10, "name", true, null);

        var exception = assertThrows(IllegalArgumentException.class, () -> handler.handle(command));
        assertEquals("pageNumber must be >= 0", exception.getMessage());
    }

    @Test
    public void testHandleListCommandWithZeroPageSizeThrowsException() {
        var command = new ListCommand(0, 0, "name", true, null);

        var exception = assertThrows(IllegalArgumentException.class, () -> handler.handle(command));
        assertEquals("pageSize must be between 1 and " + MAX_PAGE_SIZE, exception.getMessage());
    }

    @Test
    public void testHandleListCommandWithPageSizeExceedingLimitThrowsException() {
        var command = new ListCommand(0, MAX_PAGE_SIZE + 1, "name", true, null);

        var exception = assertThrows(IllegalArgumentException.class, () -> handler.handle(command));
        assertEquals("pageSize must be between 1 and " + MAX_PAGE_SIZE, exception.getMessage());
    }

    @Test
    public void testHandleListCommandWithBlankOrderByThrowsException() {
        var command = new ListCommand(0, 10, "  ", true, null);

        var exception = assertThrows(IllegalArgumentException.class, () -> handler.handle(command));
        assertEquals("orderBy must not be blank", exception.getMessage());
    }

    @Test
    public void testHandleListCommandWithNullAscThrowsException() {
        var command = new ListCommand(0, 10, "name", null, null);

        var exception = assertThrows(IllegalArgumentException.class, () -> handler.handle(command));
        assertEquals("asc must not be null", exception.getMessage());
    }

    @Test
    public void testHandleListCommandWithMinPageSizeOneSucceeds() {
        // pageSize=1 é o mínimo válido — boundary mutant: pageSize < 1 -> pageSize <= 1
        var command = new ListCommand(0, 1, "name", true, null);
        when(service.findAll(anyInt(), anyInt(), anyString(), anyBoolean(), isNull())).thenReturn(Page.empty());

        var result = handler.handle(command);

        assertNotNull(result);
        verify(service).findAll(eq(0), eq(1), eq("name"), eq(true), isNull());
    }

    @Test
    public void testHandleListCommandWithMaxPageSizeSucceeds() {
        // pageSize=maxPageSize é o máximo válido — boundary mutant: pageSize > max -> pageSize >= max
        var command = new ListCommand(0, MAX_PAGE_SIZE, "name", true, null);
        when(service.findAll(anyInt(), anyInt(), anyString(), anyBoolean(), isNull())).thenReturn(Page.empty());

        var result = handler.handle(command);

        assertNotNull(result);
        verify(service).findAll(eq(0), eq(MAX_PAGE_SIZE), eq("name"), eq(true), isNull());
    }

    @Test
    public void testHandleSaveCommandReturnsNonNullUser() {
        // Garante que o retorno do handle(SaveCommand) não é null — mata NullReturnValsMutator
        var command = new SaveCommand("TestName");
        var savedUser = new User("TestName");
        when(service.save(any(User.class))).thenReturn(savedUser);

        var result = handler.handle(command);

        assertNotNull(result);
        assertEquals("TestName", result.getName());
    }
}
