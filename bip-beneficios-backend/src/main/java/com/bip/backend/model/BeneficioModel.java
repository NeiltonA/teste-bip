package com.bip.backend.model;

import com.bip.ejb.Beneficio;
import java.math.BigDecimal;
import lombok.Value;

@Value
public class BeneficioModel {

    Long id;
    String nome;
    String descricao;
    BigDecimal valor;
    boolean ativo;
    Long version;

    public static BeneficioModel fromEntity(Beneficio beneficio) {
        return new BeneficioModel(
                beneficio.getId(),
                beneficio.getNome(),
                beneficio.getDescricao(),
                beneficio.getValor(),
                beneficio.isAtivo(),
                beneficio.getVersion()
        );
    }
}
