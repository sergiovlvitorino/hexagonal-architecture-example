package com.sergiovitorino.hexagonalarchitectureexample.infrastructure.observability;

import io.micrometer.core.instrument.Counter;
import org.springframework.stereotype.Component;

/**
 * Métricas customizadas para operações de usuário.
 */
@Component
public class UserMetrics {

    private final Counter usersCreated;
    private final Counter usersUpdated;
    private final Counter usersDeleted;

    public UserMetrics(io.micrometer.core.instrument.MeterRegistry registry) {
        this.usersCreated = Counter.builder("users_created_total")
                .description("Total de usuários criados")
                .register(registry);
        this.usersUpdated = Counter.builder("users_updated_total")
                .description("Total de usuários atualizados")
                .register(registry);
        this.usersDeleted = Counter.builder("users_deleted_total")
                .description("Total de usuários deletados")
                .register(registry);
    }

    public void onUserCreated() {
        usersCreated.increment();
    }

    public void onUserUpdated() {
        usersUpdated.increment();
    }

    public void onUserDeleted() {
        usersDeleted.increment();
    }
}
