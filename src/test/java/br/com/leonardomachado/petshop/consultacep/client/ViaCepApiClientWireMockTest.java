package br.com.leonardomachado.petshop.consultacep.client;

import static com.github.tomakehurst.wiremock.client.WireMock.getRequestedFor;
import static com.github.tomakehurst.wiremock.client.WireMock.urlEqualTo;
import static com.github.tomakehurst.wiremock.core.WireMockConfiguration.wireMockConfig;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringBootConfiguration;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.flyway.FlywayAutoConfiguration;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;
import org.springframework.boot.autoconfigure.orm.jpa.HibernateJpaAutoConfiguration;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;

import com.fasterxml.jackson.databind.JsonNode;
import com.github.tomakehurst.wiremock.junit5.WireMockExtension;

import br.com.leonardomachado.petshop.consultacep.config.CepApiProperties;
import br.com.leonardomachado.petshop.consultacep.config.CepApiRestClientConfig;

@SpringBootTest(
        webEnvironment = SpringBootTest.WebEnvironment.NONE,
        classes = ViaCepApiClientWireMockTest.TestApplication.class)
@ActiveProfiles("test")
class ViaCepApiClientWireMockTest {

    @RegisterExtension
    static final WireMockExtension wireMock = WireMockExtension.newInstance()
            .options(wireMockConfig()
                    .dynamicPort()
                    .usingFilesUnderClasspath("wiremock/consulta-cep"))
            .resetOnEachTest(false)
            .build();

    @Autowired
    private ViaCepApiClient viaCepApiClient;

    @DynamicPropertySource
    static void configurarWireMock(DynamicPropertyRegistry registry) {
        registry.add(
                "app.cep.base-url",
                () -> "http://localhost:"
                        + wireMock.getRuntimeInfo().getHttpPort());
    }

    @Test
    void deveConsultarCepNoWireMock() {
        ResponseEntity<JsonNode> resposta =
                viaCepApiClient.consultar("11320180");

        assertEquals(HttpStatus.OK, resposta.getStatusCode());
        assertNotNull(resposta.getBody());
        assertEquals(
                "11320-180",
                resposta.getBody().path("cep").asText());

        wireMock.verify(getRequestedFor(
                urlEqualTo("/ws/11320180/json/")));
    }

    @SpringBootConfiguration
    @EnableAutoConfiguration(exclude = {
            DataSourceAutoConfiguration.class,
            HibernateJpaAutoConfiguration.class,
            FlywayAutoConfiguration.class
    })
    @EnableConfigurationProperties(CepApiProperties.class)
    @Import({
            CepApiRestClientConfig.class,
            ViaCepApiClient.class
    })
    static class TestApplication {
    }
}