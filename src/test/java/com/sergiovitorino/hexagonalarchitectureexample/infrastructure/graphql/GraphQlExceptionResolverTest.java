package com.sergiovitorino.hexagonalarchitectureexample.infrastructure.graphql;

import com.sergiovitorino.hexagonalarchitectureexample.domain.exception.DomainValidationException;
import com.sergiovitorino.hexagonalarchitectureexample.domain.exception.UserNotFoundException;
import graphql.execution.ExecutionStepInfo;
import graphql.execution.ResultPath;
import graphql.language.Field;
import graphql.schema.DataFetchingEnvironment;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.graphql.execution.ErrorType;

import java.util.Set;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

class GraphQlExceptionResolverTest {

    private GraphQlExceptionResolver resolver;
    private DataFetchingEnvironment env;

    @BeforeEach
    void setUp() {
        resolver = new GraphQlExceptionResolver();
        env = mock(DataFetchingEnvironment.class);

        var field = mock(Field.class);
        when(field.getSourceLocation()).thenReturn(null);
        when(env.getField()).thenReturn(field);

        var stepInfo = mock(ExecutionStepInfo.class);
        when(stepInfo.getPath()).thenReturn(ResultPath.rootPath());
        when(env.getExecutionStepInfo()).thenReturn(stepInfo);
    }

    @Test
    void userNotFoundException_returnsNotFound() {
        var ex = new UserNotFoundException(UUID.randomUUID());
        var error = resolver.resolveToSingleError(ex, env);
        assertThat(error).isNotNull();
        assertThat(error.getErrorType()).isEqualTo(ErrorType.NOT_FOUND);
    }

    @Test
    void domainValidationException_returnsBadRequest() {
        var ex = new DomainValidationException("invalid field");
        var error = resolver.resolveToSingleError(ex, env);
        assertThat(error).isNotNull();
        assertThat(error.getErrorType()).isEqualTo(ErrorType.BAD_REQUEST);
    }

    @Test
    void illegalArgumentException_returnsBadRequest() {
        var ex = new IllegalArgumentException("bad arg");
        var error = resolver.resolveToSingleError(ex, env);
        assertThat(error).isNotNull();
        assertThat(error.getErrorType()).isEqualTo(ErrorType.BAD_REQUEST);
    }

    @Test
    void constraintViolationException_returnsBadRequest() {
        ConstraintViolation<?> violation = mock(ConstraintViolation.class);
        var path = mock(jakarta.validation.Path.class);
        when(path.toString()).thenReturn("name");
        when(violation.getPropertyPath()).thenReturn(path);
        when(violation.getMessage()).thenReturn("must not be blank");

        var ex = new ConstraintViolationException("validation failed", Set.of(violation));
        var error = resolver.resolveToSingleError(ex, env);

        assertThat(error).isNotNull();
        assertThat(error.getErrorType()).isEqualTo(ErrorType.BAD_REQUEST);
        assertThat(error.getMessage()).contains("must not be blank");
    }

    @Test
    void unknownException_returnsNull() {
        var ex = new RuntimeException("unexpected");
        var error = resolver.resolveToSingleError(ex, env);
        assertThat(error).isNull();
    }
}
