package com.sergiovitorino.hexagonalarchitectureexample.infrastructure.web;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.MDC;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.UUID;
import java.util.regex.Pattern;

/**
 * Filtro que adiciona e propaga um correlationId em toda a requisição.
 * Headers inválidos (null, blank, > 64 chars ou com caracteres fora de [a-zA-Z0-9\-])
 * são descartados e um UUID novo é gerado, prevenindo log injection.
 */
@Order(Ordered.HIGHEST_PRECEDENCE)
@Component
public class CorrelationIdFilter extends OncePerRequestFilter {

    /** Aceita apenas alfanuméricos e hífen, entre 1 e 64 caracteres. */
    static final Pattern VALID_CORRELATION_ID = Pattern.compile("^[a-zA-Z0-9\\-]{1,64}$");

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {
        String correlationId = sanitize(request.getHeader("X-Correlation-Id"));

        MDC.put("correlationId", correlationId);
        response.setHeader("X-Correlation-Id", correlationId);

        try {
            filterChain.doFilter(request, response);
        } finally {
            MDC.remove("correlationId");
        }
    }

    /**
     * Valida o valor recebido contra o regex seguro.
     * Retorna o valor original se válido, ou um novo UUID caso contrário.
     */
    static String sanitize(String value) {
        if (value != null && VALID_CORRELATION_ID.matcher(value).matches()) {
            return value;
        }
        return UUID.randomUUID().toString();
    }
}
