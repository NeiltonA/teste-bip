package com.bip.backend.dto;

import java.math.BigDecimal;
import javax.validation.constraints.DecimalMin;
import javax.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class TransferenciaRequest {

    @NotNull(message = "Benefício de origem é obrigatório")
    private Long origemId;

    @NotNull(message = "Benefício de destino é obrigatório")
    private Long destinoId;

    @NotNull(message = "Valor é obrigatório")
    @DecimalMin(value = "0.01", message = "Valor deve ser maior que zero")
    private BigDecimal valor;
}
