package com.bip.backend;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;

@Slf4j
@SpringBootApplication
@EntityScan("com.bip.ejb")
public class BackendApplication {

    public static void main(String[] args) {
        log.info("Iniciando BIP Benefícios Backend...");
        SpringApplication.run(BackendApplication.class, args);
        log.info("BIP Benefícios Backend em execução (porta padrão 8080)");
    }
}
