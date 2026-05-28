package com.bip.ejb;

import java.math.BigDecimal;
import lombok.Value;

@Value
public class TransferenciaResultado {

    Long origemId;
    Long destinoId;
    BigDecimal valorTransferido;
    BigDecimal saldoOrigem;
    BigDecimal saldoDestino;
}
