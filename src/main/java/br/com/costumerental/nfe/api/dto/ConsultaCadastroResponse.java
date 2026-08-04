package br.com.costumerental.nfe.api.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ConsultaCadastroResponse implements Serializable {

    private String statusCode;
    private String statusMessage;
    private String uf;
    private String ie;
    private String cnpj;
    private String cpf;
    private List<CadastroInfo> registrations;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class CadastroInfo implements Serializable {
        private String ie;
        private String cnpj;
        private String cpf;
        private String razaoSocial;
        private String uf;
        private String regimeApuracaoIcms;
        private String situacaoContribuinte;
    }
}
