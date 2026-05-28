package com.bip.backend.model;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.bip.ejb.Beneficio;
import java.math.BigDecimal;
import org.junit.jupiter.api.Test;

class BeneficioModelTest {

    @Test
    void fromEntityShouldMapAllFields() {
        Beneficio entity = new Beneficio("Vale", "Descrição", new BigDecimal("99.99"), true);
        entity.setId(5L);

        BeneficioModel model = BeneficioModel.fromEntity(entity);

        assertEquals(5L, model.getId());
        assertEquals("Vale", model.getNome());
        assertEquals("Descrição", model.getDescricao());
        assertEquals(new BigDecimal("99.99"), model.getValor());
        assertTrue(model.isAtivo());
        assertEquals(null, model.getVersion());
    }
}
