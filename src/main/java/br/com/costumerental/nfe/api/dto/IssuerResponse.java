package br.com.costumerental.nfe.api.dto;

import lombok.Builder;

@Builder
public record IssuerResponse(
        String cnpj,
        String rootCnpj,
        String branchOrder,
        String digitoControle,
        boolean matriz,
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
        boolean certificateConfigured,
        String nfseServiceCode,
        String nfseNbsCode,
        String nfseServiceDescription,
        java.math.BigDecimal nfseIssRate,
        java.math.BigDecimal nfseTotalTaxRate,
        boolean nfseSendIm
) {
}
