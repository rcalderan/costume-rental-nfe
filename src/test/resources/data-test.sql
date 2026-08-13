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

INSERT INTO sefaz_status (code, message, category, first_seen_at) VALUES
    ('100', 'Autorizado o uso da NF-e',                'AUTHORIZED', CURRENT_TIMESTAMP),
    ('101', 'Cancelamento de NF-e homologado',          'CANCELLED',  CURRENT_TIMESTAMP),
    ('102', 'Inutilizacao de numero homologado',        'AUTHORIZED', CURRENT_TIMESTAMP),
    ('103', 'Lote recebido com sucesso',                'PROCESSING', CURRENT_TIMESTAMP),
    ('104', 'Lote processado',                          'PROCESSING', CURRENT_TIMESTAMP),
    ('105', 'Lote em processamento',                    'PROCESSING', CURRENT_TIMESTAMP),
    ('135', 'Evento registrado e vinculado a NF-e',     'AUTHORIZED', CURRENT_TIMESTAMP),
    ('150', 'Autorizado o uso da NF-e',                 'AUTHORIZED', CURRENT_TIMESTAMP),
    ('151', 'Cancelamento de NF-e homologado',          'CANCELLED',  CURRENT_TIMESTAMP),
    ('217', 'NF-e nao consta na base de dados da SEFAZ','REJECTED',   CURRENT_TIMESTAMP);
