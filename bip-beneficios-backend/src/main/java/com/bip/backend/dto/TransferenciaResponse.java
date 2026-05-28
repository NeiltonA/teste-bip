package com.bip.backend.dto;

import java.math.BigDecimal;
import lombok.Value;

@Value
public class TransferenciaResponse {

    Long origemId;
    Long destinoId;
    BigDecimal valorTransferido;
    BigDecimal saldoOrigem;
    BigDecimal saldoDestino;
}
