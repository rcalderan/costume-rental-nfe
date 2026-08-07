package br.com.costumerental.nfe.infrastructure.sefaz;

import br.com.costumerental.nfe.config.NfeProperties;
import br.com.costumerental.nfe.domain.Environment;
import br.com.swconsultoria.certificado.Certificado;
import br.com.swconsultoria.certificado.exception.CertificadoException;
import br.com.swconsultoria.nfe.dom.ConfiguracoesNfe;
import br.com.swconsultoria.nfe.dom.enuns.AmbienteEnum;
import br.com.swconsultoria.nfe.dom.enuns.EstadosEnum;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
import org.springframework.core.io.support.ResourcePatternResolver;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.Objects;

@Component
public class NfeConfigFactory {

    private static final String SCHEMAS_PATTERN = "classpath:schemas/nfe";
    private static final String XSD_PATTERN = SCHEMAS_PATTERN + "/*.xsd";

    private final NfeProperties properties;
    private final ResourcePatternResolver resourceResolver;
    private final String schemasDirectory;

    public NfeConfigFactory(NfeProperties properties) {
        this.properties = properties;
        this.resourceResolver = new PathMatchingResourcePatternResolver();
        this.schemasDirectory = extractSchemasToTemp();
    }

    public ConfiguracoesNfe create(Certificado certificado) throws CertificadoException {
        Objects.requireNonNull(certificado, "Certificado nao pode ser nulo");

        EstadosEnum estado = EstadosEnum.valueOf(properties.getEmit().getUf());
        AmbienteEnum ambiente = resolveAmbiente();

        ConfiguracoesNfe config = ConfiguracoesNfe.criarConfiguracoes(estado, ambiente, certificado, schemasDirectory);
        config.setValidacaoDocumento(properties.isValidacaoDocumento());
        return config;
    }

    private AmbienteEnum resolveAmbiente() {
        return Environment.fromCode(properties.getAmbiente()) == Environment.PRODUCTION
                ? AmbienteEnum.PRODUCAO
                : AmbienteEnum.HOMOLOGACAO;
    }

    private String extractSchemasToTemp() {
        Resource[] resources = resolveSchemaResources();
        Path tempDir = createTempDirectory();
        copyResourcesTo(resources, tempDir);
        return tempDir.toFile().getAbsolutePath();
    }

    private Resource[] resolveSchemaResources() {
        try {
            return resourceResolver.getResources(XSD_PATTERN);
        } catch (IOException e) {
            throw new IllegalStateException("Falha ao localizar schemas em " + XSD_PATTERN, e);
        }
    }

    private Path createTempDirectory() {
        try {
            return Files.createTempDirectory("nfe-schemas-");
        } catch (IOException e) {
            throw new IllegalStateException("Falha ao criar diretorio temporario para schemas", e);
        }
    }

    private void copyResourcesTo(Resource[] resources, Path tempDir) {
        if (resources.length == 0) {
            throw new IllegalStateException("Nenhum schema .xsd encontrado em " + SCHEMAS_PATTERN);
        }
        for (Resource resource : resources) {
            copyResource(resource, tempDir);
        }
    }

    private void copyResource(Resource resource, Path tempDir) {
        String filename = resource.getFilename();
        if (filename == null) {
            return;
        }
        try (InputStream input = resource.getInputStream()) {
            Files.copy(input, tempDir.resolve(filename), StandardCopyOption.REPLACE_EXISTING);
        } catch (IOException e) {
            throw new IllegalStateException("Falha ao extrair schema " + filename, e);
        }
    }
}
