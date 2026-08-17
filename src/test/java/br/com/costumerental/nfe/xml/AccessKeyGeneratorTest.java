package br.com.costumerental.nfe.xml;

import br.com.costumerental.nfe.config.NfeProperties;
import br.com.costumerental.nfe.domain.Firm;
import br.com.costumerental.nfe.domain.NfeIssuer;
import org.junit.jupiter.api.Test;

import java.time.OffsetDateTime;
import java.time.ZoneOffset;

import static org.assertj.core.api.Assertions.assertThat;

class AccessKeyGeneratorTest {

    private static final OffsetDateTime ISSUE_DATE = OffsetDateTime.of(2026, 8, 17, 10, 0, 0, 0, ZoneOffset.UTC);

    @Test
    void shouldUseModelo55ForNfe() {
        AccessKeyGenerator generator = generatorWithModelo("55");
        NfeIssuer issuer = buildIssuer();

        String accessKey = generator.generate(ISSUE_DATE, issuer, "1", "100", "12345678");

        assertThat(accessKey).hasSize(44);
        assertThat(accessKey.substring(20, 22)).isEqualTo("55");
    }

    @Test
    void shouldUseModelo65ForNfce() {
        AccessKeyGenerator generator = generatorWithModelo("65");
        NfeIssuer issuer = buildIssuer();

        String accessKey = generator.generate(ISSUE_DATE, issuer, "1", "100", "12345678");

        assertThat(accessKey).hasSize(44);
        assertThat(accessKey.substring(20, 22)).isEqualTo("65");
    }

    private AccessKeyGenerator generatorWithModelo(String modelo) {
        NfeProperties properties = new NfeProperties();
        properties.setModelo(modelo);
        return new AccessKeyGenerator(properties);
    }

    private NfeIssuer buildIssuer() {
        Firm firm = new Firm();
        firm.setRootCnpj("00000000");
        firm.setBranchOrder("0001");
        firm.setDigit("91");
        NfeIssuer issuer = new NfeIssuer();
        issuer.setFirm(firm);
        issuer.setUf("SP");
        return issuer;
    }
}
