package br.com.leonardomachado.petshop.consultacep.config;

import static org.assertj.core.api.Assertions.assertThat;

import java.net.URI;
import java.time.Duration;

import org.junit.jupiter.api.Test;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;
import org.springframework.context.annotation.Configuration;

class CepApiPropertiesTest {

    private final ApplicationContextRunner contextRunner =
            new ApplicationContextRunner()
                    .withUserConfiguration(PropertiesTestConfig.class);

    @Test
    void naoDeveIniciarSemBaseUrlObrigatoria() {
        contextRunner
                .withPropertyValues(
                        "app.cep.connect-timeout=2s",
                        "app.cep.read-timeout=3s")
                .run(context -> {
                    assertThat(context.getStartupFailure()).isNotNull();
                    assertThat(context.getStartupFailure())
                            .hasStackTraceContaining("baseUrl");
                });
    }

    @Test
    void deveIniciarComTodasAsPropriedadesObrigatorias() {
        contextRunner
                .withPropertyValues(
                        "app.cep.base-url=http://localhost:8082",
                        "app.cep.connect-timeout=2s",
                        "app.cep.read-timeout=3s")
                .run(context -> {
                    assertThat(context.getStartupFailure()).isNull();

                    CepApiProperties properties =
                            context.getBean(CepApiProperties.class);

                    assertThat(properties.baseUrl())
                            .isEqualTo(URI.create("http://localhost:8082"));
                    assertThat(properties.connectTimeout())
                            .isEqualTo(Duration.ofSeconds(2));
                    assertThat(properties.readTimeout())
                            .isEqualTo(Duration.ofSeconds(3));
                });
    }

    @Configuration(proxyBeanMethods = false)
    @EnableConfigurationProperties(CepApiProperties.class)
    static class PropertiesTestConfig {
    }
}