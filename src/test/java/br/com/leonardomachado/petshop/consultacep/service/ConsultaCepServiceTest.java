package br.com.leonardomachado.petshop.consultacep.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.ResponseEntity;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import br.com.leonardomachado.petshop.consultacep.client.CepApiClient;
import br.com.leonardomachado.petshop.consultacep.dto.CepApiResponse;
import br.com.leonardomachado.petshop.consultacep.entity.LogConsultaCep;
import br.com.leonardomachado.petshop.consultacep.entity.StatusConsultaCep;
import br.com.leonardomachado.petshop.consultacep.exception.CepInvalidoException;
import br.com.leonardomachado.petshop.consultacep.exception.CepNaoEncontradoException;
import br.com.leonardomachado.petshop.consultacep.exception.ConsultaCepApiException;
import br.com.leonardomachado.petshop.consultacep.repository.LogConsultaCepRepository;

@ExtendWith(MockitoExtension.class)
class ConsultaCepServiceTest {

    private static final Instant DATA_HORA_CONSULTA =
            Instant.parse("2026-06-26T03:00:00Z");

    @Mock
    private CepApiClient cepApiClient;

    @Mock
    private LogConsultaCepRepository logConsultaCepRepository;

    private ObjectMapper objectMapper;
    private ConsultaCepService consultaCepService;

    @BeforeEach
    void setUp() {
        objectMapper = new ObjectMapper();

        Clock clock = Clock.fixed(
                DATA_HORA_CONSULTA,
                ZoneOffset.UTC);

        consultaCepService = new ConsultaCepService(
                cepApiClient,
                logConsultaCepRepository,
                objectMapper,
                clock);
    }

    @Test
    void deveConsultarCepESalvarLogComSucesso() throws Exception {
        JsonNode jsonRetorno = objectMapper.readTree("""
                {
                  "cep": "11320-180",
                  "logradouro": "Rua Saldanha da Gama",
                  "bairro": "Itararé",
                  "localidade": "São Vicente",
                  "uf": "SP",
                  "erro": false
                }
                """);

        given(cepApiClient.consultar("11320180"))
                .willReturn(ResponseEntity.ok(jsonRetorno));

        CepApiResponse resposta =
                consultaCepService.consultar("11320-180");

        assertThat(resposta.cep()).isEqualTo("11320-180");
        assertThat(resposta.localidade()).isEqualTo("São Vicente");
        assertThat(resposta.uf()).isEqualTo("SP");

        ArgumentCaptor<LogConsultaCep> captor =
                ArgumentCaptor.forClass(LogConsultaCep.class);

        verify(logConsultaCepRepository).save(captor.capture());

        LogConsultaCep log = captor.getValue();

        assertThat(log.getCep()).isEqualTo("11320180");
        assertThat(log.getDataHoraConsulta())
                .isEqualTo(DATA_HORA_CONSULTA);
        assertThat(log.getStatus())
                .isEqualTo(StatusConsultaCep.SUCESSO);
        assertThat(log.getStatusHttp()).isEqualTo(200);
        assertThat(log.getJsonRetorno()).isEqualTo(jsonRetorno);
        assertThat(log.getDescricaoErro()).isNull();
    }

    @Test
    void deveSalvarLogELancarExcecaoQuandoCepNaoForEncontrado()
            throws Exception {

        JsonNode jsonRetorno = objectMapper.readTree("""
                {
                  "erro": true
                }
                """);

        given(cepApiClient.consultar("00000000"))
                .willReturn(ResponseEntity.ok(jsonRetorno));

        assertThatThrownBy(() ->
                consultaCepService.consultar("00000-000"))
                .isInstanceOf(CepNaoEncontradoException.class)
                .hasMessage("CEP 00000000 não encontrado.");

        ArgumentCaptor<LogConsultaCep> captor =
                ArgumentCaptor.forClass(LogConsultaCep.class);

        verify(logConsultaCepRepository).save(captor.capture());

        LogConsultaCep log = captor.getValue();

        assertThat(log.getStatus())
                .isEqualTo(StatusConsultaCep.CEP_NAO_ENCONTRADO);
        assertThat(log.getStatusHttp()).isEqualTo(200);
        assertThat(log.getJsonRetorno()).isEqualTo(jsonRetorno);
    }

    @Test
    void deveSalvarLogDeErroQuandoApiExternaFalhar() {
        ConsultaCepApiException exception =
                new ConsultaCepApiException(
                        "A API externa retornou um erro.",
                        503,
                        new RuntimeException());

        given(cepApiClient.consultar("11320180"))
                .willThrow(exception);

        assertThatThrownBy(() ->
                consultaCepService.consultar("11320-180"))
                .isSameAs(exception);

        ArgumentCaptor<LogConsultaCep> captor =
                ArgumentCaptor.forClass(LogConsultaCep.class);

        verify(logConsultaCepRepository).save(captor.capture());

        LogConsultaCep log = captor.getValue();

        assertThat(log.getStatus())
                .isEqualTo(StatusConsultaCep.ERRO_API_EXTERNA);
        assertThat(log.getStatusHttp()).isEqualTo(503);
        assertThat(log.getJsonRetorno()).isNull();
        assertThat(log.getDescricaoErro())
                .isEqualTo("A API externa retornou um erro.");
    }

    @Test
    void naoDeveChamarApiNemSalvarLogQuandoCepForInvalido() {
        assertThatThrownBy(() ->
                consultaCepService.consultar("123"))
                .isInstanceOf(CepInvalidoException.class)
                .hasMessage("O CEP deve possuir exatamente 8 dígitos.");

        verifyNoInteractions(cepApiClient, logConsultaCepRepository);
    }
}