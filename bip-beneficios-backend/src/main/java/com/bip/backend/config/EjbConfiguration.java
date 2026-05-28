package com.bip.backend.config;

import com.bip.ejb.BeneficioEjbService;
import javax.persistence.EntityManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class EjbConfiguration {

    @Bean
    public BeneficioEjbService beneficioEjbService(EntityManager entityManager) {
        return new BeneficioEjbService(entityManager);
    }
}
