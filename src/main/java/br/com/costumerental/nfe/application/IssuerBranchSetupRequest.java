package br.com.costumerental.nfe.application;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Pattern;

public record IssuerBranchSetupRequest(
        @NotBlank(message = "CNPJ da filial e obrigatorio")
        @Pattern(regexp = "^\\d{14}$", message = "CNPJ deve conter 14 digitos numericos")
        String cnpj,

        String nomeFantasia,

        String ie,

        String im,

        String fone,

        @NotBlank(message = "Logradouro e obrigatorio")
        String logradouro,

        @NotBlank(message = "Numero e obrigatorio")
        String numero,

        @NotBlank(message = "Bairro e obrigatorio")
        String bairro,

        @NotBlank(message = "Codigo do municipio e obrigatorio")
        String municipioCodigo,

        @NotBlank(message = "Nome do municipio e obrigatorio")
        String municipioNome,

        @NotBlank(message = "UF e obrigatoria")
        @Pattern(regexp = "^[A-Z]{2}$", message = "UF deve ser sigla de 2 letras maiusculas")
        String uf,

        @NotBlank(message = "CEP e obrigatorio")
        @Pattern(regexp = "^\\d{8}$", message = "CEP deve conter 8 digitos numericos")
        String cep,

        String certificatePath,

        String certificatePassword
) {
}
