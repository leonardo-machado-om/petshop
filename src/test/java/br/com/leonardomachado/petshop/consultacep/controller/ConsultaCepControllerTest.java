package br.com.leonardomachado.petshop.consultacep.controller;

import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import br.com.leonardomachado.petshop.config.RequestLoggingInterceptor;
import br.com.leonardomachado.petshop.consultacep.dto.CepApiResponse;
import br.com.leonardomachado.petshop.consultacep.exception.CepInvalidoException;
import br.com.leonardomachado.petshop.consultacep.exception.CepNaoEncontradoException;
import br.com.leonardomachado.petshop.consultacep.exception.ConsultaCepApiException;
import br.com.leonardomachado.petshop.consultacep.service.ConsultaCepService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(controllers = ConsultaCepController.class)
@Import(RequestLoggingInterceptor.class)
class ConsultaCepControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private ConsultaCepService consultaCepService;

    @Test
    void deveRetornarOkQuandoCepForConsultadoComSucesso() throws Exception {
        CepApiResponse resposta = new CepApiResponse(
                "11320-180",
                "Rua Saldanha da Gama",
                null,
                null,
                "Itararé",
                "São Vicente",
                "SP",
                "São Paulo",
                "Sudeste",
                "3551009",
                null,
                "13",
                "7121",
                false);

        given(consultaCepService.consultar("11320-180"))
                .willReturn(resposta);

        mockMvc.perform(get("/api/ceps/11320-180"))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(
                        MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.cep").value("11320-180"))
                .andExpect(jsonPath("$.logradouro")
                        .value("Rua Saldanha da Gama"))
                .andExpect(jsonPath("$.bairro").value("Itararé"))
                .andExpect(jsonPath("$.localidade").value("São Vicente"))
                .andExpect(jsonPath("$.uf").value("SP"));

        verify(consultaCepService).consultar("11320-180");
    }

    @Test
    void deveRetornarBadRequestQuandoCepForInvalido() throws Exception {
        given(consultaCepService.consultar("123"))
                .willThrow(new CepInvalidoException(
                        "O CEP deve possuir exatamente 8 dígitos.",
                        "123"));

        mockMvc.perform(get("/api/ceps/123"))
                .andExpect(status().isBadRequest())
                .andExpect(content().contentTypeCompatibleWith(
                        MediaType.APPLICATION_PROBLEM_JSON))
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.title").value("CEP inválido"))
                .andExpect(jsonPath("$.detail")
                        .value("O CEP deve possuir exatamente 8 dígitos."))
                .andExpect(jsonPath("$.codigo").value("CEP_INVALIDO"));

        verify(consultaCepService).consultar("123");
    }

    @Test
    void deveRetornarNotFoundQuandoCepNaoForEncontrado() throws Exception {
        given(consultaCepService.consultar("00000-000"))
                .willThrow(new CepNaoEncontradoException("00000000"));

        mockMvc.perform(get("/api/ceps/00000-000"))
                .andExpect(status().isNotFound())
                .andExpect(content().contentTypeCompatibleWith(
                        MediaType.APPLICATION_PROBLEM_JSON))
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.title").value("CEP não encontrado"))
                .andExpect(jsonPath("$.detail")
                        .value("CEP 00000000 não encontrado."))
                .andExpect(jsonPath("$.codigo")
                        .value("CEP_NAO_ENCONTRADO"));

        verify(consultaCepService).consultar("00000-000");
    }

    @Test
    void deveRetornarServiceUnavailableQuandoApiExternaFalhar()
            throws Exception {

        given(consultaCepService.consultar("11320-180"))
                .willThrow(new ConsultaCepApiException(
                        "A API externa retornou um erro.",
                        503,
                        new RuntimeException("Falha externa")));

        mockMvc.perform(get("/api/ceps/11320-180"))
                .andExpect(status().isServiceUnavailable())
                .andExpect(content().contentTypeCompatibleWith(
                        MediaType.APPLICATION_PROBLEM_JSON))
                .andExpect(jsonPath("$.status").value(503))
                .andExpect(jsonPath("$.detail")
                        .value("Não foi possível consultar o serviço externo de CEP."))
                .andExpect(jsonPath("$.type")
                        .value("urn:petshop:cep-errors:servidor-indisponivel"))
                .andExpect(jsonPath("$.codigo")
                        .value("CEP_PROVIDER_UNAVAILABLE"));

        verify(consultaCepService).consultar("11320-180");
    }
}