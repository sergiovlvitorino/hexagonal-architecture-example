package com.sergiovitorino.hexagonalarchitectureexample.infrastructure.observability;

import io.micrometer.core.instrument.simple.SimpleMeterRegistry;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class UserMetricsTest {

    private SimpleMeterRegistry registry;
    private UserMetrics userMetrics;

    @BeforeEach
    void setUp() {
        registry = new SimpleMeterRegistry();
        userMetrics = new UserMetrics(registry);
    }

    @Test
    public void onUserCreated_incrementsCounter() {
        // Arrange: contador começa em 0
        double before = registry.counter("users_created_total").count();

        // Act
        userMetrics.onUserCreated();

        // Assert
        assertEquals(before + 1.0, registry.counter("users_created_total").count(), 0.001);
    }

    @Test
    public void onUserUpdated_incrementsCounter() {
        double before = registry.counter("users_updated_total").count();

        userMetrics.onUserUpdated();

        assertEquals(before + 1.0, registry.counter("users_updated_total").count(), 0.001);
    }

    @Test
    public void onUserDeleted_incrementsCounter() {
        double before = registry.counter("users_deleted_total").count();

        userMetrics.onUserDeleted();

        assertEquals(before + 1.0, registry.counter("users_deleted_total").count(), 0.001);
    }

    @Test
    public void counters_haveCorrectName() {
        // Verifica que os três contadores estão registrados com os nomes esperados
        var meters = registry.getMeters();

        // Após construção, os contadores já estão registrados no registry
        assertTrue(
            meters.stream().anyMatch(m -> m.getId().getName().equals("users_created_total")),
            "Deve existir contador 'users_created_total'"
        );
        assertTrue(
            meters.stream().anyMatch(m -> m.getId().getName().equals("users_updated_total")),
            "Deve existir contador 'users_updated_total'"
        );
        assertTrue(
            meters.stream().anyMatch(m -> m.getId().getName().equals("users_deleted_total")),
            "Deve existir contador 'users_deleted_total'"
        );
    }

    @Test
    public void onUserCreated_multipleIncrements_accumulatesCount() {
        // Garante que múltiplos incrementos acumulam corretamente
        userMetrics.onUserCreated();
        userMetrics.onUserCreated();
        userMetrics.onUserCreated();

        assertEquals(3.0, registry.counter("users_created_total").count(), 0.001);
    }

    @Test
    public void counters_areIndependent() {
        // Incrementar um contador não deve afetar os outros
        userMetrics.onUserCreated();

        assertEquals(1.0, registry.counter("users_created_total").count(), 0.001);
        assertEquals(0.0, registry.counter("users_updated_total").count(), 0.001);
        assertEquals(0.0, registry.counter("users_deleted_total").count(), 0.001);
    }
}
