package com.sergiovitorino.hexagonalarchitectureexample.infrastructure.persistence.test;

import com.sergiovitorino.hexagonalarchitectureexample.domain.model.User;
import com.sergiovitorino.hexagonalarchitectureexample.infrastructure.persistence.UserEntity;
import com.sergiovitorino.hexagonalarchitectureexample.infrastructure.persistence.UserRepository;
import com.sergiovitorino.hexagonalarchitectureexample.infrastructure.persistence.UserRepositoryAdapter;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Example;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class UserRepositoryAdapterTest {

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private UserRepositoryAdapter adapter;

    @Test
    @SuppressWarnings("unchecked")
    public void testFindAllWithNullUserNameDoesNotFilterByName() {
        var pageable = PageRequest.of(0, 10);
        var entity = new UserEntity(UUID.randomUUID(), "Alice");
        when(userRepository.findAll(any(Example.class), eq(pageable)))
                .thenReturn(new PageImpl<>(List.of(entity)));

        var result = adapter.findAll(null, pageable);

        ArgumentCaptor<Example<UserEntity>> captor = ArgumentCaptor.forClass(Example.class);
        verify(userRepository).findAll(captor.capture(), eq(pageable));
        assertNull(captor.getValue().getProbe().getName());
        assertEquals(1, result.getContent().size());
        assertInstanceOf(User.class, result.getContent().get(0));
    }

    @Test
    @SuppressWarnings("unchecked")
    public void testFindAllWithUserNameFiltersCorrectly() {
        var pageable = PageRequest.of(0, 10);
        var entity = new UserEntity(UUID.randomUUID(), "Alice");
        when(userRepository.findAll(any(Example.class), eq(pageable)))
                .thenReturn(new PageImpl<>(List.of(entity)));

        var result = adapter.findAll("Alice", pageable);

        ArgumentCaptor<Example<UserEntity>> captor = ArgumentCaptor.forClass(Example.class);
        verify(userRepository).findAll(captor.capture(), eq(pageable));
        assertEquals("Alice", captor.getValue().getProbe().getName());
        assertEquals("Alice", result.getContent().get(0).getName());
    }

    @Test
    @SuppressWarnings("unchecked")
    public void testFindAllReturnsMappedDomainObjects() {
        var pageable = PageRequest.of(0, 10);
        var id = UUID.randomUUID();
        var entity = new UserEntity(id, "Bob");
        when(userRepository.findAll(any(Example.class), eq(pageable)))
                .thenReturn(new PageImpl<>(List.of(entity)));

        var result = adapter.findAll(null, pageable);

        var user = result.getContent().get(0);
        assertEquals(id, user.getId());
        assertEquals("Bob", user.getName());
    }

    @Test
    public void testSaveConvertsAndPersists() {
        var user = new User("Charlie");
        var savedEntity = new UserEntity(UUID.randomUUID(), "Charlie");
        when(userRepository.save(any(UserEntity.class))).thenReturn(savedEntity);

        var result = adapter.save(user);

        assertNotNull(result.getId());
        assertEquals("Charlie", result.getName());
        verify(userRepository).save(any(UserEntity.class));
    }

    @Test
    public void testSavePassesCorrectEntityToRepository() {
        var id = UUID.randomUUID();
        var user = new User(id, "Diana");
        var savedEntity = new UserEntity(id, "Diana");
        when(userRepository.save(any(UserEntity.class))).thenReturn(savedEntity);

        adapter.save(user);

        var captor = ArgumentCaptor.forClass(UserEntity.class);
        verify(userRepository).save(captor.capture());
        assertEquals(id, captor.getValue().getId());
        assertEquals("Diana", captor.getValue().getName());
    }
}
