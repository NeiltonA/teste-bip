package com.bip.ejb;

import javax.ejb.ApplicationException;

import java.math.BigDecimal;

@ApplicationException(rollback = true)
public class SaldoInsuficienteException extends RuntimeException {

    public SaldoInsuficienteException(Long beneficioId, BigDecimal saldo, BigDecimal valorTransferencia) {
        super("Saldo insuficiente no benefício " + beneficioId
                + ". Saldo atual: " + saldo
                + ", transferência solicitada: " + valorTransferencia);
    }
}
