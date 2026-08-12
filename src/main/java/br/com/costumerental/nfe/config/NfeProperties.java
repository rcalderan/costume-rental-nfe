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

        private String cnpj;

        private String ie;

        private String im;

        private String uf;

        private String razaoSocial;

        private String nomeFantasia;

        private String fone;

        private String crt;

        private EnderecoProperties endereco;
    }

    @Getter
    @Setter
    public static class EnderecoProperties {

        private String logradouro;

        private String numero;

        private String bairro;

        private String municipioCodigo;

        private String municipioNome;

        private String uf;

        private String cep;

        private String paisCodigo;

        private String paisNome;
    }

    @Getter
    @Setter
    public static class CertificateProperties {

        private String path;

        private String password;

        @NotBlank
        private String tipo;
    }
}
