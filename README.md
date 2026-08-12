# costume-rental-nfe

Microserviço Spring Boot para emissão e eventos de NF-e (modelo 55) usando a biblioteca [Java_NFe](https://github.com/Samuel-Oliveira/Java_NFe).

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
