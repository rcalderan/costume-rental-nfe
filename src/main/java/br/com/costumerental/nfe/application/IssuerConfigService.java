package br.com.costumerental.nfe.application;

import br.com.costumerental.nfe.config.NfeProperties;
import br.com.costumerental.nfe.domain.Cnpj;
import br.com.costumerental.nfe.domain.Empresa;
import br.com.costumerental.nfe.domain.NfeIssuer;
import br.com.costumerental.nfe.infrastructure.certificado.CertificateEncryption;
import br.com.costumerental.nfe.infrastructure.certificado.CertificateLoader;
import br.com.costumerental.nfe.repository.EmpresaRepository;
import br.com.costumerental.nfe.repository.NfeIssuerRepository;
import br.com.swconsultoria.certificado.exception.CertificadoException;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
public class IssuerConfigService {

    private static final String MATRIZ_BRANCH = "0001";

    private final NfeIssuerRepository issuerRepository;
    private final EmpresaRepository empresaRepository;
    private final CertificateEncryption certificateEncryption;
    private final CertificateLoader certificateLoader;
    private final NfeProperties properties;

    public IssuerConfigService(NfeIssuerRepository issuerRepository,
                               EmpresaRepository empresaRepository,
                               CertificateEncryption certificateEncryption,
                               CertificateLoader certificateLoader,
                               NfeProperties properties) {
        this.issuerRepository = issuerRepository;
        this.empresaRepository = empresaRepository;
        this.certificateEncryption = certificateEncryption;
        this.certificateLoader = certificateLoader;
        this.properties = properties;
    }

    @PostConstruct
    public void loadDefaultIssuer() {
        Optional<NfeIssuer> issuer = issuerRepository.findFirstByActiveTrueOrderByBranchOrderAsc();
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
        return issuerRepository.findFirstByActiveTrueOrderByBranchOrderAsc();
    }

    public Optional<NfeIssuer> findByCnpj(String cnpj14) {
        Cnpj cnpj = Cnpj.parse(cnpj14);
        return issuerRepository.findByEmpresaRootCnpjAndBranchOrder(cnpj.root(), cnpj.branch());
    }

    public List<NfeIssuer> findBranchesOf(String rootCnpj) {
        return issuerRepository.findByEmpresaRootCnpj(rootCnpj);
    }

    public NfeIssuer configureIssuer(IssuerSetupRequest request) {
        Cnpj cnpj = Cnpj.parse(request.cnpj());
        if (!cnpj.isDvValido()) {
            throw new IllegalArgumentException(
                    "Digitos de controle do CNPJ invalidos. Esperado: "
                            + Cnpj.calcularDv(cnpj.root(), cnpj.branch()) + ", recebido: " + cnpj.dv());
        }
        Empresa empresa = empresaRepository.findById(cnpj.root())
                .orElseGet(() -> new Empresa());
        empresa.setRootCnpj(cnpj.root());
        empresa.setRazaoSocial(request.razaoSocial());
        empresa.setCrt(request.crt());
        empresa.setPaisCodigo(request.paisCodigo());
        empresa.setPaisNome(request.paisNome());
        empresa.setMatrizCnpj(cnpj.format());
        empresa = empresaRepository.save(empresa);

        NfeIssuer issuer = issuerRepository.findByEmpresaRootCnpjAndBranchOrder(cnpj.root(), cnpj.branch())
                .orElse(new NfeIssuer());
        issuer.setEmpresa(empresa);
        issuer.setBranchOrder(cnpj.branch());
        issuer.setDigitoControle(cnpj.dv());
        issuer.setNomeFantasia(request.nomeFantasia());
        issuer.setIe(request.ie());
        issuer.setIm(request.im());
        issuer.setFone(request.fone());
        issuer.setLogradouro(request.logradouro());
        issuer.setNumero(request.numero());
        issuer.setBairro(request.bairro());
        issuer.setMunicipioCodigo(request.municipioCodigo());
        issuer.setMunicipioNome(request.municipioNome());
        issuer.setUf(request.uf());
        issuer.setCep(request.cep());
        issuer.setActive(true);

        NfeIssuer saved = issuerRepository.save(issuer);
        updateProperties(saved);
        return saved;
    }

