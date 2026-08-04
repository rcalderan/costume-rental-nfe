package br.com.costumerental.nfe;

import br.com.costumerental.nfe.api.dto.CustomerInfo;
import br.com.costumerental.nfe.api.dto.NfeEmissionRequest;
import br.com.costumerental.nfe.api.dto.NfeItemRequest;
import br.com.costumerental.nfe.config.NfeProperties;
import br.com.costumerental.nfe.infrastructure.certificado.CertificateLoader;
import br.com.costumerental.nfe.infrastructure.sefaz.NfeConfigFactory;
import br.com.costumerental.nfe.infrastructure.sefaz.NfeLibraryAdapter;
import br.com.costumerental.nfe.xml.AccessKeyGenerator;
import br.com.costumerental.nfe.xml.NfeXmlAssembler;
import br.com.swconsultoria.certificado.Certificado;
import br.com.swconsultoria.nfe.dom.ConfiguracoesNfe;
import br.com.swconsultoria.nfe.schema_4.enviNFe.TEnviNFe;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import java.math.BigDecimal;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(classes = {NfeProperties.class, CertificateLoader.class, NfeConfigFactory.class,
        NfeXmlAssembler.class, AccessKeyGenerator.class, NfeLibraryAdapter.class})
@EnableConfigurationProperties
@ActiveProfiles("test")
class NfeLibraryIntegrationTest {

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
        TEnviNFe enviNFe = xmlAssembler.build(sampleRequest());

        TEnviNFe signed = libraryAdapter.signAndValidate(config, enviNFe);
        String xml = libraryAdapter.toXml(signed);

        assertThat(signed.getNFe().get(0).getSignature()).isNotNull();
        assertThat(xml).contains("Signature");
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
