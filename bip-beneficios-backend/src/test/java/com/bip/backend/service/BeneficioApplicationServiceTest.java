package com.bip.backend.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import org.mockito.ArgumentCaptor;

import com.bip.backend.common.error.ResourceNotFoundException;
import com.bip.backend.dto.BeneficioRequest;
import com.bip.backend.dto.BeneficioResponse;
import com.bip.backend.dto.TransferenciaRequest;
import com.bip.backend.repository.BeneficioRepository;
import com.bip.ejb.Beneficio;
import com.bip.ejb.BeneficioEjbService;
import com.bip.ejb.TransferenciaResultado;
import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class BeneficioApplicationServiceTest {

    @Mock
    private BeneficioRepository repository;

    @Mock
    private BeneficioEjbService ejbService;

    @InjectMocks
    private BeneficioApplicationService service;

    @Test
    void listShouldReturnAllBeneficios() {
        Beneficio entity = new Beneficio("Vale", "Desc", new BigDecimal("100.00"), true);
        entity.setId(1L);
        when(repository.findAll()).thenReturn(List.of(entity));

        List<BeneficioResponse> result = service.list();

        assertEquals(1, result.size());
        assertEquals("Vale", result.get(0).getNome());
    }

    @Test
    void findByIdShouldReturnBeneficioWhenExists() {
        Beneficio entity = new Beneficio("Plano", "Desc", new BigDecimal("250.00"), true);
        entity.setId(7L);
        when(repository.findById(7L)).thenReturn(Optional.of(entity));

        BeneficioResponse response = service.findById(7L);

        assertEquals(7L, response.getId());
        assertEquals("Plano", response.getNome());
        assertEquals(new BigDecimal("250.00"), response.getValor());
    }

    @Test
    void findByIdShouldThrowWhenMissing() {
        when(repository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> service.findById(99L));
    }

    @Test
    void createShouldPersistBeneficio() {
        BeneficioRequest request = new BeneficioRequest();
        request.setNome("  Auxilio  ");
        request.setDescricao("Mensal");
        request.setValor(new BigDecimal("150.555"));
        request.setAtivo(true);

        when(repository.save(any(Beneficio.class))).thenAnswer(invocation -> {
            Beneficio saved = invocation.getArgument(0);
            saved.setId(10L);
            return saved;
        });

        BeneficioResponse response = service.create(request);

        assertEquals(10L, response.getId());
        assertEquals("Auxilio", response.getNome());
        assertEquals(new BigDecimal("150.56"), response.getValor());
        verify(repository).save(any(Beneficio.class));
    }

    @Test
    void createShouldRespectAtivoFalse() {
        BeneficioRequest request = new BeneficioRequest();
        request.setNome("Inativo");
        request.setValor(new BigDecimal("5.00"));
        request.setAtivo(false);

        when(repository.save(any(Beneficio.class))).thenAnswer(invocation -> {
            Beneficio saved = invocation.getArgument(0);
            saved.setId(12L);
            return saved;
        });

        service.create(request);

        ArgumentCaptor<Beneficio> captor = ArgumentCaptor.forClass(Beneficio.class);
        verify(repository).save(captor.capture());
        assertEquals(false, captor.getValue().isAtivo());
    }

    @Test
    void createShouldDefaultAtivoTrueWhenNull() {
        BeneficioRequest request = new BeneficioRequest();
        request.setNome("Benefício");
        request.setValor(new BigDecimal("10.00"));
        request.setAtivo(null);

        when(repository.save(any(Beneficio.class))).thenAnswer(invocation -> {
            Beneficio saved = invocation.getArgument(0);
            saved.setId(11L);
            return saved;
        });

        service.create(request);

        ArgumentCaptor<Beneficio> captor = ArgumentCaptor.forClass(Beneficio.class);
        verify(repository).save(captor.capture());
        assertTrue(captor.getValue().isAtivo());
    }

    @Test
    void updateShouldDefaultAtivoTrueWhenNull() {
        Beneficio entity = new Beneficio("A", null, new BigDecimal("10.00"), false);
        entity.setId(6L);
        when(repository.findById(6L)).thenReturn(Optional.of(entity));
        when(repository.save(any(Beneficio.class))).thenAnswer(invocation -> invocation.getArgument(0));

        BeneficioRequest request = new BeneficioRequest();
        request.setNome("A");
        request.setValor(new BigDecimal("10.00"));
        request.setAtivo(null);

        BeneficioResponse response = service.update(6L, request);

        assertTrue(response.isAtivo());
    }

    @Test
    void updateShouldPersistChanges() {
        Beneficio entity = new Beneficio("Antigo", "Desc", new BigDecimal("50.00"), true);
        entity.setId(4L);
        when(repository.findById(4L)).thenReturn(Optional.of(entity));
        when(repository.save(any(Beneficio.class))).thenAnswer(invocation -> invocation.getArgument(0));

        BeneficioRequest request = new BeneficioRequest();
        request.setNome("  Novo  ");
        request.setDescricao("Atualizado");
        request.setValor(new BigDecimal("75.555"));
        request.setAtivo(false);

        BeneficioResponse response = service.update(4L, request);

        assertEquals("Novo", response.getNome());
        assertEquals("Atualizado", response.getDescricao());
        assertEquals(new BigDecimal("75.56"), response.getValor());
        assertEquals(false, response.isAtivo());
    }

    @Test
    void updateShouldThrowWhenBeneficioDoesNotExist() {
        when(repository.findById(5L)).thenReturn(Optional.empty());

        BeneficioRequest request = new BeneficioRequest();
        request.setNome("X");
        request.setValor(new BigDecimal("1.00"));
        request.setAtivo(true);

        assertThrows(ResourceNotFoundException.class, () -> service.update(5L, request));
        verify(repository, never()).save(any());
    }

    @Test
    void deleteShouldRemoveExistingBeneficio() {
        Beneficio entity = new Beneficio("A", null, new BigDecimal("10.00"), true);
        entity.setId(3L);
        when(repository.findById(3L)).thenReturn(Optional.of(entity));

        service.delete(3L);

        verify(repository).delete(entity);
    }

    @Test
    void deleteShouldThrowWhenBeneficioDoesNotExist() {
        when(repository.findById(8L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> service.delete(8L));
        verify(repository, never()).delete(any());
    }

    @Test
    void transferShouldDelegateToEjb() {
        TransferenciaRequest request = new TransferenciaRequest();
        request.setOrigemId(1L);
        request.setDestinoId(2L);
        request.setValor(new BigDecimal("25.00"));

        when(ejbService.transfer(1L, 2L, new BigDecimal("25.00")))
                .thenReturn(new TransferenciaResultado(1L, 2L, new BigDecimal("25.00"), new BigDecimal("75.00"), new BigDecimal("75.00")));

        var response = service.transfer(request);

        assertEquals(new BigDecimal("75.00"), response.getSaldoOrigem());
        verify(ejbService).transfer(1L, 2L, new BigDecimal("25.00"));
    }
}
