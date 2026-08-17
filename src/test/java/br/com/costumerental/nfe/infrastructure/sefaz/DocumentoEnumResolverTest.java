package br.com.costumerental.nfe.infrastructure.sefaz;

import br.com.swconsultoria.nfe.dom.enuns.DocumentoEnum;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class DocumentoEnumResolverTest {

    private static final String NFE_ACCESS_KEY = "35260800000000000000550010000000011000000010";
    private static final String NFCE_ACCESS_KEY = "35260800000000000000650010000000011000000010";

    @Test
    void shouldResolveNfeFromModelo() {
        assertThat(DocumentoEnumResolver.fromModelo("55")).isEqualTo(DocumentoEnum.NFE);
    }

    @Test
    void shouldResolveNfceFromModelo() {
        assertThat(DocumentoEnumResolver.fromModelo("65")).isEqualTo(DocumentoEnum.NFCE);
    }

    @Test
    void shouldResolveNfeFromAccessKey() {
        assertThat(DocumentoEnumResolver.fromAccessKey(NFE_ACCESS_KEY)).isEqualTo(DocumentoEnum.NFE);
    }

    @Test
    void shouldResolveNfceFromAccessKey() {
        assertThat(DocumentoEnumResolver.fromAccessKey(NFCE_ACCESS_KEY)).isEqualTo(DocumentoEnum.NFCE);
    }

    @Test
    void shouldRejectAccessKeyTooShort() {
        assertThatThrownBy(() -> DocumentoEnumResolver.fromAccessKey("123"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Chave de acesso invalida");
    }

    @Test
    void shouldRejectNullAccessKey() {
        assertThatThrownBy(() -> DocumentoEnumResolver.fromAccessKey(null))
                .isInstanceOf(IllegalArgumentException.class);
    }
}
