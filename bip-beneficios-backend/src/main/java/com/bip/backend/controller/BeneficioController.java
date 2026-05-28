package com.bip.backend.controller;

import com.bip.backend.dto.BeneficioRequest;
import com.bip.backend.dto.BeneficioResponse;
import com.bip.backend.dto.TransferenciaRequest;
import com.bip.backend.dto.TransferenciaResponse;
import com.bip.backend.service.BeneficioApplicationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.net.URI;
import java.util.List;
import javax.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/beneficios")
@Tag(name = "Beneficios", description = "CRUD de beneficios e transferencia de valores")
@RequiredArgsConstructor
public class BeneficioController {

    private final BeneficioApplicationService service;

    @GetMapping
    @Operation(summary = "Lista beneficios")
    public List<BeneficioResponse> list() {
        return service.list();
    }

    @GetMapping("/{id}")
    @Operation(summary = "Busca beneficio por ID")
    public BeneficioResponse findById(@PathVariable Long id) {
        return service.findById(id);
    }

    @PostMapping
    @Operation(summary = "Cria beneficio")
    public ResponseEntity<BeneficioResponse> create(@Valid @RequestBody BeneficioRequest request) {
        BeneficioResponse response = service.create(request);
        return ResponseEntity
                .created(URI.create("/api/v1/beneficios/" + response.getId()))
                .body(response);
    }

    @PutMapping("/{id}")
    @Operation(summary = "Atualiza beneficio")
    public BeneficioResponse update(@PathVariable Long id, @Valid @RequestBody BeneficioRequest request) {
        return service.update(id, request);
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Remove beneficio")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        service.delete(id);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/transferencias")
    @Operation(summary = "Transfere valor entre beneficios")
    public TransferenciaResponse transfer(@Valid @RequestBody TransferenciaRequest request) {
        return service.transfer(request);
    }
}
