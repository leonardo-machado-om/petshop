# Petshop CEP API

API REST desenvolvida para consultar CEPs na API pública do **ViaCEP** e registrar o resultado de cada consulta no PostgreSQL.

A aplicação valida o CEP antes de chamar o serviço externo, transforma as respostas em contratos HTTP claros e guarda um histórico das consultas realizadas.

## O que a API faz

* aceita CEP com ou sem hífen;
* valida o formato antes de chamar a API externa;
* consulta o ViaCEP;
* retorna os dados de endereço quando o CEP existe;
* devolve `404` quando o CEP é válido, mas não existe;
* grava no PostgreSQL o horário, resultado, status HTTP e dados retornados pela integração;
* padroniza respostas de erro com `ProblemDetail`;
* registra no log o início, fim, status e duração das requisições HTTP;
* disponibiliza documentação interativa com Swagger/OpenAPI;
* expõe health check local pelo Spring Boot Actuator.

## Arquitetura e fluxo

```text
Cliente / Swagger
       |
       v
ConsultaCepController
       |
       v
ConsultaCepService
       |--------------------------------|
       v                                v
CepApiClient                  LogConsultaCepRepository
(ViaCepApiClient)                      |
       |                                v
       v                           PostgreSQL
API pública ViaCEP
```

1. O cliente chama `GET /api/ceps/{cep}`.
2. O `ConsultaCepController` encaminha a chamada para o `ConsultaCepService`.
3. O service valida e normaliza o CEP informado.
4. O `ViaCepApiClient` consulta o ViaCEP.
5. O service interpreta a resposta, registra a consulta no banco e devolve o resultado ao controller.
6. O controller responde ao cliente.

## Tecnologias

* Java 17;
* Spring Boot;
* Spring Web;
* Spring Data JPA;
* PostgreSQL;
* Flyway;
* Springdoc OpenAPI / Swagger UI;
* Spring Boot Actuator;
* Docker Compose;
* JUnit 5 e Mockito;
* Lombok.

## Pré-requisitos

* Java 17 ou superior;
* Docker e Docker Compose;
* Git, caso queira clonar o projeto.

## Configuração local

Crie um arquivo `.env` a partir do `.env.example` na raiz do projeto.

No Windows:

```bat
copy .env.example .env
```

No Linux ou macOS:

```bash
cp .env.example .env
```

Use estas variáveis para executar com o ViaCEP real:

```env
DB_HOST=localhost
DB_PORT=5432
DB_NAME=petshop_db
DB_USERNAME=petshop_user
DB_PASSWORD=petshop_password
CEP_API_BASE_URL=https://viacep.com.br
```

A URL da integração é configurável. Dessa forma, a aplicação pode apontar futuramente para uma implementação compatível, como um mock local, sem alterar a regra de negócio.

## Como executar

### 1. Subir o PostgreSQL

Na raiz do projeto:

```bash
docker compose up -d
```

O banco fica disponível localmente na porta `5432`.

### 2. Iniciar a aplicação

No Windows:

```bat
mvnw.cmd spring-boot:run
```

No Linux ou macOS:

```bash
./mvnw spring-boot:run
```

A API ficará disponível em:

```text
http://localhost:8080
```

## Swagger

Com a aplicação em execução, a documentação interativa fica disponível em:

```text
http://localhost:8080/swagger-ui/index.html
```

## Endpoint

### Consultar CEP

```http
GET /api/ceps/{cep}
```

Exemplos válidos:

```text
GET /api/ceps/11320-180
GET /api/ceps/11320180
```

Teste pelo terminal no Windows:

```bat
curl.exe -i http://localhost:8080/api/ceps/11320180
```

## Respostas da API

### CEP encontrado — `200 OK`

```json
{
  "cep": "11320-180",
  "logradouro": "Rua Saldanha da Gama",
  "complemento": "",
  "unidade": "",
  "bairro": "Itararé",
  "localidade": "São Vicente",
  "uf": "SP",
  "estado": "São Paulo",
  "regiao": "Sudeste",
  "ibge": "3551009",
  "gia": "6579",
  "ddd": "13",
  "siafi": "7121",
  "erro": false
}
```

### CEP inválido — `400 Bad Request`

```json
{
  "type": "urn:petshop:cep-errors:cep-invalido",
  "title": "CEP inválido",
  "status": 400,
  "detail": "O CEP deve possuir exatamente 8 dígitos.",
  "instance": "/api/ceps/123",
  "codigo": "CEP_INVALIDO"
}
```

