package com.bip.backend.dto;

import java.math.BigDecimal;
import lombok.Value;

@Value
public class BeneficioResponse {

    Long id;
    String nome;
    String descricao;
    BigDecimal valor;
    boolean ativo;
    Long version;
}
