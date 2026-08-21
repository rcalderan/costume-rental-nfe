-- V3: SEFAZ pode retornar cStat de 4 digitos (ex: 1115), entao estende as colunas de codigo.
ALTER TABLE fiscal_document DROP CONSTRAINT IF EXISTS fk_document_sefaz_status;
ALTER TABLE fiscal_document_event DROP CONSTRAINT IF EXISTS fk_event_sefaz_status;
ALTER TABLE fiscal_document_inutilization DROP CONSTRAINT IF EXISTS fk_inut_sefaz_status;

ALTER TABLE sefaz_status ALTER COLUMN code TYPE VARCHAR(4);
ALTER TABLE fiscal_document ALTER COLUMN sefaz_status_code TYPE VARCHAR(4);
ALTER TABLE fiscal_document_event ALTER COLUMN sefaz_status_code TYPE VARCHAR(4);
ALTER TABLE fiscal_document_inutilization ALTER COLUMN sefaz_status_code TYPE VARCHAR(4);

ALTER TABLE fiscal_document ADD CONSTRAINT fk_document_sefaz_status FOREIGN KEY (sefaz_status_code) REFERENCES sefaz_status(code);
ALTER TABLE fiscal_document_event ADD CONSTRAINT fk_event_sefaz_status FOREIGN KEY (sefaz_status_code) REFERENCES sefaz_status(code);
ALTER TABLE fiscal_document_inutilization ADD CONSTRAINT fk_inut_sefaz_status FOREIGN KEY (sefaz_status_code) REFERENCES sefaz_status(code);
