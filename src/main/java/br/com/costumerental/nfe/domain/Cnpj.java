package br.com.costumerental.nfe.domain;

import java.util.Objects;

/**
 * Value object para CNPJ de 14 digitos.
 * Decompoem o CNPJ em raiz (8), ordem da filial (4) e digitos de controle (2).
 *
 * Uso:
 *   Cnpj cnpj = Cnpj.parse("47960950000121");
 *   cnpj.root();      // "47960950"
 *   cnpj.branch();    // "0001"
 *   cnpj.dv();        // "21"
 *   cnpj.format();    // "47960950000121"
 */
public final class Cnpj {

    private static final int[] DV1_WEIGHTS = {5, 4, 3, 2, 9, 8, 7, 6, 5, 4, 3, 2};
    private static final int[] DV2_WEIGHTS = {6, 5, 4, 3, 2, 9, 8, 7, 6, 5, 4, 3, 2};

    private final String root;
    private final String branch;
    private final String dv;

    private Cnpj(String root, String branch, String dv) {
        this.root = root;
        this.branch = branch;
        this.dv = dv;
    }

    public String root() {
        return root;
    }

    public String branch() {
        return branch;
    }

    public String dv() {
        return dv;
    }

    public String format() {
        return root + branch + dv;
    }

    public boolean isMatriz() {
        return "0001".equals(branch);
    }

    public boolean isFilial() {
        return !isMatriz();
    }

    public boolean isDvValido() {
        return dv.equals(calcularDv(root, branch));
    }

    /**
     * Faz o parse de um CNPJ de 14 digitos numericos.
     * Nao valida os digitos de controle; use {@link #validar(String)} para isso.
     *
     * @throws IllegalArgumentException se o CNPJ nao tiver 14 digitos numericos
     */
    public static Cnpj parse(String cnpj14) {
        Objects.requireNonNull(cnpj14, "CNPJ nao pode ser nulo");
        String digits = cnpj14.replaceAll("\\D", "");
        if (digits.length() != 14) {
            throw new IllegalArgumentException(
                    "CNPJ deve conter 14 digitos numericos, mas tinha " + digits.length() + ": " + cnpj14);
        }
        return new Cnpj(
                digits.substring(0, 8),
                digits.substring(8, 12),
                digits.substring(12, 14)
        );
    }

    /**
     * Formata os componentes em um CNPJ de 14 digitos.
     */
    public static String format(String root, String branch, String dv) {
        return root + branch + dv;
    }

    /**
     * Calcula os 2 digitos de controle a partir da raiz e ordem da filial.
     *
     * @return string de 2 digitos
     */
    public static String calcularDv(String root, String branch) {
        String base = root + branch;
        if (base.length() != 12 || !base.matches("\\d{12}")) {
            throw new IllegalArgumentException(
                    "Raiz (8) + ordem (4) devem formar 12 digitos numericos, mas tinha: " + base);
        }
        int dv1 = calculateDigit(base, DV1_WEIGHTS);
        int dv2 = calculateDigit(base + dv1, DV2_WEIGHTS);
        return String.format("%02d", dv1 * 10 + dv2);
    }

    /**
     * Valida um CNPJ de 14 digitos: formato numerico e digitos de controle.
     *
     * @return true se o CNPJ e valido
     */
    public static boolean validar(String cnpj14) {
        try {
            Cnpj cnpj = parse(cnpj14);
            return cnpj.isDvValido();
        } catch (IllegalArgumentException e) {
            return false;
        }
    }

    private static int calculateDigit(String base, int[] weights) {
        int sum = 0;
        for (int i = 0; i < base.length(); i++) {
            int digit = Character.getNumericValue(base.charAt(i));
            sum += digit * weights[i];
        }
        int remainder = sum % 11;
        return remainder < 2 ? 0 : 11 - remainder;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Cnpj)) return false;
        Cnpj cnpj = (Cnpj) o;
        return root.equals(cnpj.root) && branch.equals(cnpj.branch) && dv.equals(cnpj.dv);
    }

    @Override
    public int hashCode() {
        return Objects.hash(root, branch, dv);
    }

    @Override
    public String toString() {
        return format();
    }
}
