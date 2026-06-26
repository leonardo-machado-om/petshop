CREATE TABLE log_consulta_cep (
    id UUID PRIMARY KEY,
    cep VARCHAR(8) NOT NULL,
    dh_consulta TIMESTAMP WITH TIME ZONE NOT NULL,
    status VARCHAR(30) NOT NULL,
    nr_status_http INTEGER,
    json_resposta_api JSONB,
    ds_erro VARCHAR(500)
);

CREATE INDEX idx_log_consulta_cep_dh_consulta
    ON log_consulta_cep (dh_consulta DESC);

CREATE INDEX idx_log_consulta_cep_cep
    ON log_consulta_cep (cep);
