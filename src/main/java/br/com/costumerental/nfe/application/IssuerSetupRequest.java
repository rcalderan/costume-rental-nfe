package br.com.costumerental.nfe.application;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Pattern;

public record IssuerSetupRequest(
        @NotBlank(message = "CNPJ é obrigatório")
        @Pattern(regexp = "^\\d{14}$", message = "CNPJ deve conter 14 dígitos numéricos")
        String cnpj,

        @NotBlank(message = "Razão Social é obrigatória")
        String razaoSocial,

        String nomeFantasia,

        String ie,

        String im,

        @NotBlank(message = "CRT é obrigatório")
        @Pattern(regexp = "^[123]$", message = "CRT deve ser 1, 2 ou 3")
        String crt,

        String fone,

        @NotBlank(message = "Logradouro é obrigatório")
        String logradouro,

        @NotBlank(message = "Número é obrigatório")
        String numero,

        @NotBlank(message = "Bairro é obrigatório")
        String bairro,

        @NotBlank(message = "Código do município é obrigatório")
        @Pattern(regexp = "^\\d{7}$", message = "Código do município deve conter 7 dígitos numéricos (IBGE)")
        String municipioCodigo,

        @NotBlank(message = "Nome do município é obrigatório")
        String municipioNome,

        @NotBlank(message = "UF é obrigatória")
        @Pattern(regexp = "^[A-Z]{2}$", message = "UF deve ser sigla de 2 letras maiúsculas")
        String uf,

        @NotBlank(message = "CEP é obrigatório")
        @Pattern(regexp = "^\\d{8}$", message = "CEP deve conter 8 dígitos numéricos")
        String cep,

        @NotBlank(message = "Código do país é obrigatório")
        @Pattern(regexp = "^\\d{4}$", message = "Código do país deve conter 4 dígitos numéricos (ex: 1058)")
        String paisCodigo,

        @NotBlank(message = "Nome do país é obrigatório")
        String paisNome
) {
}
