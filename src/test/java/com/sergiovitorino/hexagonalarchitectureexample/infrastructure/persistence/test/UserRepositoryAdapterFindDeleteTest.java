package com.sergiovitorino.hexagonalarchitectureexample.infrastructure.persistence.test;

import com.sergiovitorino.hexagonalarchitectureexample.infrastructure.persistence.UserEntity;
import com.sergiovitorino.hexagonalarchitectureexample.infrastructure.persistence.UserRepository;
import com.sergiovitorino.hexagonalarchitectureexample.infrastructure.persistence.UserRepositoryAdapter;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class UserRepositoryAdapterFindDeleteTest {

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private UserRepositoryAdapter adapter;

    @Test
    public void findById_existingId_returnsOptionalWithUser() {
        var id = UUID.randomUUID();
        var entity = new UserEntity(id, "Alice");
        when(userRepository.findById(id)).thenReturn(Optional.of(entity));

        var result = adapter.findById(id);

        assertTrue(result.isPresent());
        assertEquals(id, result.get().getId());
        assertEquals("Alice", result.get().getName());
    }

    @Test
    public void findById_nonExistingId_returnsEmptyOptional() {
        var id = UUID.randomUUID();
        when(userRepository.findById(id)).thenReturn(Optional.empty());

        var result = adapter.findById(id);

        assertTrue(result.isEmpty());
    }

    @Test
    public void deleteById_callsUnderlyingRepository() {
        var id = UUID.randomUUID();

        adapter.deleteById(id);

        verify(userRepository).deleteById(id);
    }
}
