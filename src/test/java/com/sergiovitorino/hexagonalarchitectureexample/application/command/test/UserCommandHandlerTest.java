package com.sergiovitorino.hexagonalarchitectureexample.application.command.test;

import com.sergiovitorino.hexagonalarchitectureexample.application.command.UserCommandHandler;
import com.sergiovitorino.hexagonalarchitectureexample.application.command.user.ListCommand;
import com.sergiovitorino.hexagonalarchitectureexample.application.command.user.SaveCommand;
import com.sergiovitorino.hexagonalarchitectureexample.application.service.UserService;
import com.sergiovitorino.hexagonalarchitectureexample.domain.model.User;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class UserCommandHandlerTest {

    @Mock
    private UserService service;

    @InjectMocks
    private UserCommandHandler handler;

    @Test
    public void testHandleListCommandDelegatesToService() {
        var user = new User("Alice");
        var command = new ListCommand(0, 10, "name", true, user);

        when(service.findAll(0, 10, "name", true, user)).thenReturn(Page.empty());

        handler.handle(command);

        verify(service).findAll(0, 10, "name", true, user);
    }

    @Test
    public void testHandleListCommandWithNullUserCreatesEmptyUser() {
        var command = new ListCommand(0, 10, "name", true, null);

        when(service.findAll(anyInt(), anyInt(), anyString(), anyBoolean(), any(User.class))).thenReturn(Page.empty());

        handler.handle(command);

        var userCaptor = ArgumentCaptor.forClass(User.class);
        verify(service).findAll(eq(0), eq(10), eq("name"), eq(true), userCaptor.capture());

        var capturedUser = userCaptor.getValue();
        assertNotNull(capturedUser);
        assertNull(capturedUser.getName());
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

        when(service.findAll(anyInt(), anyInt(), anyString(), anyBoolean(), any(User.class))).thenReturn(Page.empty());

        var result = handler.handle(command);

        assertNotNull(result);
        verify(service).findAll(eq(0), eq(10), eq("id"), eq(true), any(User.class));
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
}
