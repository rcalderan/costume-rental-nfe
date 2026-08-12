package br.com.costumerental.nfe.api.dto;

import lombok.Builder;

@Builder
public record IssuerResponse(
        String cnpj,
        String razaoSocial,
        String nomeFantasia,
        String ie,
        String im,
        String crt,
        String fone,
        String logradouro,
        String numero,
        String bairro,
        String municipioCodigo,
        String municipioNome,
        String uf,
        String cep,
        String paisCodigo,
        String paisNome,
        boolean certificateConfigured
) {
}
