package com.paymentchain.transaction.entities;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class ChannelTest {

    @Test
    void testChannelValues() {
        // When
        Channel[] channels = Channel.values();

        // Then
        assertEquals(3, channels.length);
        assertTrue(java.util.Arrays.asList(channels).contains(Channel.WEB));
        assertTrue(java.util.Arrays.asList(channels).contains(Channel.CAJERO));
        assertTrue(java.util.Arrays.asList(channels).contains(Channel.OFICINA));
    }

    @Test
    void testChannelValueOf() {
        // When/Then
        assertEquals(Channel.WEB, Channel.valueOf("WEB"));
        assertEquals(Channel.CAJERO, Channel.valueOf("CAJERO"));
        assertEquals(Channel.OFICINA, Channel.valueOf("OFICINA"));
    }
}
