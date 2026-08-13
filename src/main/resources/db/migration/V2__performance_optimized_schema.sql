-- V2: Schema otimizado para alta escala de NF-e
-- Banco vazio: pode recriar todas as tabelas

-- 1. Drop tabelas antigas
DROP TABLE IF EXISTS fiscal_document_event CASCADE;
DROP TABLE IF EXISTS fiscal_document_inutilization CASCADE;
DROP TABLE IF EXISTS fiscal_document CASCADE;
DROP TABLE IF EXISTS nfe_issuer CASCADE;

-- 2. Tabelas de dominio (SMALLINT PK para economia e joins rapidos)
CREATE TABLE nfe_status (
    id          SMALLSERIAL PRIMARY KEY,
    code        VARCHAR(3)  NOT NULL UNIQUE,
    description VARCHAR(50) NOT NULL,
    category    VARCHAR(20) NOT NULL
);

CREATE TABLE nfe_document_type (
    id          SMALLSERIAL PRIMARY KEY,
    code        VARCHAR(10) NOT NULL UNIQUE,
    description VARCHAR(50) NOT NULL
);

CREATE TABLE nfe_event_type (
    id          SMALLSERIAL PRIMARY KEY,
    code        VARCHAR(6)  NOT NULL UNIQUE,
    description VARCHAR(50) NOT NULL
);

-- 3. SefazStatus: normaliza mensagens da SEFAZ (code = PK)
CREATE TABLE sefaz_status (
    code          VARCHAR(3)   NOT NULL PRIMARY KEY,
    message       VARCHAR(500),
    category      VARCHAR(20),
    first_seen_at TIMESTAMP    NOT NULL DEFAULT NOW()
);

-- 4. NfeIssuer com surrogate key BIGINT
CREATE TABLE nfe_issuer (
    id                 BIGSERIAL    PRIMARY KEY,
    cnpj               VARCHAR(14)  NOT NULL UNIQUE,
    razao_social       VARCHAR(255) NOT NULL,
    nome_fantasia      VARCHAR(255),
    ie                 VARCHAR(20),
    im                 VARCHAR(20),
    crt                VARCHAR(1)   NOT NULL,
    fone               VARCHAR(20),
    logradouro         VARCHAR(255) NOT NULL,
    numero             VARCHAR(255) NOT NULL,
    bairro             VARCHAR(255) NOT NULL,
    municipio_codigo   VARCHAR(255) NOT NULL,
    municipio_nome     VARCHAR(255) NOT NULL,
    uf                 VARCHAR(2)   NOT NULL,
    cep                VARCHAR(8)   NOT NULL,
    pais_codigo        VARCHAR(255) NOT NULL,
    pais_nome          VARCHAR(255) NOT NULL,
    certificate_path   VARCHAR(500),
    encrypted_password VARCHAR(500),
    certificate_tipo   VARCHAR(10),
    active             BOOLEAN      NOT NULL DEFAULT TRUE,
    created_at         TIMESTAMP    NOT NULL,
    updated_at         TIMESTAMP    NOT NULL
);
CREATE INDEX idx_nfe_issuer_cnpj ON nfe_issuer (cnpj);
CREATE INDEX idx_nfe_issuer_active ON nfe_issuer (active);

-- 5. Tabelas de XML (LOBs isolados das tabelas fato)
CREATE TABLE fiscal_document_xml (
    id         BIGSERIAL    PRIMARY KEY,
    xml_type   VARCHAR(20)  NOT NULL,
    content    TEXT,
    created_at TIMESTAMP    NOT NULL DEFAULT NOW()
);

CREATE TABLE fiscal_event_xml (
    id         BIGSERIAL    PRIMARY KEY,
    xml_type   VARCHAR(20)  NOT NULL,
    content    TEXT,
    created_at TIMESTAMP    NOT NULL DEFAULT NOW()
);

-- 6. Controle de numeracao por emitente/serie
CREATE TABLE fiscal_document_number_control (
    issuer_id   BIGINT      NOT NULL,
    series      VARCHAR(3)  NOT NULL,
    last_number BIGINT      NOT NULL DEFAULT 0,
    PRIMARY KEY (issuer_id, series),
    CONSTRAINT fk_number_control_issuer FOREIGN KEY (issuer_id) REFERENCES nfe_issuer (id)
);

-- 7. FiscalDocument (tabela fato)
CREATE TABLE fiscal_document (
    id               BIGSERIAL    PRIMARY KEY,
    access_key       VARCHAR(44)  NOT NULL UNIQUE,
    issuer_id        BIGINT       NOT NULL,
    document_type_id SMALLINT     NOT NULL,
    status_id        SMALLINT     NOT NULL,
    sefaz_status_code VARCHAR(3),
    signed_xml_id    BIGINT,
    authorized_xml_id BIGINT,
    protocol         VARCHAR(20),
    issue_date       TIMESTAMP,
    origin_id        VARCHAR(255),
    origin           VARCHAR(255),
    created_at       TIMESTAMP    NOT NULL,
    updated_at       TIMESTAMP    NOT NULL,
    CONSTRAINT fk_document_issuer        FOREIGN KEY (issuer_id)         REFERENCES nfe_issuer (id),
    CONSTRAINT fk_document_type          FOREIGN KEY (document_type_id)  REFERENCES nfe_document_type (id),
    CONSTRAINT fk_document_status        FOREIGN KEY (status_id)         REFERENCES nfe_status (id),
    CONSTRAINT fk_document_sefaz_status  FOREIGN KEY (sefaz_status_code) REFERENCES sefaz_status (code),
    CONSTRAINT fk_document_signed_xml    FOREIGN KEY (signed_xml_id)     REFERENCES fiscal_document_xml (id),
    CONSTRAINT fk_document_authorized_xml FOREIGN KEY (authorized_xml_id) REFERENCES fiscal_document_xml (id)
);
CREATE INDEX idx_document_issuer_status_date ON fiscal_document (issuer_id, status_id, issue_date);
CREATE INDEX idx_document_access_key ON fiscal_document (access_key);

