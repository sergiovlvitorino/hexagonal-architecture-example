package com.sergiovitorino.hexagonalarchitectureexample.infrastructure.observability;

import com.sergiovitorino.hexagonalarchitectureexample.application.event.UserCreatedEvent;
import com.sergiovitorino.hexagonalarchitectureexample.application.event.UserDeletedEvent;
import com.sergiovitorino.hexagonalarchitectureexample.application.event.UserUpdatedEvent;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.UUID;

import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class UserMetricsEventListenerTest {

    @Mock
    private UserMetrics metrics;

    @InjectMocks
    private UserMetricsEventListener listener;

    @Test
    void onUserCreated_incrementsCreatedCounter() {
        listener.onUserCreated(new UserCreatedEvent(UUID.randomUUID()));
        verify(metrics).onUserCreated();
    }

    @Test
    void onUserUpdated_incrementsUpdatedCounter() {
        listener.onUserUpdated(new UserUpdatedEvent(UUID.randomUUID()));
        verify(metrics).onUserUpdated();
    }

    @Test
    void onUserDeleted_incrementsDeletedCounter() {
        listener.onUserDeleted(new UserDeletedEvent(UUID.randomUUID()));
        verify(metrics).onUserDeleted();
    }
}
