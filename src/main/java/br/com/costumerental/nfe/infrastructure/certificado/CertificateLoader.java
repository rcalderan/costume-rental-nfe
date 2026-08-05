package br.com.costumerental.nfe.infrastructure.certificado;

import br.com.costumerental.nfe.config.NfeProperties;
import br.com.swconsultoria.certificado.Certificado;
import br.com.swconsultoria.certificado.CertificadoService;
import br.com.swconsultoria.certificado.exception.CertificadoException;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

@Component
public class CertificateLoader {

    private final NfeProperties properties;

    public CertificateLoader(NfeProperties properties) {
        this.properties = properties;
    }

    public Certificado load() throws CertificadoException {
        String path = properties.getCertificate().getPath();
        String password = properties.getCertificate().getPassword();

        if (path == null || path.isBlank()) {
            throw new CertificadoException("Caminho do certificado digital nao configurado.");
        }
        if (password == null || password.isBlank()) {
            throw new CertificadoException("Senha do certificado digital nao configurada.");
        }

        Path certPath = Paths.get(path);
        if (!Files.exists(certPath)) {
            throw new CertificadoException("Certificado digital nao encontrado.");
        }

        try {
            return CertificadoService.certificadoPfx(path, password);
        } catch (java.io.FileNotFoundException e) {
            throw new CertificadoException("Certificado digital nao encontrado.", e);
        }
    }
}
