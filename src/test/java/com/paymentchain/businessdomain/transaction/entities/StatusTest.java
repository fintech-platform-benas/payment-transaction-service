package com.paymentchain.businessdomain.transaction.entities;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class StatusTest {

    @Test
    void testStatusValues() {
        // When/Then
        assertEquals("01", Status.PENDING.getCode());
        assertEquals("Pendiente", Status.PENDING.getDescripction());

        assertEquals("02", Status.SETTLED.getCode());
        assertEquals("Liquidada", Status.SETTLED.getDescripction());

        assertEquals("03", Status.REJECTED.getCode());
        assertEquals("Rechazada", Status.REJECTED.getDescripction());

        assertEquals("04", Status.CANCELLED.getCode());
        assertEquals("Cancelada", Status.CANCELLED.getDescripction());
    }

    @Test
    void testFromCodePending() {
        // When
        Status status = Status.fromCode("01");

        // Then
        assertEquals(Status.PENDING, status);
    }

    @Test
    void testFromCodeSettled() {
        // When
        Status status = Status.fromCode("02");

        // Then
        assertEquals(Status.SETTLED, status);
    }

    @Test
    void testFromCodeRejected() {
        // When
        Status status = Status.fromCode("03");

        // Then
        assertEquals(Status.REJECTED, status);
    }

    @Test
    void testFromCodeCancelled() {
        // When
        Status status = Status.fromCode("04");

        // Then
        assertEquals(Status.CANCELLED, status);
    }

    @Test
    void testFromCodeCaseInsensitive() {
        // When
        Status statusLower = Status.fromCode("01");
        Status statusUpper = Status.fromCode("01");

        // Then
        assertEquals(Status.PENDING, statusLower);
        assertEquals(Status.PENDING, statusUpper);
    }

    @Test
    void testFromCodeInvalid() {
        // When
        Status status = Status.fromCode("99");

        // Then
        assertNull(status);
    }

    @Test
    void testFromCodeBlank() {
        // When
        Status status = Status.fromCode("");

        // Then
        assertNull(status);
    }

    @Test
    void testAllStatusValues() {
        // When
        Status[] statuses = Status.values();

        // Then
        assertEquals(4, statuses.length);
        assertTrue(java.util.Arrays.asList(statuses).contains(Status.PENDING));
        assertTrue(java.util.Arrays.asList(statuses).contains(Status.SETTLED));
        assertTrue(java.util.Arrays.asList(statuses).contains(Status.REJECTED));
        assertTrue(java.util.Arrays.asList(statuses).contains(Status.CANCELLED));
    }
}