### CEP não encontrado — `404 Not Found`

```json
{
  "type": "urn:petshop:cep-errors:servidor-indisponivel",
  "title": "CEP não encontrado",
  "status": 404,
  "detail": "CEP 00000000 não encontrado.",
  "instance": "/api/ceps/00000000",
  "codigo": "CEP_NAO_ENCONTRADO"
}
```

O ViaCEP retorna `200 OK` com `{"erro": true}` quando um CEP possui formato válido, mas não existe. A aplicação interpreta esse retorno e responde `404 Not Found` para quem consome a API.

### Falha na integração externa — `503 Service Unavailable`

```json
{
  "type": "urn:petshop:cep-errors:servidor-indisponivel",
  "title": "Serviço de CEP indisponivel no momento, tente mais tarde.",
  "status": 503,
  "detail": "Não foi possível consultar o serviço externo de CEP.",
  "instance": "/api/ceps/11320180",
  "codigo": "CEP_PROVIDER_UNAVAILABLE"
}
```

### Erro inesperado — `500 Internal Server Error`

```json
{
  "type": "urn:petshop:cep-errors:internal-server-error",
  "title": "Erro interno do servidor",
  "status": 500,
  "instance": "/api/ceps/11320180",
  "detail": "Ocorreu um erro interno. Tente novamente mais tarde."
}
```

Recursos inexistentes, como `/favicon.ico`, retornam `404 Not Found` sem corpo, pois não fazem parte da API.

## Persistência das consultas

O Flyway cria a tabela `log_consulta_cep`.

Cada consulta válida gera um registro com:

* CEP consultado;
* data e hora da consulta;
* status da operação;
* status HTTP retornado pela integração, quando disponível;
* JSON devolvido pela API externa para consultas concluídas;
* mensagem do erro quando houver falha de integração.

Os status registrados são:

```text
SUCESSO
CEP_NAO_ENCONTRADO
ERRO_API_EXTERNA
```

## Logs de requisição

As chamadas para `/api/**` passam por um interceptor que registra o início e o fim da requisição, incluindo identificador, método, URI, status e duração.

Exemplo:

```text
Iniciando requisição. requestId=..., request=GET /api/ceps/11320180
Finalizando requisição. requestId=..., request=GET /api/ceps/11320180, status=200, duracaoMs=42
```

Também são registrados no log:

* `WARN` para CEP inválido;
* `INFO` para CEP não encontrado;
* `ERROR` com stack trace para falhas na integração externa;
* `ERROR` com stack trace para erros inesperados.

## Health check

O Actuator é exposto apenas localmente:

```text
http://127.0.0.1:8081/actuator/health
http://127.0.0.1:8081/actuator/info
```

## Testes

Para executar todos os testes:

No Windows:

```bat
mvnw.cmd clean test
```

No Linux ou macOS:

```bash
./mvnw clean test
```

A suíte cobre, entre outros cenários:

* consulta com sucesso;
* CEP inválido;
* CEP inexistente;
* indisponibilidade da API externa;
* respostas HTTP `200`, `400`, `404` e `503` no controller.

## Organização do código

```text
controller
└── expõe os endpoints HTTP

service
├── valida o CEP
├── coordena a consulta
└── persiste o histórico da operação

client
└── encapsula a comunicação com o ViaCEP

repository
└── persiste os logs no PostgreSQL

handler
└── transforma exceções em respostas HTTP padronizadas
```

## Decisões adotadas

* O CEP é validado antes da integração externa, evitando chamadas desnecessárias.
* O client HTTP possui timeout de conexão de 2 segundos e timeout de leitura de 3 segundos.
* O schema é versionado pelo Flyway e validado pelo Hibernate, sem criação automática de tabelas.
* A interface `CepApiClient` separa o contrato de integração da implementação concreta do ViaCEP.
* O `Clock` é injetado no service para permitir testes determinísticos.
* PostgreSQL e Actuator ficam restritos ao ambiente local durante o desenvolvimento.
* Cada erro da API possui um `type` específico para facilitar a identificação pelo consumidor.

## Melhorias futuras

* adicionar rate limit ao endpoint;
* usar Testcontainers nos testes de integração;
* publicar métricas em uma ferramenta de observabilidade;
* utilizar credenciais obrigatórias e banco gerenciado em produção;
* adicionar autenticação caso a API deixe de ser pública.
* * disponibilizar documentação para cada tipo de erro e utilizar suas URLs no campo `type` do `ProblemDetail`;
