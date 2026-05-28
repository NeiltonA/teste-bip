package com.bip.backend.common.error;

import com.bip.ejb.BeneficioNaoEncontradoException;
import com.bip.ejb.RegraTransferenciaException;
import com.bip.ejb.SaldoInsuficienteException;
import java.util.List;
import java.util.stream.Collectors;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@Slf4j
@RestControllerAdvice
public class ApiExceptionHandler {

    @ExceptionHandler({ResourceNotFoundException.class, BeneficioNaoEncontradoException.class})
    public ResponseEntity<ApiError> handleNotFound(RuntimeException exception) {
        log.warn("Recurso nao encontrado: {}", exception.getMessage());
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(ApiError.of(404, "Not Found", exception.getMessage()));
    }

    @ExceptionHandler({RegraTransferenciaException.class, SaldoInsuficienteException.class})
    public ResponseEntity<ApiError> handleBusinessRule(RuntimeException exception) {
        log.warn("Regra de negocio violada: {}", exception.getMessage());
        return ResponseEntity.status(HttpStatus.UNPROCESSABLE_ENTITY)
                .body(ApiError.of(422, "Business Rule Violation", exception.getMessage()));
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiError> handleValidation(MethodArgumentNotValidException exception) {
        List<String> messages = exception.getBindingResult()
                .getFieldErrors()
                .stream()
                .map(error -> error.getField() + ": " + error.getDefaultMessage())
                .collect(Collectors.toList());

        log.warn("Erro de validacao: {}", messages);
        return ResponseEntity.badRequest()
                .body(ApiError.of(400, "Validation Error", messages));
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiError> handleUnexpected(Exception exception) {
        log.error("Erro inesperado na API", exception);
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(ApiError.of(500, "Internal Server Error", "Erro interno ao processar a requisição."));
    }
}