-- 8. FiscalDocumentEvent
CREATE TABLE fiscal_document_event (
    id                  BIGSERIAL    PRIMARY KEY,
    fiscal_document_id  BIGINT       NOT NULL,
    event_type_id       SMALLINT     NOT NULL,
    sefaz_status_code   VARCHAR(3),
    event_xml_id        BIGINT,
    response_xml_id     BIGINT,
    sequence            VARCHAR(3),
    protocol            VARCHAR(20),
    created_at          TIMESTAMP    NOT NULL,
    updated_at          TIMESTAMP    NOT NULL,
    CONSTRAINT fk_event_document      FOREIGN KEY (fiscal_document_id)  REFERENCES fiscal_document (id),
    CONSTRAINT fk_event_type          FOREIGN KEY (event_type_id)       REFERENCES nfe_event_type (id),
    CONSTRAINT fk_event_sefaz_status  FOREIGN KEY (sefaz_status_code)   REFERENCES sefaz_status (code),
    CONSTRAINT fk_event_event_xml     FOREIGN KEY (event_xml_id)        REFERENCES fiscal_event_xml (id),
    CONSTRAINT fk_event_response_xml  FOREIGN KEY (response_xml_id)     REFERENCES fiscal_event_xml (id)
);
CREATE INDEX idx_event_document_type ON fiscal_document_event (fiscal_document_id, event_type_id);

-- 9. FiscalDocumentInutilization
CREATE TABLE fiscal_document_inutilization (
    id                BIGSERIAL    PRIMARY KEY,
    issuer_id         BIGINT       NOT NULL,
    inutilization_year VARCHAR(2)  NOT NULL,
    model             VARCHAR(2)   NOT NULL,
    series            VARCHAR(3)   NOT NULL,
    initial_number    VARCHAR(9)   NOT NULL,
    final_number      VARCHAR(9)   NOT NULL,
    justification     VARCHAR(255) NOT NULL,
    sefaz_status_code VARCHAR(3),
    response_xml_id   BIGINT,
    protocol          VARCHAR(20),
    created_at        TIMESTAMP    NOT NULL,
    updated_at        TIMESTAMP    NOT NULL,
    CONSTRAINT fk_inut_issuer        FOREIGN KEY (issuer_id)         REFERENCES nfe_issuer (id),
    CONSTRAINT fk_inut_sefaz_status  FOREIGN KEY (sefaz_status_code) REFERENCES sefaz_status (code),
    CONSTRAINT fk_inut_response_xml  FOREIGN KEY (response_xml_id)   REFERENCES fiscal_event_xml (id)
);
CREATE INDEX idx_inut_issuer_year_series ON fiscal_document_inutilization (issuer_id, inutilization_year, series);

-- 10. Seed de dominios iniciais
INSERT INTO nfe_status (code, description, category) VALUES
    ('AUT', 'AUTHORIZED',  'OK'),
    ('DEN', 'DENIED',       'DENIED'),
    ('REJ', 'REJECTED',     'REJECTED'),
    ('PRC', 'PROCESSING',   'PROCESSING'),
    ('CAN', 'CANCELLED',    'CANCELLED'),
    ('ERR', 'ERROR',        'ERROR');

INSERT INTO nfe_document_type (code, description) VALUES
    ('NFE', 'Nota Fiscal Eletronica');

INSERT INTO nfe_event_type (code, description) VALUES
    ('CANC',  'Cancelamento'),
    ('CCE',   'Carta de Correcao'),
    ('MANIF', 'Manifestacao do Destinatario'),
    ('INUT',  'Inutilizacao');

-- 11. Seed de sefaz_status mais comuns
INSERT INTO sefaz_status (code, message, category) VALUES
    ('100', 'Autorizado o uso da NF-e',                'AUTHORIZED'),
    ('101', 'Cancelamento de NF-e homologado',          'CANCELLED'),
    ('102', 'Inutilizacao de numero homologado',        'AUTHORIZED'),
    ('103', 'Lote recebido com sucesso',                'PROCESSING'),
    ('104', 'Lote processado',                          'PROCESSING'),
    ('105', 'Lote em processamento',                    'PROCESSING'),
    ('135', 'Evento registrado e vinculado a NF-e',     'AUTHORIZED'),
    ('150', 'Autorizado o uso da NF-e',                 'AUTHORIZED'),
    ('151', 'Cancelamento de NF-e homologado',          'CANCELLED'),
    ('217', 'NF-e nao consta na base de dados da SEFAZ','REJECTED'),
    ('573', 'Cancelamento homologado fora de prazo',    'CANCELLED');
