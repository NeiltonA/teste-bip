package com.bip.ejb;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import javax.persistence.EntityManager;
import javax.persistence.LockModeType;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InOrder;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class BeneficioEjbServiceTest {

    @Mock
    private EntityManager entityManager;

    @Test
    void transferShouldLockValidateBalanceAndMoveAmount() {
        Beneficio origem = beneficio(1L, "Origem", "100.00", true);
        Beneficio destino = beneficio(2L, "Destino", "50.00", true);

        when(entityManager.find(Beneficio.class, 1L, LockModeType.PESSIMISTIC_WRITE)).thenReturn(origem);
        when(entityManager.find(Beneficio.class, 2L, LockModeType.PESSIMISTIC_WRITE)).thenReturn(destino);

        TransferenciaResultado resultado = new BeneficioEjbService(entityManager)
                .transfer(1L, 2L, new BigDecimal("25.00"));

        assertEquals(new BigDecimal("75.00"), origem.getValor());
        assertEquals(new BigDecimal("75.00"), destino.getValor());
        assertEquals(new BigDecimal("25.00"), resultado.getValorTransferido());
        verify(entityManager).flush();
    }

    @Test
    void transferShouldRejectInsufficientBalanceAndRollbackByException() {
        Beneficio origem = beneficio(1L, "Origem", "10.00", true);
        Beneficio destino = beneficio(2L, "Destino", "50.00", true);

        when(entityManager.find(Beneficio.class, 1L, LockModeType.PESSIMISTIC_WRITE)).thenReturn(origem);
        when(entityManager.find(Beneficio.class, 2L, LockModeType.PESSIMISTIC_WRITE)).thenReturn(destino);

        assertThrows(
                SaldoInsuficienteException.class,
                () -> new BeneficioEjbService(entityManager).transfer(1L, 2L, new BigDecimal("25.00"))
        );

        assertEquals(new BigDecimal("10.00"), origem.getValor());
        assertEquals(new BigDecimal("50.00"), destino.getValor());
        verify(entityManager, never()).flush();
    }

    @Test
    void transferShouldRejectSameOriginAndDestination() {
        assertThrows(
                RegraTransferenciaException.class,
                () -> new BeneficioEjbService(entityManager).transfer(1L, 1L, new BigDecimal("10.00"))
        );
    }

    @Test
    void transferShouldLockBeneficiosInAscendingIdOrder() {
        Beneficio id1 = beneficio(1L, "Um", "100.00", true);
        Beneficio id2 = beneficio(2L, "Dois", "100.00", true);

        when(entityManager.find(Beneficio.class, 1L, LockModeType.PESSIMISTIC_WRITE)).thenReturn(id1);
        when(entityManager.find(Beneficio.class, 2L, LockModeType.PESSIMISTIC_WRITE)).thenReturn(id2);

        new BeneficioEjbService(entityManager).transfer(2L, 1L, new BigDecimal("10.00"));

        InOrder order = inOrder(entityManager);
        order.verify(entityManager).find(Beneficio.class, 1L, LockModeType.PESSIMISTIC_WRITE);
        order.verify(entityManager).find(Beneficio.class, 2L, LockModeType.PESSIMISTIC_WRITE);
    }

    @Test
    void transferShouldRejectInactiveBeneficio() {
        Beneficio origem = beneficio(1L, "Origem", "100.00", false);
        Beneficio destino = beneficio(2L, "Destino", "50.00", true);

        when(entityManager.find(Beneficio.class, 1L, LockModeType.PESSIMISTIC_WRITE)).thenReturn(origem);
        when(entityManager.find(Beneficio.class, 2L, LockModeType.PESSIMISTIC_WRITE)).thenReturn(destino);

        assertThrows(
                RegraTransferenciaException.class,
                () -> new BeneficioEjbService(entityManager).transfer(1L, 2L, new BigDecimal("10.00"))
        );
    }

    private Beneficio beneficio(Long id, String nome, String valor, boolean ativo) {
        Beneficio beneficio = new Beneficio(nome, null, new BigDecimal(valor), ativo);
        beneficio.setId(id);
        return beneficio;
    }
}
