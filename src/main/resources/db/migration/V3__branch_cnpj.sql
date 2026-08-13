-- V3: Normaliza nfe_issuer em empresa + nfe_issuer e adiciona suporte a filiais
-- CNPJ completo deixa de ser coluna fisica: root_cnpj(8) + branch_order(4) + digito_controle(2)

-- 1. Cria tabela empresa (dados compartilhados por raiz de CNPJ)
CREATE TABLE empresa (
    root_cnpj     VARCHAR(8)   NOT NULL PRIMARY KEY,
    razao_social  VARCHAR(255) NOT NULL,
    crt           VARCHAR(1)   NOT NULL,
    pais_codigo   VARCHAR(255) NOT NULL,
    pais_nome     VARCHAR(255) NOT NULL,
    matriz_cnpj   VARCHAR(14),
    created_at    TIMESTAMP    NOT NULL,
    updated_at    TIMESTAMP    NOT NULL
);

-- 2. Migra dados existentes de nfe_issuer para empresa (uma empresa por raiz)
INSERT INTO empresa (root_cnpj, razao_social, crt, pais_codigo, pais_nome, matriz_cnpj, created_at, updated_at)
SELECT
    SUBSTRING(cnpj, 1, 8)                AS root_cnpj,
    razao_social,
    crt,
    pais_codigo,
    pais_nome,
    MIN(cnpj)                             AS matriz_cnpj,
    MIN(created_at)                       AS created_at,
    NOW()                                 AS updated_at
FROM nfe_issuer
WHERE SUBSTRING(cnpj, 9, 4) = '0001'
GROUP BY
    SUBSTRING(cnpj, 1, 8),
    razao_social,
    crt,
    pais_codigo,
    pais_nome;

-- 3. Adiciona colunas decompostas em nfe_issuer
ALTER TABLE nfe_issuer
    ADD COLUMN empresa_root    VARCHAR(8),
    ADD COLUMN branch_order    VARCHAR(4),
    ADD COLUMN digito_controle VARCHAR(2);

-- 4. Preenche a partir do cnpj existente
UPDATE nfe_issuer
SET empresa_root    = SUBSTRING(cnpj, 1, 8),
    branch_order    = SUBSTRING(cnpj, 9, 4),
    digito_controle = SUBSTRING(cnpj, 12, 2)
WHERE empresa_root IS NULL;

-- 5. Remove coluna cnpj e colunas migradas para empresa
ALTER TABLE nfe_issuer
    DROP COLUMN cnpj,
    DROP COLUMN razao_social,
    DROP COLUMN crt,
    DROP COLUMN pais_codigo,
    DROP COLUMN pais_nome,
    ALTER COLUMN empresa_root    SET NOT NULL,
    ALTER COLUMN branch_order    SET NOT NULL,
    ALTER COLUMN digito_controle SET NOT NULL,
    ADD CONSTRAINT fk_issuer_empresa
        FOREIGN KEY (empresa_root) REFERENCES empresa (root_cnpj),
    ADD CONSTRAINT chk_issuer_branch_order
        CHECK (branch_order ~ '^[0-9]{4}$'),
    ADD CONSTRAINT chk_issuer_dv
        CHECK (digito_controle ~ '^[0-9]{2}$'),
    ADD CONSTRAINT uq_issuer_root_branch
        UNIQUE (empresa_root, branch_order);

-- 6. Indices
CREATE INDEX idx_issuer_empresa_root ON nfe_issuer (empresa_root);
CREATE INDEX idx_issuer_root_branch  ON nfe_issuer (empresa_root, branch_order);
