package com.sergiovitorino.hexagonalarchitectureexample.application.service.test;

import com.sergiovitorino.hexagonalarchitectureexample.application.event.UserDeletedEvent;
import com.sergiovitorino.hexagonalarchitectureexample.application.event.UserUpdatedEvent;
import com.sergiovitorino.hexagonalarchitectureexample.application.service.UserService;
import com.sergiovitorino.hexagonalarchitectureexample.domain.exception.UserNotFoundException;
import com.sergiovitorino.hexagonalarchitectureexample.domain.model.User;
import com.sergiovitorino.hexagonalarchitectureexample.domain.repository.UserRepositoryPort;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;

import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class UserServiceFindUpdateDeleteTest {

    @Mock
    private UserRepositoryPort repository;

    @Mock
    private ApplicationEventPublisher eventPublisher;

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
    public void update_existing_updatesNameAndSavesAndPublishesEvent() {
        var id = UUID.randomUUID();
        var user = new User(id, "OldName");
        when(repository.findById(id)).thenReturn(Optional.of(user));
        when(repository.save(any(User.class))).thenAnswer(inv -> inv.getArgument(0));

        var result = service.update(id, "NewName");

        assertEquals("NewName", result.getName());
        verify(repository).save(user);

        ArgumentCaptor<UserUpdatedEvent> eventCaptor = ArgumentCaptor.forClass(UserUpdatedEvent.class);
        verify(eventPublisher).publishEvent(eventCaptor.capture());
        assertEquals(id, eventCaptor.getValue().id());
    }

    @Test
    public void update_missing_throwsUserNotFoundException() {
        var id = UUID.randomUUID();
        when(repository.findById(id)).thenReturn(Optional.empty());

        assertThrows(UserNotFoundException.class, () -> service.update(id, "NewName"));
    }

    @Test
    public void delete_existing_callsPortDeleteByIdAndPublishesEvent() {
        var id = UUID.randomUUID();
        when(repository.existsById(id)).thenReturn(true);

        service.delete(id);

        var inOrder = inOrder(repository);
        inOrder.verify(repository).existsById(id);
        inOrder.verify(repository).deleteById(id);

        ArgumentCaptor<UserDeletedEvent> eventCaptor = ArgumentCaptor.forClass(UserDeletedEvent.class);
        verify(eventPublisher).publishEvent(eventCaptor.capture());
        assertEquals(id, eventCaptor.getValue().id());
    }

    @Test
    public void delete_missing_throwsUserNotFoundException() {
        var id = UUID.randomUUID();
        when(repository.existsById(id)).thenReturn(false);

        assertThrows(UserNotFoundException.class, () -> service.delete(id));
        verify(repository, never()).deleteById(any());
    }
}
