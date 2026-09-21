-- V4: Campos NFS-e no emitente (cTribNac, NBS, alíquotas, descrição padrão).
-- Persistidos junto ao emitente em sistema/cnpj para que o microsserviço NFS-e
-- leia a mesma fonte de verdade da NF-e/NFC-e, sem duplicar configuração no browser.
ALTER TABLE nfe_issuer
    ADD COLUMN IF NOT EXISTS nfse_service_code        VARCHAR(6),
    ADD COLUMN IF NOT EXISTS nfse_nbs_code             VARCHAR(20),
    ADD COLUMN IF NOT EXISTS nfse_service_description  VARCHAR(255),
    ADD COLUMN IF NOT EXISTS nfse_iss_rate             NUMERIC(5,2),
    ADD COLUMN IF NOT EXISTS nfse_total_tax_rate       NUMERIC(5,2);

COMMENT ON COLUMN nfe_issuer.nfse_service_code IS 'Código de Tributação Nacional (6 dígitos) para NFS-e';
COMMENT ON COLUMN nfe_issuer.nfse_nbs_code IS 'Código NBS opcional para NFS-e';
COMMENT ON COLUMN nfe_issuer.nfse_service_description IS 'Descrição padrão do serviço para NFS-e';
COMMENT ON COLUMN nfe_issuer.nfse_iss_rate IS 'Alíquota de ISS (%) para NFS-e';
COMMENT ON COLUMN nfe_issuer.nfse_total_tax_rate IS 'Alíquota total do Simples Nacional (%) para NFS-e';
