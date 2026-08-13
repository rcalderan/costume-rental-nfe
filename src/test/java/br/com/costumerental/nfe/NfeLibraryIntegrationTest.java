package br.com.costumerental.nfe;

import br.com.costumerental.nfe.api.dto.CustomerInfo;
import br.com.costumerental.nfe.api.dto.NfeEmissionRequest;
import br.com.costumerental.nfe.api.dto.NfeItemRequest;
import br.com.costumerental.nfe.application.FiscalDocumentNumberControlService;
import br.com.costumerental.nfe.config.NfeProperties;
import br.com.costumerental.nfe.domain.Empresa;
import br.com.costumerental.nfe.domain.NfeIssuer;
import br.com.costumerental.nfe.infrastructure.certificado.CertificateLoader;
import br.com.costumerental.nfe.infrastructure.sefaz.NfeConfigFactory;
import br.com.costumerental.nfe.infrastructure.sefaz.NfeLibraryAdapter;
import br.com.costumerental.nfe.xml.AccessKeyGenerator;
import br.com.costumerental.nfe.xml.IbgeCityCodeResolver;
import br.com.costumerental.nfe.xml.NfeXmlAssembler;
import br.com.swconsultoria.certificado.Certificado;
import br.com.swconsultoria.nfe.dom.ConfiguracoesNfe;
import br.com.swconsultoria.nfe.schema_4.enviNFe.TEnviNFe;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ActiveProfiles;

import java.math.BigDecimal;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(classes = {NfeProperties.class, CertificateLoader.class, NfeConfigFactory.class,
        NfeXmlAssembler.class, AccessKeyGenerator.class, IbgeCityCodeResolver.class, NfeLibraryAdapter.class,
        FiscalDocumentNumberControlService.class})
@EnableConfigurationProperties
@ActiveProfiles("test")
class NfeLibraryIntegrationTest {

    @MockBean
    private FiscalDocumentNumberControlService numberControlService;

    @Autowired
    private NfeProperties properties;

    @Autowired
    private CertificateLoader certificateLoader;

    @Autowired
    private NfeConfigFactory configFactory;

    @Autowired
    private NfeXmlAssembler xmlAssembler;

    @Autowired
    private NfeLibraryAdapter libraryAdapter;

    @Test
    void shouldSignAndValidateXmlWithJavaNFe() throws Exception {
        Certificado certificado = certificateLoader.load();
        ConfiguracoesNfe config = configFactory.create(certificado);
        TEnviNFe enviNFe = xmlAssembler.build(sampleRequest(), buildIssuer());

        TEnviNFe signed = libraryAdapter.signAndValidate(config, enviNFe);
        String xml = libraryAdapter.toXml(signed);

        assertThat(signed.getNFe().get(0).getSignature()).isNotNull();
        assertThat(xml).contains("Signature");
    }

    private NfeIssuer buildIssuer() {
        Empresa empresa = new Empresa();
        empresa.setRootCnpj("08299621");
        empresa.setRazaoSocial("Emitente Teste");
        empresa.setCrt("1");
        empresa.setPaisCodigo("1058");
        empresa.setPaisNome("BRASIL");
        NfeIssuer iss = new NfeIssuer();
        iss.setId(1L);
        iss.setEmpresa(empresa);
        iss.setBranchOrder("0001");
        iss.setDigitoControle("20");
        iss.setIe("111111111111");
        iss.setLogradouro("Rua Teste");
        iss.setNumero("0");
        iss.setBairro("Centro");
        iss.setMunicipioCodigo("3548906");
        iss.setMunicipioNome("Sao Carlos");
        iss.setUf("SP");
        iss.setCep("13560000");
        return iss;
    }

    private NfeEmissionRequest sampleRequest() {
        return NfeEmissionRequest.builder()
                .natureOperation("Venda de mercadoria")
                .customer(CustomerInfo.builder()
                        .name("Cliente Teste")
                        .document("12345678901")
                        .street("Rua do Cliente")
                        .number("100")
                        .neighborhood("Centro")
                        .cityCode("3548906")
                        .cityName("Sao Carlos")
                        .state("SP")
                        .zipCode("13560000")
                        .build())
                .items(List.of(NfeItemRequest.builder()
                        .productCode("PROD-001")
                        .description("Produto de teste")
                        .ncm("99999999")
                        .cfop("5102")
                        .unit("UN")
                        .quantity(BigDecimal.ONE)
                        .unitValue(new BigDecimal("10.00"))
                        .build()))
                .build();
    }
}
