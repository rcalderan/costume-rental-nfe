package br.com.costumerental.nfe.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Pattern;

@Getter
@Setter
@Validated
@ConfigurationProperties(prefix = "nfe")
public class NfeProperties {

    @NotNull
    private EmitProperties emit;

    @NotNull
    private CertificateProperties certificate;

    @NotBlank
    @Pattern(regexp = "1|2", message = "Ambiente deve ser 1 (producao) ou 2 (homologacao)")
    private String ambiente;

    @NotBlank
    @Pattern(regexp = "\\d{1,3}", message = "Serie deve ser numerica de 1 a 3 digitos")
    private String serie;

    @NotBlank
    @Pattern(regexp = "55|65", message = "Modelo deve ser 55 (NF-e) ou 65 (NFC-e)")
    private String modelo;

    @NotBlank
    private String processoVersao;

    private boolean validacaoDocumento = true;

    @Getter
    @Setter
    public static class EmitProperties {

        @NotBlank
        @Pattern(regexp = "\\d{14}", message = "CNPJ deve conter 14 digitos")
        private String cnpj;

        private String ie;

        @NotBlank
        @Pattern(regexp = "[A-Z]{2}", message = "UF deve ser sigla de 2 letras maiusculas")
        private String uf;

        @NotBlank
        private String razaoSocial;

        private String nomeFantasia;

        private String fone;

        @NotBlank
        @Pattern(regexp = "[123]", message = "CRT deve ser 1, 2 ou 3")
        private String crt;

        @NotNull
        private EnderecoProperties endereco;
    }

    @Getter
    @Setter
    public static class EnderecoProperties {

        @NotBlank
        private String logradouro;

        @NotBlank
        private String numero;

        @NotBlank
        private String bairro;

        @NotBlank
        private String municipioCodigo;

        @NotBlank
        private String municipioNome;

        @NotBlank
        @Pattern(regexp = "[A-Z]{2}")
        private String uf;

        @NotBlank
        @Pattern(regexp = "\\d{8}", message = "CEP deve conter 8 digitos")
        private String cep;

        @NotBlank
        private String paisCodigo;

        @NotBlank
        private String paisNome;
    }

    @Getter
    @Setter
    public static class CertificateProperties {

        @NotBlank
        private String path;

        @NotBlank
        private String password;

        @NotBlank
        private String tipo;
    }
}
