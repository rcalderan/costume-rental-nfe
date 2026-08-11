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
import java.util.Optional;

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
        Optional<NfeIssuer> issuer = issuerRepository.findFirstByActiveTrue();
        issuer.ifPresent(this::updateProperties);
        if (issuer.isEmpty()) {
            String cnpj = properties.getEmit().getCnpj();
            if (cnpj != null && !cnpj.isBlank()) {
                updateProperties(seedFromProperties(cnpj));
            }
        }
    }

    public boolean isConfigured() {
        return properties.getEmit().getCnpj() != null && !properties.getEmit().getCnpj().isBlank();
    }

    public Optional<NfeIssuer> findCurrentIssuer() {
        return issuerRepository.findFirstByActiveTrue();
    }

    public NfeIssuer configureIssuer(IssuerSetupRequest request) {
        NfeIssuer issuer = issuerRepository.findById(request.cnpj())
                .orElse(new NfeIssuer());
        issuer.setCnpj(request.cnpj());
        issuer.setRazaoSocial(request.razaoSocial());
        issuer.setNomeFantasia(request.nomeFantasia());
        issuer.setIe(request.ie());
        issuer.setIm(request.im());
        issuer.setCrt(request.crt());
        issuer.setFone(request.fone());
        issuer.setLogradouro(request.logradouro());
        issuer.setNumero(request.numero());
        issuer.setBairro(request.bairro());
        issuer.setMunicipioCodigo(request.municipioCodigo());
        issuer.setMunicipioNome(request.municipioNome());
        issuer.setUf(request.uf());
        issuer.setCep(request.cep());
        issuer.setPaisCodigo(request.paisCodigo());
        issuer.setPaisNome(request.paisNome());
        issuer.setActive(true);

        issuerRepository.findAll().stream()
                .filter(i -> !request.cnpj().equals(i.getCnpj()) && i.isActive())
                .forEach(i -> {
                    i.setActive(false);
                    issuerRepository.save(i);
                });

        NfeIssuer saved = issuerRepository.save(issuer);
        updateProperties(saved);
        return saved;
    }

    private NfeIssuer seedFromProperties(String cnpj) {
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

    public void activate(String certificatePath, String certificatePassword) throws CertificadoException {
        certificateLoader.load(certificatePath, certificatePassword);
        NfeIssuer issuer = issuerRepository.findFirstByActiveTrue()
                .orElseThrow(() -> new IllegalStateException("Nenhum emitente ativo encontrado na tabela nfe_issuer."));
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

    private void updateProperties(NfeIssuer issuer) {
        properties.getEmit().setCnpj(issuer.getCnpj());
        properties.getEmit().setRazaoSocial(issuer.getRazaoSocial());
        properties.getEmit().setNomeFantasia(issuer.getNomeFantasia());
        properties.getEmit().setIe(issuer.getIe());
        properties.getEmit().setIm(issuer.getIm());
        properties.getEmit().setCrt(issuer.getCrt());
        properties.getEmit().setFone(issuer.getFone());
        NfeProperties.EnderecoProperties endereco = properties.getEmit().getEndereco();
        if (endereco == null) {
            endereco = new NfeProperties.EnderecoProperties();
            properties.getEmit().setEndereco(endereco);
        }
        endereco.setLogradouro(issuer.getLogradouro());
        endereco.setNumero(issuer.getNumero());
        endereco.setBairro(issuer.getBairro());
        endereco.setMunicipioCodigo(issuer.getMunicipioCodigo());
        endereco.setMunicipioNome(issuer.getMunicipioNome());
        endereco.setUf(issuer.getUf());
        endereco.setCep(issuer.getCep());
        endereco.setPaisCodigo(issuer.getPaisCodigo());
        endereco.setPaisNome(issuer.getPaisNome());
        properties.getCertificate().setPath(issuer.getCertificatePath());
        properties.getCertificate().setTipo(issuer.getCertificateTipo());

        if (issuer.getEncryptedPassword() != null && !issuer.getEncryptedPassword().isBlank()) {
            String decryptedPassword = certificateEncryption.decrypt(issuer.getEncryptedPassword());
            properties.getCertificate().setPassword(decryptedPassword);
        }
    }
}
