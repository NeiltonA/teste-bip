package com.bip.ejb;

import javax.ejb.ApplicationException;

@ApplicationException(rollback = true)
public class BeneficioNaoEncontradoException extends RuntimeException {

    public BeneficioNaoEncontradoException(Long id) {
        super("Benefício não encontrado: " + id);
    }
}
