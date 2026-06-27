package br.com.leonardomachado.petshop.consultacep;

import static com.github.tomakehurst.wiremock.client.WireMock.getRequestedFor;
import static com.github.tomakehurst.wiremock.client.WireMock.urlEqualTo;
import static com.github.tomakehurst.wiremock.core.WireMockConfiguration.wireMockConfig;
import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import com.github.tomakehurst.wiremock.junit5.WireMockExtension;

import br.com.leonardomachado.petshop.consultacep.entity.LogConsultaCep;
import br.com.leonardomachado.petshop.consultacep.entity.StatusConsultaCep;
import br.com.leonardomachado.petshop.consultacep.repository.LogConsultaCepRepository;

@Testcontainers
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class ConsultaCepIntegrationTest {

    private static final PostgreSQLContainer<?> POSTGRE_SQL_CONTAINER = new PostgreSQLContainer<>(
            "postgres:16-alpine");

	@Container
    static final PostgreSQLContainer<?> postgres = POSTGRE_SQL_CONTAINER
                    .withDatabaseName("petshop_test")
                    .withUsername("petshop_test")
                    .withPassword("petshop_test");

    @RegisterExtension
    static final WireMockExtension wireMock = WireMockExtension.newInstance()
            .options(wireMockConfig()
                    .dynamicPort()
                    .usingFilesUnderClasspath("wiremock/consulta-cep"))
            .resetOnEachTest(false)
            .build();

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private LogConsultaCepRepository logConsultaCepRepository;

    @DynamicPropertySource
    static void configurarDependencias(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", postgres::getJdbcUrl);
        registry.add("spring.datasource.username", postgres::getUsername);
        registry.add("spring.datasource.password", postgres::getPassword);
        registry.add(
                "app.cep.base-url",
                () -> "http://localhost:"
                        + wireMock.getRuntimeInfo().getHttpPort());
    }

    @BeforeEach
    void prepararCenario() {
        wireMock.resetRequests();
        logConsultaCepRepository.deleteAllInBatch();
    }

    @Test
    void deveConsultarCepRegistrarLogEChamarProviderMockado()
            throws Exception {

        mockMvc.perform(get("/api/ceps/11320-180")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(
                        MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.cep").value("11320-180"))
                .andExpect(jsonPath("$.logradouro")
                        .value("Rua Saldanha da Gama"))
                .andExpect(jsonPath("$.localidade").value("São Vicente"))
                .andExpect(jsonPath("$.uf").value("SP"));

        wireMock.verify(
                1,
                getRequestedFor(urlEqualTo("/ws/11320180/json/")));

        LogConsultaCep log = obterLogUnico();

        assertThat(log.getCep()).isEqualTo("11320180");
        assertThat(log.getStatus()).isEqualTo(StatusConsultaCep.SUCESSO);
        assertThat(log.getStatusHttp()).isEqualTo(200);
        assertThat(log.getJsonRetorno()).isNotNull();
        assertThat(log.getJsonRetorno().path("cep").asText())
                .isEqualTo("11320-180");
        assertThat(log.getDescricaoErro()).isNull();
    }

    @Test
    void deveRetornarNotFoundERegistrarLogQuandoCepNaoExistir()
            throws Exception {

        mockMvc.perform(get("/api/ceps/00000-000")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound())
                .andExpect(content().contentTypeCompatibleWith(
                        MediaType.APPLICATION_PROBLEM_JSON))
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.codigo")
                        .value("CEP_NAO_ENCONTRADO"));

        wireMock.verify(
                1,
                getRequestedFor(urlEqualTo("/ws/00000000/json/")));

        LogConsultaCep log = obterLogUnico();

        assertThat(log.getCep()).isEqualTo("00000000");
        assertThat(log.getStatus())
                .isEqualTo(StatusConsultaCep.CEP_NAO_ENCONTRADO);
        assertThat(log.getStatusHttp()).isEqualTo(200);
        assertThat(log.getJsonRetorno()).isNotNull();
        assertThat(log.getJsonRetorno().path("erro").asBoolean()).isTrue();
        assertThat(log.getDescricaoErro()).isNull();
    }

    @Test
    void deveRetornarServiceUnavailableERegistrarUmLogQuandoProviderFalhar()
            throws Exception {

        mockMvc.perform(get("/api/ceps/99999-999")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isServiceUnavailable())
                .andExpect(content().contentTypeCompatibleWith(
                        MediaType.APPLICATION_PROBLEM_JSON))
                .andExpect(jsonPath("$.status").value(503))
                .andExpect(jsonPath("$.codigo")
                        .value("CEP_PROVIDER_UNAVAILABLE"));

        wireMock.verify(
                1,
                getRequestedFor(urlEqualTo("/ws/99999999/json/")));

        LogConsultaCep log = obterLogUnico();

        assertThat(log.getCep()).isEqualTo("99999999");
        assertThat(log.getStatus())
                .isEqualTo(StatusConsultaCep.ERRO_API_EXTERNA);
        assertThat(log.getStatusHttp()).isEqualTo(503);
        assertThat(log.getJsonRetorno()).isNull();
        assertThat(log.getDescricaoErro())
                .isEqualTo("A API externa retornou um erro.");
    }

    private LogConsultaCep obterLogUnico() {
        List<LogConsultaCep> logs = logConsultaCepRepository.findAll();

        assertThat(logs).hasSize(1);

        return logs.get(0);
    }
}
