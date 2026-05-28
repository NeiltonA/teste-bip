package com.bip.backend.service;

import com.bip.backend.common.error.ResourceNotFoundException;
import com.bip.backend.dto.BeneficioRequest;
import com.bip.backend.dto.BeneficioResponse;
import com.bip.backend.dto.TransferenciaRequest;
import com.bip.backend.dto.TransferenciaResponse;
import com.bip.backend.model.BeneficioModel;
import com.bip.backend.repository.BeneficioRepository;
import com.bip.ejb.Beneficio;
import com.bip.ejb.BeneficioEjbService;
import com.bip.ejb.TransferenciaResultado;
import java.math.RoundingMode;
import java.util.List;
import java.util.stream.Collectors;
import javax.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class BeneficioApplicationService {

    private final BeneficioRepository repository;
    private final BeneficioEjbService ejbService;

    public List<BeneficioResponse> list() {
        log.info("Servico: listando beneficios");
        List<BeneficioResponse> beneficios = repository.findAll()
                .stream()
                .map(BeneficioModel::fromEntity)
                .map(this::toResponse)
                .collect(Collectors.toList());
        log.info("Servico: listagem concluida - {} beneficio(s)", beneficios.size());
        return beneficios;
    }

    public BeneficioResponse findById(Long id) {
        log.info("Servico: buscando beneficio id={}", id);
        BeneficioResponse response = repository.findById(id)
                .map(BeneficioModel::fromEntity)
                .map(this::toResponse)
                .orElseThrow(() -> {
                    log.warn("Servico: beneficio id={} nao encontrado", id);
                    return new ResourceNotFoundException("Benefício não encontrado: " + id);
                });
        log.info("Servico: beneficio id={} encontrado nome={}", id, response.getNome());
        return response;
    }

    @Transactional
    public BeneficioResponse create(BeneficioRequest request) {
        log.info("Servico: criando beneficio nome={} ativo={}", request.getNome(), request.getAtivo());
        Beneficio beneficio = new Beneficio(
                request.getNome().trim(),
                request.getDescricao(),
                request.getValor().setScale(2, RoundingMode.HALF_UP),
                request.getAtivo() == null || request.getAtivo()
        );

        BeneficioResponse response = toResponse(BeneficioModel.fromEntity(repository.save(beneficio)));
        log.info("Servico: beneficio criado id={} valor={}", response.getId(), response.getValor());
        return response;
    }

    @Transactional
    public BeneficioResponse update(Long id, BeneficioRequest request) {
        log.info("Servico: atualizando beneficio id={}", id);
        Beneficio beneficio = repository.findById(id)
                .orElseThrow(() -> {
                    log.warn("Servico: beneficio id={} nao encontrado para atualizacao", id);
                    return new ResourceNotFoundException("Benefício não encontrado: " + id);
                });

        beneficio.setNome(request.getNome().trim());
        beneficio.setDescricao(request.getDescricao());
        beneficio.setValor(request.getValor().setScale(2, RoundingMode.HALF_UP));
        beneficio.setAtivo(request.getAtivo() == null || request.getAtivo());

        BeneficioResponse response = toResponse(BeneficioModel.fromEntity(repository.save(beneficio)));
        log.info("Servico: beneficio id={} atualizado valor={} ativo={}", id, response.getValor(), response.isAtivo());
        return response;
    }

    @Transactional
    public void delete(Long id) {
        log.info("Servico: excluindo beneficio id={}", id);
        Beneficio beneficio = repository.findById(id)
                .orElseThrow(() -> {
                    log.warn("Servico: beneficio id={} nao encontrado para exclusao", id);
                    return new ResourceNotFoundException("Benefício não encontrado: " + id);
                });
        log.info("Servico: removendo beneficio id={} nome={}", id, beneficio.getNome());
        repository.delete(beneficio);
        log.info("Servico: beneficio id={} excluido", id);
    }

    @Transactional
    public TransferenciaResponse transfer(TransferenciaRequest request) {
        log.info(
                "Servico: transferencia origem={} destino={} valor={}",
                request.getOrigemId(),
                request.getDestinoId(),
                request.getValor()
        );
        TransferenciaResultado resultado = ejbService.transfer(
                request.getOrigemId(),
                request.getDestinoId(),
                request.getValor()
        );

        TransferenciaResponse response = new TransferenciaResponse(
                resultado.getOrigemId(),
                resultado.getDestinoId(),
                resultado.getValorTransferido(),
                resultado.getSaldoOrigem(),
                resultado.getSaldoDestino()
        );
        log.info(
                "Servico: transferencia concluida origem={} saldoOrigem={} destino={} saldoDestino={}",
                response.getOrigemId(),
                response.getSaldoOrigem(),
                response.getDestinoId(),
                response.getSaldoDestino()
        );
        return response;
    }

    private BeneficioResponse toResponse(BeneficioModel beneficio) {
        return new BeneficioResponse(
                beneficio.getId(),
                beneficio.getNome(),
                beneficio.getDescricao(),
                beneficio.getValor(),
                beneficio.isAtivo(),
                beneficio.getVersion()
        );
    }
}
