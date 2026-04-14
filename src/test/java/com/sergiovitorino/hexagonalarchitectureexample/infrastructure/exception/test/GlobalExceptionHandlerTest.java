package com.sergiovitorino.hexagonalarchitectureexample.infrastructure.exception.test;

import com.sergiovitorino.hexagonalarchitectureexample.infrastructure.exception.GlobalExceptionHandler;
import org.junit.jupiter.api.Test;
import org.springframework.core.MethodParameter;
import org.springframework.http.HttpStatus;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.validation.BeanPropertyBindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

public class GlobalExceptionHandlerTest {

    private final GlobalExceptionHandler handler = new GlobalExceptionHandler();

    @Test
    public void testHandleIllegalArgument() {
        var response = handler.handleIllegalArgument(new IllegalArgumentException("invalid value"));

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertEquals("invalid value", response.getBody().get("error"));
    }

    @Test
    public void testHandleGenericException() {
        var response = handler.handleGeneric(new RuntimeException("unexpected"));

        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
        assertEquals("An unexpected error occurred", response.getBody().get("error"));
    }

    @Test
    public void testHandleValidationException() {
        var bindingResult = new BeanPropertyBindingResult(new Object(), "saveCommand");
        bindingResult.addError(new FieldError("saveCommand", "name", "must not be empty"));

        var methodParameter = new MethodParameter(Object.class.getDeclaredMethods()[0], -1);
        var ex = new MethodArgumentNotValidException(methodParameter, bindingResult);
        var response = handler.handleValidation(ex);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertNotNull(response.getBody());
        assertTrue(response.getBody().containsKey("errors"));

        @SuppressWarnings("unchecked")
        var errors = (List<Map<String, String>>) response.getBody().get("errors");
        assertEquals(1, errors.size());
        assertEquals("name", errors.get(0).get("field"));
        assertEquals("must not be empty", errors.get(0).get("message"));
    }

    @Test
    public void testHandleNotReadable() {
        var ex = new HttpMessageNotReadableException("bad json", (org.springframework.http.HttpInputMessage) null);
        var response = handler.handleNotReadable(ex);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertEquals("Malformed request body", response.getBody().get("error"));
    }

    @Test
    public void testHandleTypeMismatch() {
        var ex = new MethodArgumentTypeMismatchException("value", Integer.class, "pageNumber", null, null);
        var response = handler.handleTypeMismatch(ex);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertTrue(response.getBody().get("error").contains("pageNumber"));
    }

}
