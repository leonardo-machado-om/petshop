package br.com.leonardomachado.petshop.consultacep.client;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;

import br.com.leonardomachado.petshop.consultacep.dto.CepApiResponse;
import br.com.leonardomachado.petshop.consultacep.exception.ConsultaCepApiException;

@Component
public class WireMockCepApiClient implements CepApiClient {

	private RestClient restClient;
	
	public WireMockCepApiClient(
	        @Qualifier("cepApiRestClient") RestClient restClient) {
	    this.restClient = restClient;
	}

    @Override
    public CepApiResponse consultar(String cep) {
        try {
            CepApiResponse resposta = restClient.get()
                    .uri("/ws/{cep}/json/", cep)
                    .accept(MediaType.APPLICATION_JSON)
                    .retrieve()
                    .body(CepApiResponse.class);

            if (resposta == null) {
                throw new ConsultaCepApiException(
                        "A API externa retornou uma resposta vazia.");
            }

            return resposta;

        } catch (RestClientException exception) {
            throw new ConsultaCepApiException(
                    "Não foi possível consultar o serviço externo de CEP.",
                    exception);
        }
    }
}