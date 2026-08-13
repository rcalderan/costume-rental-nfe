-- V4: Renomeia empresa -> firm, move branch_order e digito_controle para firm, converte PKs para UID
-- firm: uma por estabelecimento (root + branch + digit)
-- nfe_issuer: dados operacionais do emitente, referencia firm.id

-- 1. Renomeia empresa -> firm
ALTER TABLE empresa RENAME TO firm;

-- 2. Adiciona id UUID em firm como nova PK
ALTER TABLE firm DROP CONSTRAINT empresa_pkey CASCADE;
ALTER TABLE firm ADD COLUMN id UUID DEFAULT gen_random_uuid() NOT NULL;
ALTER TABLE firm ADD CONSTRAINT pk_firm PRIMARY KEY (id);

-- 3. Move branch_order e digit (ex-digito_controle) para firm
ALTER TABLE firm ADD COLUMN branch_order VARCHAR(4);
ALTER TABLE firm ADD COLUMN digit VARCHAR(2);

UPDATE firm f
SET branch_order = i.branch_order,
    digit        = i.digito_controle
FROM nfe_issuer i
WHERE i.empresa_root = f.root_cnpj;

ALTER TABLE firm ALTER COLUMN branch_order SET NOT NULL;
ALTER TABLE firm ALTER COLUMN digit SET NOT NULL;
ALTER TABLE firm ADD CONSTRAINT chk_firm_branch_order CHECK (branch_order ~ '^[0-9]{4}$');
ALTER TABLE firm ADD CONSTRAINT chk_firm_digit CHECK (digit ~ '^[0-9]{2}$');

-- 4. Adiciona firm_id em nfe_issuer e popula
ALTER TABLE nfe_issuer ADD COLUMN firm_id UUID;

UPDATE nfe_issuer i
SET firm_id = f.id
FROM firm f
WHERE f.root_cnpj = i.empresa_root;

ALTER TABLE nfe_issuer ALTER COLUMN firm_id SET NOT NULL;

-- 5. Remove constraints e colunas migradas (fk_issuer_empresa ja caiu com CASCADE no passo 2)
ALTER TABLE nfe_issuer DROP CONSTRAINT IF EXISTS uq_issuer_root_branch;
ALTER TABLE nfe_issuer DROP CONSTRAINT IF EXISTS chk_issuer_branch_order;
ALTER TABLE nfe_issuer DROP CONSTRAINT IF EXISTS chk_issuer_dv;
DROP INDEX idx_issuer_empresa_root;
DROP INDEX idx_issuer_root_branch;
ALTER TABLE nfe_issuer DROP COLUMN empresa_root;
ALTER TABLE nfe_issuer DROP COLUMN branch_order;
ALTER TABLE nfe_issuer DROP COLUMN digito_controle;

-- 6. Nova FK nfe_issuer.firm_id -> firm.id
ALTER TABLE nfe_issuer ADD CONSTRAINT fk_issuer_firm
    FOREIGN KEY (firm_id) REFERENCES firm (id);
ALTER TABLE nfe_issuer ADD CONSTRAINT uq_issuer_firm UNIQUE (firm_id);

-- 7. Converte nfe_issuer.id de bigint para UUID
-- Remove FKs que apontam para nfe_issuer.id
ALTER TABLE fiscal_document DROP CONSTRAINT fk_document_issuer;
ALTER TABLE fiscal_document_inutilization DROP CONSTRAINT fk_inut_issuer;
ALTER TABLE fiscal_document_number_control DROP CONSTRAINT fk_number_control_issuer;

-- Remove PK e sequencia antiga
ALTER TABLE nfe_issuer DROP CONSTRAINT nfe_issuer_pkey;
ALTER TABLE nfe_issuer ALTER COLUMN id DROP DEFAULT;
DROP SEQUENCE IF EXISTS nfe_issuer_id_seq;

-- Converte id para UUID preservando valores
ALTER TABLE nfe_issuer ALTER COLUMN id TYPE UUID USING gen_random_uuid();
ALTER TABLE nfe_issuer ALTER COLUMN id SET DEFAULT gen_random_uuid();
ALTER TABLE nfe_issuer ADD CONSTRAINT pk_nfe_issuer PRIMARY KEY (id);

-- 8. Converte colunas que referenciam nfe_issuer.id para UUID
ALTER TABLE fiscal_document ALTER COLUMN issuer_id TYPE UUID USING gen_random_uuid();
ALTER TABLE fiscal_document ADD CONSTRAINT fk_document_issuer
    FOREIGN KEY (issuer_id) REFERENCES nfe_issuer (id);

ALTER TABLE fiscal_document_inutilization ALTER COLUMN issuer_id TYPE UUID USING gen_random_uuid();
ALTER TABLE fiscal_document_inutilization ADD CONSTRAINT fk_inut_issuer
    FOREIGN KEY (issuer_id) REFERENCES nfe_issuer (id);

ALTER TABLE fiscal_document_number_control ALTER COLUMN issuer_id TYPE UUID USING gen_random_uuid();
ALTER TABLE fiscal_document_number_control ADD CONSTRAINT fk_number_control_issuer
    FOREIGN KEY (issuer_id) REFERENCES nfe_issuer (id);

-- 9. Indices
CREATE INDEX idx_issuer_firm_id ON nfe_issuer (firm_id);
CREATE INDEX idx_firm_root_branch ON firm (root_cnpj, branch_order);
ALTER TABLE firm ADD CONSTRAINT uq_firm_root_branch UNIQUE (root_cnpj, branch_order);
