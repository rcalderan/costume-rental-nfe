# Costume Rental NFe - Postman API Collection

Coleção Postman do microsserviço `costume-rental-nfe`, com massas de teste para os endpoints de emissão e assinatura de NF-e (modelo 55) via SEFAZ em homologação.

## 📋 Conteúdo

- **NF-e**
  - `Emitir NF-e - Sucesso (1 item)` — emissão completa com um item de locação
  - `Emitir NF-e - Sucesso (Multiplos Itens)` — emissão com 3 itens e destinatário PJ com IE
  - `Emitir NF-e - Erro Validacao (sem itens)` — espera `400` (lista de itens vazia)
  - `Emitir NF-e - Erro Validacao (destinatario sem documento)` — espera `400` (CPF/CNPJ em branco)
  - `Assinar XML (sign-only) - Sucesso` — constrói e assina o XML sem enviar à SEFAZ
- **NF-e Consultas** *(Fase 2 — consulta e persistência)*
  - `Status do Servico SEFAZ` — consulta o status do webservice de recepção de NF-e em homologação
  - `Consultar NF-e por Chave - Sucesso` — consulta a situação da NF-e pela chave de acesso salva por uma emissão anterior (`access_key`)
  - `Consultar NF-e por Chave - Nao Encontrada` — espera `404` (cStat `217`, chave inexistente na SEFAZ)
  - `Consultar Recibo` — consulta o protocolo de autorização a partir do número do recibo (`receipt_number`)
- **NF-e Eventos** *(Fase 3 — eventos da NF-e)*
  - `Cancelar NF-e` — envia evento de cancelamento para uma NF-e autorizada (`access_key` + `protocol`)
  - `Cancelar NF-e - Erro Validacao (justificativa curta)` — espera `400` (justificativa menor que 15 caracteres)
  - `Carta de Correcao` — envia evento de carta de correção para uma NF-e autorizada
  - `Carta de Correcao - Erro Validacao (correcao curta)` — espera `400` (correção menor que 15 caracteres)
  - `Inutilizar Numeracao` — solicita inutilização de uma faixa de numeração de NF-e
  - `Manifestacao do Destinatario` — envia manifestação do destinatário (`210200`, `210210`, `210220`, `210240`)
  - `Consulta Cadastro` — consulta cadastro de contribuinte na SEFAZ
  - `Distribuicao DFe` — consulta documentos fiscais eletrônicos distribuídos

## 🚀 Como Usar

### 1. Importar a Coleção

1. Abra o Postman
2. Clique em **Import**
3. Selecione o arquivo: `CostumeRentalNfe_API.postman_collection.json`

### 2. Importar o Environment

- Arquivo: `CostumeRentalNfe_Local.postman_environment.json`
- Base URL: `http://localhost:8081`

**Como importar:**
1. No Postman, clique no ícone de **Environments** (canto superior direito)
2. Clique em **Import** e selecione o arquivo
3. Ative o environment no dropdown

### 3. Subir a Aplicação Localmente

```bash
mvn spring-boot:run "-Dspring-boot.run.profiles=local"
```

> No PowerShell, o parâmetro `-D` precisa estar entre aspas, senão o Maven interpreta o `=` incorretamente.

A aplicação sobe em `http://localhost:8081`. Swagger disponível em `/swagger-ui.html`.

### 4. Testar os Endpoints

A collection usa **Bearer Token** herdado a nível de collection. Em ambiente local sem Caddy gateway, a API aceita requests sem token — basta deixar a variável `token` vazia no environment. Em **HK/produção**, o gateway Caddy repassa o header `Authorization` e o serviço NFe valida o JWT, então é necessário:

1. Fazer login no Rentafit: `POST http://<host>/api/auth/login` com `{ "username": "...", "password": "..." }`
2. Copiar o `accessToken` da resposta
3. Colar na variável `token` do environment

> **Atenção:** o endpoint `Emitir NF-e` depende de um certificado digital A1 válido (`NFSE_CERT_PATH`/`NFSE_CERT_PASSWORD`) e de conectividade com o webservice SEFAZ de homologação. Sem essas condições, use `Assinar XML (sign-only)` para validar apenas a montagem/assinatura do XML.

## 📁 Estrutura de Variáveis

### Collection Variables

