package com.bip.ejb;

import javax.ejb.ApplicationException;

@ApplicationException(rollback = true)
public class RegraTransferenciaException extends RuntimeException {

    public RegraTransferenciaException(String message) {
        super(message);
    }
}
