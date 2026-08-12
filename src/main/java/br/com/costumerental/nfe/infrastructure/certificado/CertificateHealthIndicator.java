package br.com.costumerental.nfe.infrastructure.certificado;

import br.com.costumerental.nfe.config.NfeProperties;
import org.springframework.boot.actuate.health.Health;
import org.springframework.boot.actuate.health.HealthIndicator;
import org.springframework.stereotype.Component;

import java.io.FileInputStream;
import java.security.KeyStore;
import java.security.cert.X509Certificate;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Date;
import java.util.Enumeration;

/**
 * Reports the health of the A1 digital certificate used for SEFAZ communication.
 * Checks: file exists, password valid, not expired, and warns when expiring within 30 days.
 *
 * Usage: GET /actuator/health -> "certificate": { "status": "UP", "details": { ... } }
 */
@Component
public class CertificateHealthIndicator implements HealthIndicator {

    private static final long WARNING_DAYS = 30;

    private final NfeProperties properties;

    public CertificateHealthIndicator(NfeProperties properties) {
        this.properties = properties;
    }

    @Override
    public Health health() {
        String path = properties.getCertificate().getPath();
        String password = properties.getCertificate().getPassword();

        if (path == null || path.isBlank()) {
            return Health.up().withDetail("status", "NOT_CONFIGURED")
                    .withDetail("reason", "NFSE_CERT_PATH not configured").build();
        }
        if (password == null || password.isBlank()) {
            return Health.up().withDetail("status", "NOT_CONFIGURED")
                    .withDetail("reason", "NFSE_CERT_PASSWORD not configured").build();
        }

        try (FileInputStream fis = new FileInputStream(path)) {
            KeyStore ks = KeyStore.getInstance("PKCS12");
            ks.load(fis, password.toCharArray());

            Enumeration<String> aliases = ks.aliases();
            if (!aliases.hasMoreElements()) {
                return Health.down()
                        .withDetail("reason", "No certificate entries found in keystore")
                        .build();
            }

            String alias = aliases.nextElement();
            X509Certificate cert = (X509Certificate) ks.getCertificate(alias);
            Date notAfter = cert.getNotAfter();
            Date notBefore = cert.getNotBefore();
            String subject = cert.getSubjectX500Principal().getName();
            long daysUntilExpiry = ChronoUnit.DAYS.between(Instant.now(), notAfter.toInstant());

            Health.Builder builder = daysUntilExpiry <= 0
                    ? Health.down().withDetail("reason", "Certificate expired on " + notAfter)
                    : daysUntilExpiry <= WARNING_DAYS
                        ? Health.up().withDetail("warning", "Certificate expires in " + daysUntilExpiry + " days")
                        : Health.up();

            return builder
                    .withDetail("subject", subject)
                    .withDetail("validFrom", notBefore.toString())
                    .withDetail("validUntil", notAfter.toString())
                    .withDetail("daysUntilExpiry", daysUntilExpiry)
                    .build();
        } catch (java.io.FileNotFoundException e) {
            return Health.down()
                    .withDetail("reason", "Certificate file not found. Verifique a configuracao NFSE_CERT_PATH.")
                    .build();
        } catch (java.io.IOException e) {
            return Health.down()
                    .withDetail("reason", "Failed to read certificate: " + e.getMessage())
                    .build();
        } catch (Exception e) {
            return Health.down()
                    .withDetail("reason", "Certificate validation failed: " + e.getMessage())
                    .build();
        }
    }
}
