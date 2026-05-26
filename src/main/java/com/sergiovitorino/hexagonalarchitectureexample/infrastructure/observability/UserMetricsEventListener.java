package com.sergiovitorino.hexagonalarchitectureexample.infrastructure.observability;

import com.sergiovitorino.hexagonalarchitectureexample.application.event.UserCreatedEvent;
import com.sergiovitorino.hexagonalarchitectureexample.application.event.UserDeletedEvent;
import com.sergiovitorino.hexagonalarchitectureexample.application.event.UserUpdatedEvent;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

/**
 * Listener que incrementa métricas Micrometer após o commit da transação.
 * Usar @TransactionalEventListener(AFTER_COMMIT) garante que o contador só
 * sobe se a transação foi persistida com sucesso, evitando métricas infladas
 * por rollbacks.
 */
@Component
public class UserMetricsEventListener {

    private final UserMetrics metrics;

    public UserMetricsEventListener(UserMetrics metrics) {
        this.metrics = metrics;
    }

    // fallbackExecution = true garante que o listener também dispara quando o evento
    // é publicado fora de uma transação ativa (ex: seeds, testes de integração com H2).
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT, fallbackExecution = true)
    public void onUserCreated(UserCreatedEvent event) {
        metrics.onUserCreated();
    }

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT, fallbackExecution = true)
    public void onUserUpdated(UserUpdatedEvent event) {
        metrics.onUserUpdated();
    }

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT, fallbackExecution = true)
    public void onUserDeleted(UserDeletedEvent event) {
        metrics.onUserDeleted();
    }
}
