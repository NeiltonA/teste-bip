package com.bip.backend.common.error;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.bip.ejb.BeneficioNaoEncontradoException;
import com.bip.ejb.RegraTransferenciaException;
import com.bip.ejb.SaldoInsuficienteException;
import java.math.BigDecimal;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BeanPropertyBindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;

class ApiExceptionHandlerTest {

    private ApiExceptionHandler handler;

    @BeforeEach
    void setUp() {
        handler = new ApiExceptionHandler();
    }

    @Test
    void handleNotFoundShouldReturn404ForResourceNotFound() {
        ResponseEntity<ApiError> response =
                handler.handleNotFound(new ResourceNotFoundException("Benefício não encontrado: 1"));

        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        assertEquals(404, response.getBody().getStatus());
        assertTrue(response.getBody().getMessages().get(0).contains("Benefício não encontrado"));
    }

    @Test
    void handleNotFoundShouldReturn404ForBeneficioNaoEncontrado() {
        ResponseEntity<ApiError> response =
                handler.handleNotFound(new BeneficioNaoEncontradoException(99L));

        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        assertEquals(404, response.getBody().getStatus());
    }

    @Test
    void handleBusinessRuleShouldReturn422ForRegraTransferencia() {
        ResponseEntity<ApiError> response =
                handler.handleBusinessRule(new RegraTransferenciaException("Origem e destino devem ser diferentes."));

        assertEquals(HttpStatus.UNPROCESSABLE_ENTITY, response.getStatusCode());
        assertEquals(422, response.getBody().getStatus());
    }

    @Test
    void handleBusinessRuleShouldReturn422ForSaldoInsuficiente() {
        ResponseEntity<ApiError> response = handler.handleBusinessRule(
                new SaldoInsuficienteException(1L, new BigDecimal("10.00"), new BigDecimal("50.00")));

        assertEquals(HttpStatus.UNPROCESSABLE_ENTITY, response.getStatusCode());
        assertTrue(response.getBody().getMessages().get(0).contains("Saldo insuficiente"));
    }

    @Test
    void handleValidationShouldReturn400WithFieldMessages() {
        BeanPropertyBindingResult bindingResult =
                new BeanPropertyBindingResult(new Object(), "beneficioRequest");
        bindingResult.addError(new FieldError("beneficioRequest", "nome", "Nome é obrigatório"));

        MethodArgumentNotValidException exception = mock(MethodArgumentNotValidException.class);
        when(exception.getBindingResult()).thenReturn(bindingResult);

        ResponseEntity<ApiError> response = handler.handleValidation(exception);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertEquals(400, response.getBody().getStatus());
        assertEquals("nome: Nome é obrigatório", response.getBody().getMessages().get(0));
    }

    @Test
    void handleUnexpectedShouldReturn500() {
        ResponseEntity<ApiError> response = handler.handleUnexpected(new RuntimeException("falha inesperada"));

        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
        assertEquals(500, response.getBody().getStatus());
        assertEquals("Erro interno ao processar a requisição.", response.getBody().getMessages().get(0));
    }
}
