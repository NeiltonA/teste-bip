package com.bip.backend;

import static org.hamcrest.Matchers.greaterThanOrEqualTo;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class BeneficioControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    void shouldListSeedBeneficios() throws Exception {
        mockMvc.perform(get("/api/v1/beneficios"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()", greaterThanOrEqualTo(2)))
                .andExpect(jsonPath("$[0].nome").exists())
                .andExpect(jsonPath("$[0].valor").exists());
    }

    @Test
    void shouldGetBeneficioById() throws Exception {
        mockMvc.perform(get("/api/v1/beneficios/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.nome").value("Beneficio A"))
                .andExpect(jsonPath("$.valor").value(1000.00));
    }

    @Test
    void shouldReturn404WhenBeneficioNotFound() throws Exception {
        mockMvc.perform(get("/api/v1/beneficios/99999"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404));
    }

    @Test
    void shouldCreateUpdateAndDeleteBeneficio() throws Exception {
        mockMvc.perform(post("/api/v1/beneficios")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"nome\":\"Auxilio Transporte\",\"descricao\":\"Saldo mensal\",\"valor\":120.50,\"ativo\":true}"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.nome").value("Auxilio Transporte"))
                .andExpect(jsonPath("$.valor").value(120.50));

        mockMvc.perform(put("/api/v1/beneficios/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"nome\":\"Beneficio A Atualizado\",\"descricao\":\"Nova desc\",\"valor\":1100.00,\"ativo\":true}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.nome").value("Beneficio A Atualizado"))
                .andExpect(jsonPath("$.valor").value(1100.00));

        mockMvc.perform(delete("/api/v1/beneficios/2"))
                .andExpect(status().isNoContent());

        mockMvc.perform(get("/api/v1/beneficios/2"))
                .andExpect(status().isNotFound());
    }

    @Test
    void shouldRejectInvalidPayloadOnCreate() throws Exception {
        mockMvc.perform(post("/api/v1/beneficios")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"nome\":\"\",\"valor\":-1,\"ativo\":true}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400));
    }

    @Test
    void shouldTransferAndRejectInsufficientBalance() throws Exception {
        mockMvc.perform(post("/api/v1/beneficios/transferencias")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"origemId\":1,\"destinoId\":2,\"valor\":999999.00}"))
                .andExpect(status().isUnprocessableEntity())
                .andExpect(jsonPath("$.status").value(422));

        mockMvc.perform(post("/api/v1/beneficios/transferencias")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"origemId\":1,\"destinoId\":2,\"valor\":125.00}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.saldoOrigem").value(875.00))
                .andExpect(jsonPath("$.saldoDestino").value(625.00));
    }

    @Test
    void shouldReturn404WhenDeletingMissingBeneficio() throws Exception {
        mockMvc.perform(delete("/api/v1/beneficios/99999"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404));
    }

    @Test
    void shouldRejectTransferWithSameOriginAndDestination() throws Exception {
        mockMvc.perform(post("/api/v1/beneficios/transferencias")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"origemId\":1,\"destinoId\":1,\"valor\":10.00}"))
                .andExpect(status().isUnprocessableEntity());
    }
}
