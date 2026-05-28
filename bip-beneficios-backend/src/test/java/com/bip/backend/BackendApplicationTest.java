package com.bip.backend;

import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.verify;

import org.junit.jupiter.api.Test;
import org.springframework.boot.SpringApplication;
import org.springframework.context.ConfigurableApplicationContext;

class BackendApplicationTest {

    @Test
    void mainShouldStartSpringApplication() {
        try (var springApplication = mockStatic(SpringApplication.class)) {
            ConfigurableApplicationContext context = mock(ConfigurableApplicationContext.class);
            springApplication
                    .when(() -> SpringApplication.run(eq(BackendApplication.class), eq(new String[] {})))
                    .thenReturn(context);

            BackendApplication.main(new String[] {});

            springApplication.verify(() -> SpringApplication.run(BackendApplication.class, new String[] {}));
        }
    }
}
