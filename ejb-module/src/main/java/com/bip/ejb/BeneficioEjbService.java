package com.bip.ejb;

import java.math.BigDecimal;
import java.math.RoundingMode;
import javax.ejb.Stateless;
import javax.ejb.TransactionAttribute;
import javax.ejb.TransactionAttributeType;
import javax.persistence.EntityManager;
import javax.persistence.LockModeType;
import javax.persistence.PersistenceContext;
import lombok.Value;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Stateless
public class BeneficioEjbService {

    @PersistenceContext
    private EntityManager em;

    public BeneficioEjbService() {
    }

    public BeneficioEjbService(EntityManager em) {
        this.em = em;
    }

    @TransactionAttribute(TransactionAttributeType.REQUIRED)
    public TransferenciaResultado transfer(Long fromId, Long toId, BigDecimal amount) {
        log.info("EJB: iniciando transferencia origem={} destino={} valor={}", fromId, toId, amount);

        validateRequest(fromId, toId, amount);

        BigDecimal normalizedAmount = amount.setScale(2, RoundingMode.HALF_UP);
        LockedBeneficios locked = lockBeneficios(fromId, toId);

        Beneficio from = locked.getFrom();
        Beneficio to = locked.getTo();

        validateBeneficioAtivo(from, "origem");
        validateBeneficioAtivo(to, "destino");

        if (from.getValor().compareTo(normalizedAmount) < 0) {
            log.warn(
                    "EJB: saldo insuficiente origem={} saldo={} valor={}",
                    fromId,
                    from.getValor(),
                    normalizedAmount
            );
            throw new SaldoInsuficienteException(fromId, from.getValor(), normalizedAmount);
        }

        from.setValor(from.getValor().subtract(normalizedAmount));
        to.setValor(to.getValor().add(normalizedAmount));

        em.flush();

        TransferenciaResultado resultado = new TransferenciaResultado(
                from.getId(),
                to.getId(),
                normalizedAmount,
                from.getValor(),
                to.getValor()
        );

        log.info(
                "EJB: transferencia concluida origem={} saldoOrigem={} destino={} saldoDestino={}",
                resultado.getOrigemId(),
                resultado.getSaldoOrigem(),
                resultado.getDestinoId(),
                resultado.getSaldoDestino()
        );

        return resultado;
    }

    private void validateRequest(Long fromId, Long toId, BigDecimal amount) {
        if (fromId == null || toId == null) {
            log.warn("EJB: origem ou destino nao informado");
            throw new RegraTransferenciaException("Benefício de origem e destino são obrigatórios.");
        }
        if (fromId.equals(toId)) {
            log.warn("EJB: origem e destino iguais id={}", fromId);
            throw new RegraTransferenciaException("Origem e destino devem ser benefícios diferentes.");
        }
        if (amount == null || amount.compareTo(BigDecimal.ZERO) <= 0) {
            log.warn("EJB: valor da transferencia invalido valor={}", amount);
            throw new RegraTransferenciaException("Valor da transferência deve ser maior que zero.");
        }
    }

    private LockedBeneficios lockBeneficios(Long fromId, Long toId) {
        Long firstId = fromId < toId ? fromId : toId;
        Long secondId = fromId < toId ? toId : fromId;

        log.debug("EJB: bloqueando beneficios ids={},{} (ordem deadlock-safe)", firstId, secondId);

        Beneficio first = findWithLock(firstId);
        Beneficio second = findWithLock(secondId);

        return fromId.equals(firstId)
                ? new LockedBeneficios(first, second)
                : new LockedBeneficios(second, first);
    }

    private Beneficio findWithLock(Long id) {
        Beneficio beneficio = em.find(Beneficio.class, id, LockModeType.PESSIMISTIC_WRITE);
        if (beneficio == null) {
            log.warn("EJB: beneficio id={} nao encontrado", id);
            throw new BeneficioNaoEncontradoException(id);
        }
        return beneficio;
    }

    private void validateBeneficioAtivo(Beneficio beneficio, String papel) {
        if (!beneficio.isAtivo()) {
            log.warn("EJB: beneficio id={} inativo no papel {}", beneficio.getId(), papel);
            throw new RegraTransferenciaException("Benefício de " + papel + " está inativo.");
        }
    }

    @Value
    private static class LockedBeneficios {
        Beneficio from;
        Beneficio to;
    }
}
