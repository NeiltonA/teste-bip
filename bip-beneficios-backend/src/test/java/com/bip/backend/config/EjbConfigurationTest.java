package com.bip.backend.config;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.mock;

import com.bip.ejb.BeneficioEjbService;
import javax.persistence.EntityManager;
import org.junit.jupiter.api.Test;

class EjbConfigurationTest {

    @Test
    void shouldCreateBeneficioEjbServiceBean() {
        EjbConfiguration configuration = new EjbConfiguration();
        EntityManager entityManager = mock(EntityManager.class);

        BeneficioEjbService service = configuration.beneficioEjbService(entityManager);

        assertNotNull(service);
    }
}
