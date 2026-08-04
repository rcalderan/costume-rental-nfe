package br.com.costumerental.nfe.infrastructure.sefaz;

import br.com.costumerental.nfe.config.NfeProperties;
import br.com.costumerental.nfe.domain.Environment;
import br.com.swconsultoria.certificado.Certificado;
import br.com.swconsultoria.certificado.exception.CertificadoException;
import br.com.swconsultoria.nfe.dom.ConfiguracoesNfe;
import br.com.swconsultoria.nfe.dom.enuns.AmbienteEnum;
import br.com.swconsultoria.nfe.dom.enuns.EstadosEnum;
import org.springframework.stereotype.Component;

import java.util.Objects;

@Component
public class NfeConfigFactory {

    private final NfeProperties properties;

    public NfeConfigFactory(NfeProperties properties) {
        this.properties = properties;
    }

    public ConfiguracoesNfe create(Certificado certificado) throws CertificadoException {
        Objects.requireNonNull(certificado, "Certificado nao pode ser nulo");

        EstadosEnum estado = EstadosEnum.valueOf(properties.getEmit().getUf());
        AmbienteEnum ambiente = resolveAmbiente();
        String schemasPath = resolveSchemasPath();

        ConfiguracoesNfe config = ConfiguracoesNfe.criarConfiguracoes(estado, ambiente, certificado, schemasPath);
        config.setValidacaoDocumento(properties.isValidacaoDocumento());
        return config;
    }

    private AmbienteEnum resolveAmbiente() {
        return Environment.fromCode(properties.getAmbiente()) == Environment.PRODUCTION
                ? AmbienteEnum.PRODUCAO
                : AmbienteEnum.HOMOLOGACAO;
    }

    private String resolveSchemasPath() {
        java.net.URL url = getClass().getResource("/schemas/nfe");
        if (url == null) {
            throw new IllegalStateException("Pasta de schemas nao encontrada em /schemas/nfe");
        }
        try {
            return new java.io.File(url.toURI()).getAbsolutePath();
        } catch (Exception e) {
            throw new IllegalStateException("Falha ao resolver caminho dos schemas: " + url, e);
        }
    }
}
