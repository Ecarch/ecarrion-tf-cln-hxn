package com.mfpe.adapter.in.rest;

import com.mfpe.exception.OrderAlreadyCancelledException;
import com.mfpe.exception.OrderAlreadyPaidException;
import com.mfpe.exception.OrderDomainException;
import com.mfpe.exception.OrderNotFoundException;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class GlobalExceptionHandlerTest {

    private final GlobalExceptionHandler handler = new GlobalExceptionHandler();

    // ── OrderNotFoundException → 404 ────────────────────────────────

    @Test
    void handleNotFound_shouldReturn404() {
        var ex = new OrderNotFoundException("order-123");

        ResponseEntity<Map<String, Object>> response = handler.handleNotFound(ex);

        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        assertNotNull(response.getBody());
        assertTrue(response.getBody().get("error").toString().contains("order-123"));
        assertNotNull(response.getBody().get("timestamp"));
    }

    // ── OrderAlreadyPaidException → 409 ─────────────────────────────

    @Test
    void handleConflict_shouldReturn409ForAlreadyPaid() {
        var ex = new OrderAlreadyPaidException("order-456");

        ResponseEntity<Map<String, Object>> response = handler.handleConflict(ex);

        assertEquals(HttpStatus.CONFLICT, response.getStatusCode());
        assertNotNull(response.getBody());
        assertTrue(response.getBody().get("error").toString().contains("order-456"));
    }

    // ── OrderAlreadyCancelledException → 409 ────────────────────────

    @Test
    void handleConflict_shouldReturn409ForAlreadyCancelled() {
        var ex = new OrderAlreadyCancelledException("order-789");

        ResponseEntity<Map<String, Object>> response = handler.handleConflict(ex);

        assertEquals(HttpStatus.CONFLICT, response.getStatusCode());
        assertNotNull(response.getBody());
        assertTrue(response.getBody().get("error").toString().contains("order-789"));
    }

    // ── OrderDomainException → 400 ──────────────────────────────────

    @Test
    void handleDomainException_shouldReturn400() {
        var ex = new OrderDomainException("Some domain error");

        ResponseEntity<Map<String, Object>> response = handler.handleDomainException(ex);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals("Some domain error", response.getBody().get("error"));
    }

    // ── Generic Exception → 500 ────────────────────────────────────

    @Test
    void handleGeneric_shouldReturn500() {
        var ex = new RuntimeException("Unexpected failure");

        ResponseEntity<Map<String, Object>> response = handler.handleGeneric(ex);

        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
        assertNotNull(response.getBody());
        assertTrue(response.getBody().get("error").toString().contains("Unexpected failure"));
    }

    // ── Verify error body structure ─────────────────────────────────

    @Test
    void errorBody_shouldContainErrorAndTimestamp() {
        var ex = new OrderNotFoundException("order-1");

        ResponseEntity<Map<String, Object>> response = handler.handleNotFound(ex);

        Map<String, Object> body = response.getBody();
        assertNotNull(body);
        assertTrue(body.containsKey("error"));
        assertTrue(body.containsKey("timestamp"));
        assertEquals(2, body.size());
    }
}
