# costume-rental-nfe

Microserviço Spring Boot para emissão e eventos de NF-e (modelo 55) e NFC-e (modelo 65) usando a biblioteca [Java_NFe](https://github.com/Samuel-Oliveira/Java_NFe).

## NFC-e (modelo 65)

Defina `NFE_MODELO=65` para emitir NFC-e em vez de NF-e. Nesse modo sao obrigatorias as URLs de QRCode e consulta por chave do estado do emitente (secoes `NFCe_<UF>_H`/`NFCe_<UF>_P` do `WebServicesNfe.ini` da Java_NFe):

```bash
export NFE_MODELO=65
export NFE_NFCE_QRCODE_URL=https://www.homologacao.nfce.fazenda.sp.gov.br/qrcode
export NFE_NFCE_CONSULTA_URL=https://www.homologacao.nfce.fazenda.sp.gov.br/consulta
```

O QRCode (padrao V3 online, NT 2025-001) e a URL de consulta sao embutidos no XML antes da assinatura e retornados em `qrCode`/`consultaUrl` na resposta de `/api/v1/nfe/emit`.

## Fase atual

**Fase 1** concluída: fundação + emissão em homologação com assinatura, validação de schema e envio à SEFAZ.

## Requisitos

- Java 21
- Maven 3.9+
- Certificado digital A1 (PKCS12 `.pfx`/`.p12`)

## Configuração

O CNPJ e IE do emitente são lidos da tabela `nfe_issuer` (banco `costume_rental_nfe`).
Cadastre/ative o emitente via banco ou pelo endpoint de upload de certificado.

Defina as demais variáveis de ambiente antes de rodar:

```bash
export NFE_EMIT_UF=SP
export NFE_AMBIENTE=2
export NFSE_CERT_PATH=/caminho/certificado.pfx
export NFSE_CERT_PASSWORD=senha
```

## Build

```bash
mvn clean package
```

## Testes

```bash
mvn test
```

## Executar localmente

```bash
mvn spring-boot:run -Dspring-boot.run.profiles=local
```

Acesse o Swagger em `http://localhost:8081/swagger-ui.html`.

## Endpoints

- `POST /api/v1/nfe/emit` — emite uma NF-e em homologação
- `POST /api/v1/nfe/sign-only` — constrói e assina o XML sem enviar à SEFAZ

## Estrutura

- `config/` — propriedades (`NfeProperties`)
- `infrastructure/certificado/` — carregamento do certificado A1
- `infrastructure/sefaz/` — adapter para a biblioteca `Java_NFe`
- `application/` — serviços de emissão e eventos
- `api/` — controllers REST
- `xml/` — montagem do XML da NF-e
- `domain/` — enums (ambiente, status, tipo de documento)
- `exception/` — handlers e exceções de negócio

## Próximas fases

1. Fase 2 — consulta, persistência e produção
2. Fase 3 — eventos (cancelamento, carta de correção, inutilização, manifestação)
3. Fase 4 — documentação OpenAPI, observabilidade, Docker e CI/CD
