package com.bip.backend.common.error;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import java.util.List;
import org.junit.jupiter.api.Test;

class ApiErrorTest {

    @Test
    void ofShouldCreateErrorWithSingleMessage() {
        ApiError error = ApiError.of(404, "Not Found", "Benefício não encontrado");

        assertNotNull(error.getTimestamp());
        assertEquals(404, error.getStatus());
        assertEquals("Not Found", error.getError());
        assertEquals(List.of("Benefício não encontrado"), error.getMessages());
    }

    @Test
    void ofShouldCreateErrorWithMultipleMessages() {
        ApiError error = ApiError.of(400, "Validation Error", List.of("nome: obrigatório", "valor: inválido"));

        assertEquals(400, error.getStatus());
        assertEquals(2, error.getMessages().size());
    }

    @Test
    void constructorShouldExposeAllFields() {
        ApiError error = ApiError.of(500, "Internal Server Error", "Erro interno");

        assertNotNull(error.getTimestamp());
        assertEquals(500, error.getStatus());
        assertEquals("Internal Server Error", error.getError());
        assertEquals("Erro interno", error.getMessages().get(0));
    }
}
