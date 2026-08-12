package br.com.costumerental.nfe.application;

import br.com.costumerental.nfe.config.NfeProperties;
import br.com.costumerental.nfe.domain.NfeIssuer;
import br.com.costumerental.nfe.infrastructure.certificado.CertificateEncryption;
import br.com.costumerental.nfe.infrastructure.certificado.CertificateLoader;
import br.com.costumerental.nfe.repository.NfeIssuerRepository;
import br.com.swconsultoria.certificado.exception.CertificadoException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class IssuerConfigServiceTest {

    @Mock
    private NfeIssuerRepository issuerRepository;

    @Mock
    private CertificateEncryption certificateEncryption;

    @Mock
    private CertificateLoader certificateLoader;

    private NfeProperties properties;
    private IssuerConfigService service;

    @BeforeEach
    void setUp() {
        properties = buildProperties();
        service = new IssuerConfigService(issuerRepository, certificateEncryption, certificateLoader, properties);
    }

    private NfeProperties buildProperties() {
        NfeProperties props = new NfeProperties();

        NfeProperties.EmitProperties emit = new NfeProperties.EmitProperties();
        emit.setCnpj("08299621000120");
        emit.setIe("637287665118");
        emit.setUf("SP");
        emit.setRazaoSocial("Emitente Teste");
        emit.setCrt("1");
        NfeProperties.EnderecoProperties endereco = new NfeProperties.EnderecoProperties();
        emit.setEndereco(endereco);

        NfeProperties.CertificateProperties certificate = new NfeProperties.CertificateProperties();
        certificate.setPath("/certs/cert.pfx");
        certificate.setPassword("password123");
        certificate.setTipo("A1");

        props.setEmit(emit);
        props.setCertificate(certificate);
        return props;
    }

    @Test
    void shouldLoadActiveIssuerAndUpdateProperties() {
        NfeIssuer issuer = buildIssuer("08299621000120");
        issuer.setEncryptedPassword("encrypted");
        when(issuerRepository.findFirstByActiveTrue()).thenReturn(Optional.of(issuer));
        when(certificateEncryption.decrypt("encrypted")).thenReturn("decrypted-password");

        service.loadDefaultIssuer();

        assertThat(properties.getEmit().getRazaoSocial()).isEqualTo("Emitente Teste");
        assertThat(properties.getEmit().getCnpj()).isEqualTo("08299621000120");
        assertThat(properties.getCertificate().getPassword()).isEqualTo("decrypted-password");
    }

    @Test
    void shouldSeedIssuerFromPropertiesWhenNoActiveIssuerExists() {
        when(issuerRepository.findFirstByActiveTrue()).thenReturn(Optional.empty());
        when(certificateEncryption.encrypt("password123")).thenReturn("encrypted");
        when(certificateEncryption.decrypt("encrypted")).thenReturn("password123");
        when(issuerRepository.save(any(NfeIssuer.class))).thenAnswer(invocation -> invocation.getArgument(0));

        service.loadDefaultIssuer();

        verify(issuerRepository).save(any(NfeIssuer.class));
        assertThat(properties.getEmit().getCnpj()).isEqualTo("08299621000120");
    }

    @Test
    void shouldLeavePropertiesEmptyWhenNoActiveIssuerAndNoCnpjConfigured() {
        properties.getEmit().setCnpj("");
        when(issuerRepository.findFirstByActiveTrue()).thenReturn(Optional.empty());

        service.loadDefaultIssuer();

        assertThat(service.isConfigured()).isFalse();
    }

    @Test
    void shouldConfigureIssuerFromRequest() {
        when(issuerRepository.findById("08299621000120")).thenReturn(Optional.empty());
        when(issuerRepository.findAll()).thenReturn(java.util.List.of());
        when(issuerRepository.save(any(NfeIssuer.class))).thenAnswer(invocation -> invocation.getArgument(0));

        IssuerSetupRequest request = new IssuerSetupRequest(
                "08299621000120",
                "NOIVA MODAS E ACESSORIOS LTDA",
                null,
                null,
                null,
                "1",
                null,
                "Rua Teste",
                "0",
                "Centro",
                "3548906",
                "Sao Carlos",
                "SP",
                "13560000",
                "1058",
                "BRASIL"
        );

        NfeIssuer issuer = service.configureIssuer(request);

        assertThat(issuer.getCnpj()).isEqualTo("08299621000120");
        assertThat(issuer.isActive()).isTrue();
        assertThat(service.isConfigured()).isTrue();
        assertThat(properties.getEmit().getRazaoSocial()).isEqualTo("NOIVA MODAS E ACESSORIOS LTDA");
    }

    @Test
    void shouldActivateCertificateAndPersistIssuer() throws CertificadoException {
        NfeIssuer issuer = buildIssuer("08299621000120");
        when(issuerRepository.findFirstByActiveTrue()).thenReturn(Optional.of(issuer));
        when(certificateLoader.load("/certs/new.pfx", "new-pass")).thenReturn(null);
        when(certificateEncryption.encrypt("new-pass")).thenReturn("new-encrypted");
        when(certificateEncryption.decrypt("new-encrypted")).thenReturn("new-pass");
        when(issuerRepository.save(any(NfeIssuer.class))).thenAnswer(invocation -> invocation.getArgument(0));

        service.activate("/certs/new.pfx", "new-pass");

        assertThat(issuer.getCertificatePath()).isEqualTo("/certs/new.pfx");
        assertThat(issuer.getEncryptedPassword()).isEqualTo("new-encrypted");
        assertThat(properties.getCertificate().getPassword()).isEqualTo("new-pass");
        verify(certificateLoader).load("/certs/new.pfx", "new-pass");
    }

    private NfeIssuer buildIssuer(String cnpj) {
        NfeIssuer issuer = new NfeIssuer();
        issuer.setCnpj(cnpj);
        issuer.setRazaoSocial("Emitente Teste");
        issuer.setIe("637287665118");
        issuer.setIm("123456789");
        issuer.setCrt("1");
        issuer.setFone("16333722363");
        issuer.setLogradouro("Rua Teste");
        issuer.setNumero("0");
        issuer.setBairro("Centro");
        issuer.setMunicipioCodigo("3548906");
        issuer.setMunicipioNome("Sao Carlos");
        issuer.setUf("SP");
        issuer.setCep("13560000");
        issuer.setPaisCodigo("1058");
        issuer.setPaisNome("BRASIL");
        issuer.setCertificatePath("/certs/cert.pfx");
        issuer.setCertificateTipo("A1");
        return issuer;
    }
}
