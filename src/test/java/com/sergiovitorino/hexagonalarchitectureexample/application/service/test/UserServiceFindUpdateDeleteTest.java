package com.sergiovitorino.hexagonalarchitectureexample.application.service.test;

import com.sergiovitorino.hexagonalarchitectureexample.application.service.UserService;
import com.sergiovitorino.hexagonalarchitectureexample.domain.exception.UserNotFoundException;
import com.sergiovitorino.hexagonalarchitectureexample.domain.model.User;
import com.sergiovitorino.hexagonalarchitectureexample.domain.repository.UserRepositoryPort;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class UserServiceFindUpdateDeleteTest {

    @Mock
    private UserRepositoryPort repository;

    @InjectMocks
    private UserService service;

    @Test
    public void findById_existing_returnsUser() {
        var id = UUID.randomUUID();
        var user = new User(id, "Alice");
        when(repository.findById(id)).thenReturn(Optional.of(user));

        var result = service.findById(id);

        assertEquals(id, result.getId());
        assertEquals("Alice", result.getName());
    }

    @Test
    public void findById_missing_throwsUserNotFoundException() {
        var id = UUID.randomUUID();
        when(repository.findById(id)).thenReturn(Optional.empty());

        var ex = assertThrows(UserNotFoundException.class, () -> service.findById(id));
        assertTrue(ex.getMessage().contains(id.toString()));
    }

    @Test
    public void update_existing_updatesNameAndSaves() {
        var id = UUID.randomUUID();
        var user = new User(id, "OldName");
        when(repository.findById(id)).thenReturn(Optional.of(user));
        when(repository.save(any(User.class))).thenAnswer(inv -> inv.getArgument(0));

        var result = service.update(id, "NewName");

        assertEquals("NewName", result.getName());
        verify(repository).save(user);
    }

    @Test
    public void update_missing_throwsUserNotFoundException() {
        var id = UUID.randomUUID();
        when(repository.findById(id)).thenReturn(Optional.empty());

        assertThrows(UserNotFoundException.class, () -> service.update(id, "NewName"));
    }

    @Test
    public void delete_existing_callsPortDeleteById() {
        var id = UUID.randomUUID();
        var user = new User(id, "Alice");
        when(repository.findById(id)).thenReturn(Optional.of(user));

        service.delete(id);

        verify(repository).deleteById(id);
    }

    @Test
    public void delete_missing_throwsUserNotFoundException() {
        var id = UUID.randomUUID();
        when(repository.findById(id)).thenReturn(Optional.empty());

        assertThrows(UserNotFoundException.class, () -> service.delete(id));
        verify(repository, never()).deleteById(any());
    }
}
