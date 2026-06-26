package br.com.leonardomachado.petshop.consultacep.client;

import br.com.leonardomachado.petshop.consultacep.dto.CepApiResponse;

public interface CepApiClient {
    CepApiResponse consultar(String cep);
}