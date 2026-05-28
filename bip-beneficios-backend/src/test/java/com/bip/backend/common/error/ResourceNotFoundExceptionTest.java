package com.bip.backend.common.error;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

class ResourceNotFoundExceptionTest {

    @Test
    void shouldExposeMessage() {
        ResourceNotFoundException exception = new ResourceNotFoundException("Benefício não encontrado: 7");

        assertEquals("Benefício não encontrado: 7", exception.getMessage());
    }
}
