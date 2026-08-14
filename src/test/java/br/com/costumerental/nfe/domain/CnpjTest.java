package br.com.costumerental.nfe.domain;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class CnpjTest {

    @Test
    void shouldParseValidCnpjIntoComponents() {
        Cnpj cnpj = Cnpj.parse("47960950000121");
        assertThat(cnpj.root()).isEqualTo("47960950");
        assertThat(cnpj.branch()).isEqualTo("0001");
        assertThat(cnpj.dv()).isEqualTo("21");
        assertThat(cnpj.format()).isEqualTo("47960950000121");
    }

    @Test
    void shouldParseCnpjWithFormattingChars() {
        Cnpj cnpj = Cnpj.parse("47.960.950/0001-21");
        assertThat(cnpj.root()).isEqualTo("47960950");
        assertThat(cnpj.branch()).isEqualTo("0001");
        assertThat(cnpj.dv()).isEqualTo("21");
    }

    @Test
    void shouldRejectCnpjWithWrongLength() {
        assertThatThrownBy(() -> Cnpj.parse("12345678"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("14 digitos");
    }

    @Test
    void shouldRejectNullCnpj() {
        assertThatThrownBy(() -> Cnpj.parse(null))
                .isInstanceOf(NullPointerException.class);
    }

    @Test
    void shouldIdentifyMatrizAndFilial() {
        Cnpj matriz = Cnpj.parse("47960950000121");
        assertThat(matriz.isMatriz()).isTrue();
        assertThat(matriz.isFilial()).isFalse();

        Cnpj filial = Cnpj.parse("47960950000202");
        assertThat(filial.isMatriz()).isFalse();
        assertThat(filial.isFilial()).isTrue();
    }

    @Test
    void shouldCalculateDvCorrectly() {
        String dv = Cnpj.calcularDv("47960950", "0001");
        assertThat(dv).isEqualTo("21");
    }

    @Test
    void shouldCalculateDvForFilial() {
        String dv = Cnpj.calcularDv("47960950", "0002");
        assertThat(dv).isEqualTo("02");
    }

    @Test
    void shouldValidateCorrectDv() {
        Cnpj cnpj = Cnpj.parse("47960950000121");
        assertThat(cnpj.isDvValido()).isTrue();
    }

    @Test
    void shouldRejectInvalidDv() {
        Cnpj cnpj = Cnpj.parse("47960950000199");
        assertThat(cnpj.isDvValido()).isFalse();
    }

    @Test
    void shouldValidateFullCnpj() {
        assertThat(Cnpj.validar("47960950000121")).isTrue();
        assertThat(Cnpj.validar("47960950000202")).isTrue();
        assertThat(Cnpj.validar("47960950000199")).isFalse();
        assertThat(Cnpj.validar("123")).isFalse();
    }

    @Test
    void shouldFormatFromComponents() {
        assertThat(Cnpj.format("47960950", "0001", "21")).isEqualTo("47960950000121");
    }

    @Test
    void shouldRejectInvalidBaseForDvCalculation() {
        assertThatThrownBy(() -> Cnpj.calcularDv("123", "0001"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("12 digitos");
    }

    @Test
    void shouldImplementEqualsAndHashCode() {
        Cnpj cnpj1 = Cnpj.parse("47960950000121");
        Cnpj cnpj2 = Cnpj.parse("47960950000121");
        Cnpj cnpj3 = Cnpj.parse("47960950000202");
        assertThat(cnpj1).isEqualTo(cnpj2);
        assertThat(cnpj1).isNotEqualTo(cnpj3);
        assertThat(cnpj1.hashCode()).isEqualTo(cnpj2.hashCode());
    }
}
