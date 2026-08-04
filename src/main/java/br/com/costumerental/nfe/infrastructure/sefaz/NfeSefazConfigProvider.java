package br.com.costumerental.nfe.infrastructure.sefaz;

import br.com.costumerental.nfe.infrastructure.certificado.CertificateLoader;
import br.com.swconsultoria.certificado.Certificado;
import br.com.swconsultoria.certificado.exception.CertificadoException;
import br.com.swconsultoria.nfe.dom.ConfiguracoesNfe;
import org.springframework.stereotype.Component;

@Component
public class NfeSefazConfigProvider {

    private final CertificateLoader certificateLoader;
    private final NfeConfigFactory configFactory;

    public NfeSefazConfigProvider(CertificateLoader certificateLoader, NfeConfigFactory configFactory) {
        this.certificateLoader = certificateLoader;
        this.configFactory = configFactory;
    }

    public ConfiguracoesNfe buildConfig() throws CertificadoException {
        Certificado certificado = certificateLoader.load();
        return configFactory.create(certificado);
    }
}
