package br.com.costumerental.nfe.application;

import br.com.costumerental.nfe.config.NfeProperties;
import br.com.costumerental.nfe.domain.NfeIssuer;
import br.com.costumerental.nfe.infrastructure.certificado.CertificateEncryption;
import br.com.costumerental.nfe.infrastructure.certificado.CertificateLoader;
import br.com.costumerental.nfe.repository.NfeIssuerRepository;
import br.com.swconsultoria.certificado.exception.CertificadoException;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.time.LocalDateTime;

@Service
public class IssuerConfigService {

    private final NfeIssuerRepository issuerRepository;
    private final CertificateEncryption certificateEncryption;
    private final CertificateLoader certificateLoader;
    private final NfeProperties properties;

    public IssuerConfigService(NfeIssuerRepository issuerRepository,
                               CertificateEncryption certificateEncryption,
                               CertificateLoader certificateLoader,
                               NfeProperties properties) {
        this.issuerRepository = issuerRepository;
        this.certificateEncryption = certificateEncryption;
        this.certificateLoader = certificateLoader;
        this.properties = properties;
    }

    @PostConstruct
    public void loadDefaultIssuer() {
        String defaultCnpj = properties.getEmit().getCnpj();
        if (defaultCnpj == null || defaultCnpj.isBlank()) {
            throw new IllegalStateException("NFE_EMIT_CNPJ não configurado. Não é possível determinar o emitente padrão.");
        }
        NfeIssuer issuer = issuerRepository.findById(defaultCnpj)
                .orElseGet(() -> seedFromEnvironment(defaultCnpj));
        updateProperties(issuer);
    }

    public void activate(String cnpj, String certificatePath, String certificatePassword) throws CertificadoException {
        certificateLoader.load(certificatePath, certificatePassword);
        NfeIssuer issuer = issuerRepository.findById(cnpj)
                .orElseGet(() -> seedFromEnvironment(cnpj));
        updateCertificate(issuer, certificatePath, certificatePassword);
        updateProperties(issuer);
        properties.getCertificate().setPassword(certificatePassword);
    }

    private void updateCertificate(NfeIssuer issuer, String certificatePath, String certificatePassword) {
        issuer.setCertificatePath(certificatePath);
        issuer.setEncryptedPassword(certificateEncryption.encrypt(certificatePassword));
        issuer.setCertificateTipo(properties.getCertificate().getTipo());
        issuer.setUpdatedAt(LocalDateTime.now());
        issuerRepository.save(issuer);
    }

    private NfeIssuer seedFromEnvironment(String cnpj) {
        NfeIssuer issuer = new NfeIssuer();
        issuer.setCnpj(cnpj);
        issuer.setRazaoSocial(properties.getEmit().getRazaoSocial());
        issuer.setNomeFantasia(properties.getEmit().getNomeFantasia());
        issuer.setIe(properties.getEmit().getIe());
        issuer.setIm(properties.getEmit().getIm());
        issuer.setCrt(properties.getEmit().getCrt());
        issuer.setFone(properties.getEmit().getFone());
        issuer.setLogradouro(properties.getEmit().getEndereco().getLogradouro());
        issuer.setNumero(properties.getEmit().getEndereco().getNumero());
        issuer.setBairro(properties.getEmit().getEndereco().getBairro());
        issuer.setMunicipioCodigo(properties.getEmit().getEndereco().getMunicipioCodigo());
        issuer.setMunicipioNome(properties.getEmit().getEndereco().getMunicipioNome());
        issuer.setUf(properties.getEmit().getEndereco().getUf());
        issuer.setCep(properties.getEmit().getEndereco().getCep());
        issuer.setPaisCodigo(properties.getEmit().getEndereco().getPaisCodigo());
        issuer.setPaisNome(properties.getEmit().getEndereco().getPaisNome());
        issuer.setCertificatePath(properties.getCertificate().getPath());
        issuer.setCertificateTipo(properties.getCertificate().getTipo());

        String certPassword = properties.getCertificate().getPassword();
        if (certPassword != null && !certPassword.isBlank()) {
            issuer.setEncryptedPassword(certificateEncryption.encrypt(certPassword));
        }

        issuer.setActive(true);
        return issuerRepository.save(issuer);
    }

    private void updateProperties(NfeIssuer issuer) {
        properties.getEmit().setCnpj(issuer.getCnpj());
        properties.getEmit().setRazaoSocial(issuer.getRazaoSocial());
        properties.getEmit().setNomeFantasia(issuer.getNomeFantasia());
        properties.getEmit().setIe(issuer.getIe());
        properties.getEmit().setIm(issuer.getIm());
        properties.getEmit().setCrt(issuer.getCrt());
        properties.getEmit().setFone(issuer.getFone());
        properties.getEmit().getEndereco().setLogradouro(issuer.getLogradouro());
        properties.getEmit().getEndereco().setNumero(issuer.getNumero());
        properties.getEmit().getEndereco().setBairro(issuer.getBairro());
        properties.getEmit().getEndereco().setMunicipioCodigo(issuer.getMunicipioCodigo());
        properties.getEmit().getEndereco().setMunicipioNome(issuer.getMunicipioNome());
        properties.getEmit().getEndereco().setUf(issuer.getUf());
        properties.getEmit().getEndereco().setCep(issuer.getCep());
        properties.getEmit().getEndereco().setPaisCodigo(issuer.getPaisCodigo());
        properties.getEmit().getEndereco().setPaisNome(issuer.getPaisNome());
        properties.getCertificate().setPath(issuer.getCertificatePath());
        properties.getCertificate().setTipo(issuer.getCertificateTipo());

        if (issuer.getEncryptedPassword() != null && !issuer.getEncryptedPassword().isBlank()) {
            String decryptedPassword = certificateEncryption.decrypt(issuer.getEncryptedPassword());
            properties.getCertificate().setPassword(decryptedPassword);
        }
    }
}
