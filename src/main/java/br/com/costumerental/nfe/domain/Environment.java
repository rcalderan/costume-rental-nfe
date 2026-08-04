package br.com.costumerental.nfe.domain;

public enum Environment {
    PRODUCTION("1"),
    HOMOLOGATION("2");

    private final String code;

    Environment(String code) {
        this.code = code;
    }

    public String getCode() {
        return code;
    }

    public static Environment fromCode(String code) {
        for (Environment env : values()) {
            if (env.code.equals(code)) {
                return env;
            }
        }
        throw new IllegalArgumentException("Ambiente desconhecido: " + code);
    }
}
