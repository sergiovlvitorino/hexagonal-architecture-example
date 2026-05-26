package com.sergiovitorino.hexagonalarchitectureexample.application.service.test;

import com.sergiovitorino.hexagonalarchitectureexample.application.event.UserCreatedEvent;
import com.sergiovitorino.hexagonalarchitectureexample.application.service.UserService;
import com.sergiovitorino.hexagonalarchitectureexample.domain.model.User;
import com.sergiovitorino.hexagonalarchitectureexample.domain.repository.UserRepositoryPort;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class UserServiceTest {

    @Mock
    private UserRepositoryPort repository;

    @Mock
    private ApplicationEventPublisher eventPublisher;

    @InjectMocks
    private UserService service;

    @Test
    public void testFindAllReturnsPaginatedResults() {
        var user1 = new User("Alice");
        user1.setId(UUID.randomUUID());
        var user2 = new User("Bob");
        user2.setId(UUID.randomUUID());
        Page<User> expectedPage = new PageImpl<>(List.of(user1, user2));

        when(repository.findAll(isNull(), any(Pageable.class))).thenReturn(expectedPage);

        var result = service.findAll(0, 10, "name", true, null);

        assertEquals(expectedPage, result);
        assertEquals(2, result.getContent().size());
        verify(repository).findAll(isNull(), any(Pageable.class));
    }

    @Test
    public void testFindAllWithFilterPassesUserNameToRepository() {
        when(repository.findAll(any(String.class), any(Pageable.class))).thenReturn(Page.empty());

        service.findAll(0, 10, "name", true, "John");

        ArgumentCaptor<String> userNameCaptor = ArgumentCaptor.forClass(String.class);
        verify(repository).findAll(userNameCaptor.capture(), any(Pageable.class));

        assertEquals("John", userNameCaptor.getValue());
    }

    @Test
    public void testFindAllAscendingSorting() {
        when(repository.findAll(isNull(), any(Pageable.class))).thenReturn(Page.empty());

        service.findAll(0, 10, "name", true, null);

        var pageableCaptor = ArgumentCaptor.forClass(Pageable.class);
        verify(repository).findAll(isNull(), pageableCaptor.capture());

        var sort = pageableCaptor.getValue().getSort();
        var order = sort.getOrderFor("name");
        assertNotNull(order);
        assertEquals(Sort.Direction.ASC, order.getDirection());
    }

    @Test
    public void testFindAllDescendingSorting() {
        when(repository.findAll(isNull(), any(Pageable.class))).thenReturn(Page.empty());

        service.findAll(0, 10, "name", false, null);

        var pageableCaptor = ArgumentCaptor.forClass(Pageable.class);
        verify(repository).findAll(isNull(), pageableCaptor.capture());

        var sort = pageableCaptor.getValue().getSort();
        var order = sort.getOrderFor("name");
        assertNotNull(order);
        assertEquals(Sort.Direction.DESC, order.getDirection());
    }

    @Test
    public void testSaveDelegatesToRepositoryAndPublishesCreatedEvent() {
        var user = new User("TestUser");
        var savedUser = new User("TestUser");
        savedUser.setId(UUID.randomUUID());

        when(repository.save(user)).thenReturn(savedUser);

        var result = service.save(user);

        assertEquals(savedUser, result);
        assertNotNull(result.getId());
        verify(repository).save(user);

        ArgumentCaptor<UserCreatedEvent> eventCaptor = ArgumentCaptor.forClass(UserCreatedEvent.class);
        verify(eventPublisher).publishEvent(eventCaptor.capture());
        assertEquals(savedUser.getId(), eventCaptor.getValue().id());
    }
}
