package br.com.costumerental.nfe.infrastructure.certificado;

import br.com.costumerental.nfe.config.NfeProperties;
import org.junit.jupiter.api.Test;
import org.springframework.boot.actuate.health.Health;
import org.springframework.boot.actuate.health.Status;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

class CertificateHealthIndicatorTest {

    @Test
    void healthDown_whenCertPathIsBlank() {
        NfeProperties props = buildProps("", "somePassword");
        CertificateHealthIndicator indicator = new CertificateHealthIndicator(props);

        Health health = indicator.health();

        assertEquals(Status.DOWN, health.getStatus());
        assertEquals("NFSE_CERT_PATH not configured", health.getDetails().get("reason"));
    }

    @Test
    void healthDown_whenCertPasswordIsBlank() {
        NfeProperties props = buildProps("/some/path.pfx", "");
        CertificateHealthIndicator indicator = new CertificateHealthIndicator(props);

        Health health = indicator.health();

        assertEquals(Status.DOWN, health.getStatus());
        assertEquals("NFSE_CERT_PASSWORD not configured", health.getDetails().get("reason"));
    }

    @Test
    void healthDown_whenCertFileNotFound() {
        NfeProperties props = buildProps("/nonexistent/cert.pfx", "password");
        CertificateHealthIndicator indicator = new CertificateHealthIndicator(props);

        Health health = indicator.health();

        assertEquals(Status.DOWN, health.getStatus());
        assertNotNull(health.getDetails().get("reason"));
    }

    private NfeProperties buildProps(String path, String password) {
        NfeProperties.CertificateProperties cert = new NfeProperties.CertificateProperties();
        cert.setPath(path);
        cert.setPassword(password);
        cert.setTipo("A1");

        NfeProperties props = new NfeProperties();
        props.setCertificate(cert);
        return props;
    }
}
