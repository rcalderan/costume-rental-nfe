package br.com.costumerental.nfe.xml;

import br.com.costumerental.nfe.api.dto.CustomerInfo;
import br.com.costumerental.nfe.api.dto.NfeEmissionRequest;
import br.com.costumerental.nfe.api.dto.NfeItemRequest;
import br.com.costumerental.nfe.config.NfeProperties;
import br.com.costumerental.nfe.domain.Firm;
import br.com.costumerental.nfe.domain.NfeIssuer;
import br.com.swconsultoria.nfe.schema_4.enviNFe.TEnviNFe;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class NfeXmlAssemblerTest {

    private NfeXmlAssembler assembler;
    private NfeIssuer issuer;

    @BeforeEach
    void setUp() {
        issuer = buildIssuer("00000000000191", "111111111111");
        assembler = createAssembler();
    }

    private NfeXmlAssembler createAssembler() {
        return createAssembler("55");
    }

    private NfeXmlAssembler createAssembler(String modelo) {
        NfeProperties properties = new NfeProperties();
        NfeProperties.EmitProperties emit = new NfeProperties.EmitProperties();
        emit.setCrt("1");
        NfeProperties.EnderecoProperties end = new NfeProperties.EnderecoProperties();
        emit.setEndereco(end);
        properties.setEmit(emit);
        properties.setAmbiente("2");
        properties.setSerie("1");
        properties.setModelo(modelo);
        properties.setProcessoVersao("1.0");
        if ("65".equals(modelo)) {
            NfeProperties.NfceProperties nfce = new NfeProperties.NfceProperties();
            nfce.setQrcodeUrl("https://www.homologacao.nfce.fazenda.sp.gov.br/qrcode");
            nfce.setConsultaUrl("https://www.homologacao.nfce.fazenda.sp.gov.br/consulta");
            properties.setNfce(nfce);
        }
        AccessKeyGenerator accessKeyGenerator = new AccessKeyGenerator(properties);
        IbgeCityCodeResolver cityCodeResolver = new IbgeCityCodeResolver();
        return new NfeXmlAssembler(properties, accessKeyGenerator, cityCodeResolver);
    }

    private NfeIssuer buildIssuer(String cnpj14, String ie) {
        String root = cnpj14.substring(0, 8);
        String branch = cnpj14.substring(8, 12);
        String dv = cnpj14.substring(12, 14);
        Firm firm = new Firm();
        firm.setRootCnpj(root);
        firm.setBranchOrder(branch);
        firm.setDigit(dv);
        firm.setRazaoSocial("Emitente Homologacao");
        firm.setCrt("1");
        firm.setPaisCodigo("1058");
        firm.setPaisNome("BRASIL");
        NfeIssuer iss = new NfeIssuer();
        iss.setFirm(firm);
        iss.setIe(ie);
        iss.setFone("");
        iss.setLogradouro("Rua Teste");
        iss.setNumero("0");
        iss.setBairro("Centro");
        iss.setMunicipioCodigo("3548906");
        iss.setMunicipioNome("Sao Carlos");
        iss.setUf("SP");
        iss.setCep("13560000");
        return iss;
    }

    @Test
    void shouldBuildEnviNFeObject() {
        NfeEmissionRequest request = sampleRequest();
        TEnviNFe enviNFe = assembler.build(request, issuer);
        assertThat(enviNFe).isNotNull();
        assertThat(enviNFe.getIdLote()).isEqualTo("1");
        assertThat(enviNFe.getIndSinc()).isEqualTo("1");
        assertThat(enviNFe.getNFe()).hasSize(1);
        assertThat(enviNFe.getNFe().get(0).getInfNFe().getId()).startsWith("NFe");
    }

    @Test
    void shouldGenerateHomologationDestinationName() {
        NfeEmissionRequest request = sampleRequest();
        String xml = assembler.buildXmlString(request, issuer);
        assertThat(xml).contains("NF-E EMITIDA EM AMBIENTE DE HOMOLOGACAO - SEM VALOR FISCAL");
    }

    @Test
    void shouldSanitizeEmDashInProductDescription() {
        NfeEmissionRequest request = sampleRequest();
        NfeItemRequest item = request.getItems().get(0);
        item.setDescription("Vestido Casual Floral — M Floral");
        String xml = assembler.buildXmlString(request, issuer);
        assertThat(xml).contains("Vestido Casual Floral - M Floral");
        assertThat(xml).doesNotContain("—");
    }

    @Test
    void shouldUseExemptWhenIeBlank() {
        NfeIssuer blankIeIssuer = buildIssuer("00000000000191", "");
        NfeEmissionRequest request = sampleRequest();
        String xml = assembler.buildXmlString(request, blankIeIssuer);
        assertThat(xml).contains("<IE>ISENTO</IE>");
        assertThat(xml.indexOf("<IE>")).isLessThan(xml.indexOf("<CRT>"));
        assertThat(assembler.build(request, blankIeIssuer)).isNotNull();
    }

    @Test
    void shouldEmitWithFilialCnpj() {
        NfeIssuer filial = buildIssuer("00000000000282", "111111111111");
        NfeEmissionRequest request = sampleRequest();
        String xml = assembler.buildXmlString(request, filial);
        assertThat(xml).contains("<CNPJ>00000000000282</CNPJ>");
        assertThat(xml).contains("Emitente Homologacao");
    }

    @Test
    void shouldBuildNfceWithModel65TpImp4AndIndPres1() {
        NfeXmlAssembler nfceAssembler = createAssembler("65");
        NfeEmissionRequest request = sampleRequest();

        String xml = nfceAssembler.buildXmlString(request, issuer);

        assertThat(xml).contains("<mod>65</mod>");
        assertThat(xml).contains("<tpImp>4</tpImp>");
        assertThat(xml).contains("<indPres>1</indPres>");
    }

    @Test
    void shouldIncludeInfNFeSupplWithQrCodeForNfce() {
        NfeXmlAssembler nfceAssembler = createAssembler("65");
        NfeEmissionRequest request = sampleRequest();

        String xml = nfceAssembler.buildXmlString(request, issuer);

        assertThat(xml).contains("<infNFeSupl>");
        assertThat(xml).contains("<qrCode>");
        assertThat(xml).contains("|3|2");
        assertThat(xml).contains("<urlChave>https://www.homologacao.nfce.fazenda.sp.gov.br/consulta</urlChave>");
    }

    @Test
    void shouldFailNfceWithoutQrCodeConfiguration() {
        NfeProperties properties = missingNfceProperties();
        NfeXmlAssembler nfceAssembler = new NfeXmlAssembler(properties, new AccessKeyGenerator(properties),
                new IbgeCityCodeResolver());
        NfeEmissionRequest request = sampleRequest();

        org.assertj.core.api.Assertions.assertThatThrownBy(() -> nfceAssembler.buildXmlString(request, issuer))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("nfe.nfce.qrcode-url");
    }

    private NfeProperties missingNfceProperties() {
        NfeProperties properties = new NfeProperties();
        NfeProperties.EmitProperties emit = new NfeProperties.EmitProperties();
        emit.setCrt("1");
        emit.setEndereco(new NfeProperties.EnderecoProperties());
        properties.setEmit(emit);
        properties.setAmbiente("2");
        properties.setSerie("1");
        properties.setModelo("65");
        properties.setProcessoVersao("1.0");
        return properties;
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
