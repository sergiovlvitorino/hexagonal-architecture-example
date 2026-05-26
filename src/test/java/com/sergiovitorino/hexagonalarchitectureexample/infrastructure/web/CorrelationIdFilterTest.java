package com.sergiovitorino.hexagonalarchitectureexample.infrastructure.web;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.slf4j.MDC;
import org.springframework.mock.web.MockFilterChain;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

import java.io.IOException;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicReference;

import static org.junit.jupiter.api.Assertions.*;

public class CorrelationIdFilterTest {

    private CorrelationIdFilter filter;

    @BeforeEach
    void setUp() {
        filter = new CorrelationIdFilter();
        // Garante MDC limpo antes de cada teste
        MDC.clear();
    }

    @Test
    public void doFilter_headerPresent_usesHeaderValue() throws ServletException, IOException {
        // Arrange
        var request = new MockHttpServletRequest();
        request.addHeader("X-Correlation-Id", "abc123");
        var response = new MockHttpServletResponse();

        // Captura o MDC durante execução da chain
        var mdcDuringChain = new AtomicReference<String>();
        FilterChain capturingChain = (req, res) -> mdcDuringChain.set(MDC.get("correlationId"));

        // Act
        filter.doFilterInternal(request, response, capturingChain);

        // Assert: response header e MDC durante chain contêm o valor do header de entrada
        assertEquals("abc123", response.getHeader("X-Correlation-Id"));
        assertEquals("abc123", mdcDuringChain.get());
    }

    @Test
    public void doFilter_headerAbsent_generatesUuid() throws ServletException, IOException {
        // Arrange: sem header X-Correlation-Id
        var request = new MockHttpServletRequest();
        var response = new MockHttpServletResponse();
        var chain = new MockFilterChain();

        // Act
        filter.doFilterInternal(request, response, chain);

        // Assert: response header não-vazio e com formato UUID válido
        var generatedId = response.getHeader("X-Correlation-Id");
        assertNotNull(generatedId, "X-Correlation-Id deve ser gerado quando ausente");
        assertFalse(generatedId.isBlank());
        // Deve ser um UUID válido (não lança exceção)
        assertDoesNotThrow(() -> UUID.fromString(generatedId),
            "ID gerado deve ser um UUID válido, recebeu: " + generatedId);
    }

    @Test
    public void doFilter_clearsMDCAfterRequest() throws ServletException, IOException {
        // Arrange
        var request = new MockHttpServletRequest();
        request.addHeader("X-Correlation-Id", "mdc-test-id");
        var response = new MockHttpServletResponse();
        var chain = new MockFilterChain();

        // Act
        filter.doFilterInternal(request, response, chain);

        // Assert: MDC deve estar limpo após execução do filtro
        assertNull(MDC.get("correlationId"),
            "MDC.correlationId deve ser removido após execução do filtro");
    }

    @Test
    public void doFilter_clearsMDC_evenWhenChainThrows() {
        // Arrange
        var request = new MockHttpServletRequest();
        request.addHeader("X-Correlation-Id", "error-test-id");
        var response = new MockHttpServletResponse();
        FilterChain throwingChain = (req, res) -> {
            throw new IOException("simulated chain failure");
        };

        // Act: o filtro deve propagar a exceção mas garantir limpeza do MDC via finally
        assertThrows(IOException.class,
            () -> filter.doFilterInternal(request, response, throwingChain));

        // Assert: MDC limpo mesmo após exceção
        assertNull(MDC.get("correlationId"),
            "MDC.correlationId deve ser removido mesmo quando a chain lança exceção");
    }

    @Test
    public void doFilter_emptyHeader_generatesUuid() throws ServletException, IOException {
        // Arrange: header presente mas vazio (string vazia deve gerar UUID como ausente)
        var request = new MockHttpServletRequest();
        request.addHeader("X-Correlation-Id", "");
        var response = new MockHttpServletResponse();
        var chain = new MockFilterChain();

        // Act
        filter.doFilterInternal(request, response, chain);

        // Assert: deve gerar um UUID, não usar a string vazia
        var generatedId = response.getHeader("X-Correlation-Id");
        assertNotNull(generatedId);
        assertFalse(generatedId.isBlank(), "Header vazio deve gerar UUID, não propagar string vazia");
        assertDoesNotThrow(() -> UUID.fromString(generatedId));
    }

    @Test
    public void doFilter_setsCorrelationIdInMDC_duringChainExecution() throws ServletException, IOException {
        // Arrange: verifica que o MDC está populado durante a execução da chain
        var request = new MockHttpServletRequest();
        var response = new MockHttpServletResponse();
        var mdcDuringChain = new AtomicReference<String>();

        FilterChain capturingChain = (req, res) -> mdcDuringChain.set(MDC.get("correlationId"));

        // Act
        filter.doFilterInternal(request, response, capturingChain);

        // Assert: MDC estava preenchido durante a chain
        assertNotNull(mdcDuringChain.get(),
            "MDC.correlationId deve estar preenchido durante execução da chain");
        // E deve ser um UUID válido (gerado automaticamente)
        assertDoesNotThrow(() -> UUID.fromString(mdcDuringChain.get()));
    }

    // --- Testes de proteção contra log injection ---

    @Test
    public void doFilter_headerWithNewline_isRejectedAndNewUuidGenerated() throws ServletException, IOException {
        // Header com \n pode ser usado para log injection — deve ser descartado
        var request = new MockHttpServletRequest();
        request.addHeader("X-Correlation-Id", "valid-id\ninjected-line");
        var response = new MockHttpServletResponse();
        var chain = new MockFilterChain();

        filter.doFilterInternal(request, response, chain);

        var resultId = response.getHeader("X-Correlation-Id");
        assertNotNull(resultId);
        assertDoesNotThrow(() -> UUID.fromString(resultId),
            "Header com newline deve ser descartado e UUID gerado");
    }

    @Test
    public void doFilter_headerExceeding64Chars_isRejectedAndNewUuidGenerated() throws ServletException, IOException {
        // Header com mais de 64 caracteres deve ser descartado
        var longId = "a".repeat(65);
        var request = new MockHttpServletRequest();
        request.addHeader("X-Correlation-Id", longId);
        var response = new MockHttpServletResponse();
        var chain = new MockFilterChain();

        filter.doFilterInternal(request, response, chain);

        var resultId = response.getHeader("X-Correlation-Id");
        assertNotNull(resultId);
        assertNotEquals(longId, resultId, "Header > 64 chars deve ser descartado");
        assertDoesNotThrow(() -> UUID.fromString(resultId));
    }

    @Test
    public void doFilter_headerWithSpecialChars_isRejectedAndNewUuidGenerated() throws ServletException, IOException {
        // Caracteres especiais fora de [a-zA-Z0-9\-] devem ser rejeitados
        var request = new MockHttpServletRequest();
        request.addHeader("X-Correlation-Id", "id<script>alert(1)</script>");
        var response = new MockHttpServletResponse();
        var chain = new MockFilterChain();

        filter.doFilterInternal(request, response, chain);

        var resultId = response.getHeader("X-Correlation-Id");
        assertNotNull(resultId);
        assertDoesNotThrow(() -> UUID.fromString(resultId),
            "Header com caracteres inválidos deve ser descartado e UUID gerado");
    }
}
