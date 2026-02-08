package com.sergiovitorino.hexagonalarchitectureexample.infrastructure.test;

import com.sergiovitorino.hexagonalarchitectureexample.infrastructure.GlobalExceptionHandler;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;

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

}
