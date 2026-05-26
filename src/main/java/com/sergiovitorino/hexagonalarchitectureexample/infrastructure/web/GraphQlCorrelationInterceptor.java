package com.sergiovitorino.hexagonalarchitectureexample.infrastructure.web;

import org.slf4j.MDC;
import org.springframework.graphql.server.WebGraphQlInterceptor;
import org.springframework.graphql.server.WebGraphQlRequest;
import org.springframework.graphql.server.WebGraphQlResponse;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

/**
 * Interceptor GraphQL que propaga o correlation-id no MDC para toda requisição GraphQL.
 * A validação do header é delegada a {@link CorrelationIdFilter#sanitize(String)},
 * garantindo a mesma proteção contra log injection que o filtro HTTP.
 */
@Component
public class GraphQlCorrelationInterceptor implements WebGraphQlInterceptor {

    private static final String CORRELATION_ID_HEADER = "X-Correlation-Id";
    private static final String CORRELATION_ID_MDC_KEY = "correlationId";

    @Override
    public Mono<WebGraphQlResponse> intercept(WebGraphQlRequest request, Chain chain) {
        String correlationId = CorrelationIdFilter.sanitize(
                request.getHeaders().getFirst(CORRELATION_ID_HEADER));

        // TODO: doFirst/doFinally rodam na thread reactor; com virtual threads o handler pode não ver o MDC.
        //  Usar ContextRegistry/ThreadLocalAccessor (micrometer-context-propagation) quando necessário.
        return chain.next(request)
                .doFirst(() -> MDC.put(CORRELATION_ID_MDC_KEY, correlationId))
                .doFinally(signal -> MDC.remove(CORRELATION_ID_MDC_KEY));
    }
}
