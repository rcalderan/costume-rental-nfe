package br.com.costumerental.nfe.infrastructure.certificado;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class CertificateEncryptionTest {

    private static final String VALID_KEY = "MDEyMzQ1Njc4OWFiY2RlZjAxMjM0NTY3ODlhYmNkZWY=";

    @Test
    void shouldEncryptAndDecrypt() {
        CertificateEncryption encryption = new CertificateEncryption(VALID_KEY);
        String password = "test-password-123";

        String encrypted = encryption.encrypt(password);
        String decrypted = encryption.decrypt(encrypted);

        assertThat(encrypted).isNotEqualTo(password);
        assertThat(decrypted).isEqualTo(password);
    }

    @Test
    void shouldProduceDifferentCiphertextForSamePlaintext() {
        CertificateEncryption encryption = new CertificateEncryption(VALID_KEY);
        String password = "same-password";

        String first = encryption.encrypt(password);
        String second = encryption.encrypt(password);

        assertThat(first).isNotEqualTo(second);
        assertThat(encryption.decrypt(first)).isEqualTo(password);
        assertThat(encryption.decrypt(second)).isEqualTo(password);
    }

    @Test
    void shouldRejectMissingKey() {
        assertThatThrownBy(() -> new CertificateEncryption(""))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("CERT_ENCRYPTION_KEY");
    }

    @Test
    void shouldRejectInvalidBase64Key() {
        assertThatThrownBy(() -> new CertificateEncryption("not-base64!!!"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("base64");
    }

    @Test
    void shouldRejectKeyWithWrongSize() {
        assertThatThrownBy(() -> new CertificateEncryption("c2hvcnQ="))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("32");
    }
}