| Variável | Descrição | Exemplo |
|----------|-----------|---------|
| `base_url` | URL base da API | `http://localhost:8081` |
| `access_key` | Chave de acesso da NF-e (salva ao emitir com sucesso) | `35...` |
| `protocol` | Protocolo de autorização (salvo ao emitir com sucesso) | `135...` |
| `signed_xml` | XML assinado (salvo pelo `sign-only`) | `<NFe>...</NFe>` |
| `receipt_number` | Número do recibo do lote (salvo ao emitir com sucesso, usado na consulta de recibo) | `351...` |
| `event_sequence` | Sequência do evento (cancelamento/carta de correção) | `1` |
| `cancel_justification` | Justificativa de cancelamento | `Cancelamento por erro...` |
| `correction_text` | Texto da carta de correção | `Correcao da descricao...` |
| `inutilization_year` | Ano para inutilização | `26` |
| `inutilization_initial` | Número inicial da faixa de inutilização | `1` |
| `inutilization_final` | Número final da faixa de inutilização | `5` |
| `manifestation_type` | Tipo de manifestação do destinatário | `210210` |
| `query_document` | CPF/CNPJ usado em consultas cadastrais e DFe | `52998224725` |
| `nsu_or_key` | NSU ou chave de acesso para distribuição DFe | `000000000000000` |
| `query_type` | Tipo de consulta DFe (`NSU`, `NSU_UNICO`, `CHAVE`) | `NSU` |
| `token` | JWT Bearer token do Rentafit (obrigatório em HK/prod via Caddy; vazio em local) | `eyJhbGciOi...` |

## 🔄 Massas de Teste

| Cenário | Request | Resultado esperado |
|---------|---------|---------------------|
| Emissão simples | 1 item, destinatário PF (CPF válido) | `201 Created`, `status: AUTHORIZED` (com SEFAZ/certificado configurados) |
| Emissão múltiplos itens | 3 itens, destinatário PJ (CNPJ válido) com IE | `201 Created` ou `422` (se SEFAZ rejeitar), conforme validações fiscais |
| Lista de itens vazia | `items: []` | `400 Bad Request` (`@NotEmpty`) |
| Documento do destinatário em branco | `customer.document: ""` | `400 Bad Request` (`@NotBlank`) |
| Assinatura sem envio | 1 item, `sign-only` | `200 OK` com `signedXml` preenchido, sem depender da SEFAZ |
| Status do serviço | `GET /status-servico` | `200 OK` com `statusCode`/`statusMessage` do webservice da SEFAZ |
| Consulta por chave (encontrada) | `GET /{chave}` com `access_key` de uma emissão anterior | `200 OK` com o status atual da NF-e (ex.: `AUTHORIZED`) |
| Consulta por chave (não encontrada) | `GET /{chave}` com chave inexistente | `404 Not Found` (cStat `217`) |
| Consulta de recibo | `GET /recibo/{nRec}` com `receipt_number` de uma emissão anterior | `200 OK` com o protocolo, ou `202 Accepted` se o lote ainda estiver em processamento (cStat `105`) |
| Cancelar NF-e | `POST /{chave}/cancelar` com `protocol` de emissão anterior | `200 OK` com status `AUTHORIZED`, ou `422` se a SEFAZ rejeitar |
| Cancelar NF-e - validação | `POST /{chave}/cancelar` com justificativa curta | `400 Bad Request` (`@Size` na justificativa) |
| Carta de Correção | `POST /{chave}/carta-correcao` com texto de correção | `200 OK` com evento registrado, ou `422` se rejeitado |
| Carta de Correção - validação | `POST /{chave}/carta-correcao` com correção curta | `400 Bad Request` (`@Size` na correção) |
| Inutilizar numeração | `POST /inutilizar` com faixa de numeração e justificativa | `200 OK` com status `AUTHORIZED` se homologado |
| Manifestação do destinatário | `POST /{chave}/manifestacao` com `eventCode` | `200 OK` com evento registrado |
| Consulta cadastro | `POST /consulta-cadastro` com UF e CPF/CNPJ | `200 OK` com dados do cadastro ou mensagem da SEFAZ |
| Distribuição DFe | `POST /distribuicao-dfe` com tipo, documento e NSU/chave | `200 OK` com documentos retornados pela SEFAZ |

> **Observação:** como a emissão é feita em modo síncrono (`indSinc=1`), a SEFAZ normalmente retorna o protocolo direto na emissão, sem gerar `receiptNumber`. O endpoint de consulta de recibo é mantido para cenários de contingência/processamento assíncrono.

## 🔐 Autenticação (Fase 5 — Caddy Gateway)

Em ambiente local, a API **não exige autenticação** — o NFe não possui Spring Security. Em HK/produção, o tráfego passa pelo **Caddy gateway** que repassa o header `Authorization` para o NFe; a validação do JWT é feita pelo próprio serviço NFe.

```
Frontend/Postman → :80 (Caddy) → proxy_pass → NFe :8081
```

Para testar via Postman em HK:
1. A collection já herda `Authorization: Bearer {{token}}` em todos os requests
2. Preencha a variável `token` com o `accessToken` obtido do login do Rentafit

## 🩺 Actuator / Health Check

O endpoint `GET /actuator/health` inclui um indicador customizado do certificado digital:
- **UP**: certificado válido, com dias até expiração
- **UP + warning**: certificado expira em menos de 30 dias
- **DOWN**: certificado expirado, não encontrado ou senha inválida

Em ambiente local, detalhes são exibidos sempre (`show-details: always`).

## 🔐 Configuração de Ambiente da Aplicação

Antes de testar a emissão real, defina as variáveis de ambiente (CNPJ do emitente, endereço e certificado A1) na sua shell/IDE. Veja o [README principal](../../../README.md) do projeto para detalhes.
