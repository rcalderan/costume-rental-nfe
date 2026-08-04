package br.com.costumerental.nfe.domain;

import java.util.Arrays;

public enum ManifestacaoDestinatarioType {

    CONFIRMACAO_OPERACAO("Confirmacao da Operacao", "210200"),
    CIENCIA_OPERACAO("Ciencia da Operacao", "210210"),
    DESCONHECIMENTO_OPERACAO("Desconhecimento da Operacao", "210220"),
    OPERACAO_NAO_REALIZADA("Operacao nao Realizada", "210240");

    private final String description;
    private final String eventCode;

    ManifestacaoDestinatarioType(String description, String eventCode) {
        this.description = description;
        this.eventCode = eventCode;
    }

    public String getDescription() {
        return description;
    }

    public String getEventCode() {
        return eventCode;
    }

    public static ManifestacaoDestinatarioType fromCode(String code) {
        return Arrays.stream(values())
                .filter(type -> type.eventCode.equals(code))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Tipo de manifestacao desconhecido: " + code));
    }
}
