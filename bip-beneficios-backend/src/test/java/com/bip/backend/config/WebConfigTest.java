package com.bip.backend.config;

import org.junit.jupiter.api.Test;
import org.springframework.web.servlet.config.annotation.CorsRegistry;

class WebConfigTest {

    @Test
    void shouldRegisterCorsForApiAndLocalhost() {
        WebConfig config = new WebConfig();
        CorsRegistry registry = new CorsRegistry();

        config.addCorsMappings(registry);
    }
}