    public NfeIssuer configureBranch(IssuerBranchSetupRequest request) {
        Cnpj cnpj = Cnpj.parse(request.cnpj());
        if (cnpj.isMatriz()) {
            throw new IllegalArgumentException(
                    "CNPJ informado e da matriz (sufixo 0001). Use o endpoint /setup para cadastrar a matriz.");
        }
        if (!cnpj.isDvValido()) {
            throw new IllegalArgumentException(
                    "Digitos de controle do CNPJ invalidos. Esperado: "
                            + Cnpj.calcularDv(cnpj.root(), cnpj.branch()) + ", recebido: " + cnpj.dv());
        }
        Empresa empresa = empresaRepository.findById(cnpj.root())
                .orElseThrow(() -> new IllegalArgumentException(
                        "Matriz nao cadastrada para a raiz " + cnpj.root()
                                + ". Cadastre a matriz (sufixo 0001) primeiro."));

        NfeIssuer issuer = issuerRepository.findByEmpresaRootCnpjAndBranchOrder(cnpj.root(), cnpj.branch())
                .orElse(new NfeIssuer());
        issuer.setEmpresa(empresa);
        issuer.setBranchOrder(cnpj.branch());
        issuer.setDigitoControle(cnpj.dv());
        issuer.setNomeFantasia(request.nomeFantasia());
        issuer.setIe(request.ie());
        issuer.setIm(request.im());
        issuer.setFone(request.fone());
        issuer.setLogradouro(request.logradouro());
        issuer.setNumero(request.numero());
        issuer.setBairro(request.bairro());
        issuer.setMunicipioCodigo(request.municipioCodigo());
        issuer.setMunicipioNome(request.municipioNome());
        issuer.setUf(request.uf());
        issuer.setCep(request.cep());
        issuer.setActive(true);

        if (request.certificatePath() != null && !request.certificatePath().isBlank()) {
            issuer.setCertificatePath(request.certificatePath());
            if (request.certificatePassword() != null && !request.certificatePassword().isBlank()) {
                issuer.setEncryptedPassword(certificateEncryption.encrypt(request.certificatePassword()));
            }
            issuer.setCertificateTipo(properties.getCertificate().getTipo());
        }

        return issuerRepository.save(issuer);
    }

    private NfeIssuer seedFromProperties(String cnpj14) {
        Cnpj cnpj = Cnpj.parse(cnpj14);
        Empresa empresa = empresaRepository.findById(cnpj.root())
                .orElseGet(Empresa::new);
        empresa.setRootCnpj(cnpj.root());
        empresa.setRazaoSocial(properties.getEmit().getRazaoSocial());
        empresa.setCrt(properties.getEmit().getCrt());
        empresa.setPaisCodigo(properties.getEmit().getEndereco().getPaisCodigo());
        empresa.setPaisNome(properties.getEmit().getEndereco().getPaisNome());
        empresa.setMatrizCnpj(cnpj.format());
        empresa = empresaRepository.save(empresa);

        NfeIssuer issuer = new NfeIssuer();
        issuer.setEmpresa(empresa);
        issuer.setBranchOrder(cnpj.branch());
        issuer.setDigitoControle(cnpj.dv());
        issuer.setNomeFantasia(properties.getEmit().getNomeFantasia());
        issuer.setIe(properties.getEmit().getIe());
        issuer.setIm(properties.getEmit().getIm());
        issuer.setFone(properties.getEmit().getFone());
        issuer.setLogradouro(properties.getEmit().getEndereco().getLogradouro());
        issuer.setNumero(properties.getEmit().getEndereco().getNumero());
        issuer.setBairro(properties.getEmit().getEndereco().getBairro());
        issuer.setMunicipioCodigo(properties.getEmit().getEndereco().getMunicipioCodigo());
        issuer.setMunicipioNome(properties.getEmit().getEndereco().getMunicipioNome());
        issuer.setUf(properties.getEmit().getEndereco().getUf());
        issuer.setCep(properties.getEmit().getEndereco().getCep());
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
        NfeIssuer issuer = issuerRepository.findFirstByActiveTrueOrderByBranchOrderAsc()
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
