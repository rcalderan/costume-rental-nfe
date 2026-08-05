package br.com.costumerental.nfe.xml;

import br.com.costumerental.nfe.api.dto.CustomerInfo;
import br.com.costumerental.nfe.api.dto.NfeEmissionRequest;
import br.com.costumerental.nfe.api.dto.NfeItemRequest;
import br.com.costumerental.nfe.config.NfeProperties;
import br.com.swconsultoria.nfe.schema_4.enviNFe.TEnviNFe;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class NfeXmlAssemblerTest {

    private NfeXmlAssembler assembler;

    @BeforeEach
    void setUp() {
        assembler = createAssembler("111111111111");
    }

    private NfeXmlAssembler createAssembler(String ie) {
        NfeProperties properties = new NfeProperties();
        NfeProperties.EmitProperties emit = new NfeProperties.EmitProperties();
        emit.setCnpj("00000000000191");
        emit.setIe(ie);
        emit.setUf("SP");
        emit.setRazaoSocial("Emitente Homologacao");
        emit.setCrt("1");

        NfeProperties.EnderecoProperties end = new NfeProperties.EnderecoProperties();
        end.setLogradouro("Rua Teste");
        end.setNumero("0");
        end.setBairro("Centro");
        end.setMunicipioCodigo("3548906");
        end.setMunicipioNome("Sao Carlos");
        end.setUf("SP");
        end.setCep("13560000");
        end.setPaisCodigo("1058");
        end.setPaisNome("BRASIL");
        emit.setEndereco(end);
        properties.setEmit(emit);
        properties.setAmbiente("2");
        properties.setSerie("1");
        properties.setModelo("55");
        properties.setProcessoVersao("1.0");

        AccessKeyGenerator accessKeyGenerator = new AccessKeyGenerator(properties);
        IbgeCityCodeResolver cityCodeResolver = new IbgeCityCodeResolver();
        return new NfeXmlAssembler(properties, accessKeyGenerator, cityCodeResolver);
    }

    @Test
    void shouldBuildEnviNFeObject() {
        NfeEmissionRequest request = sampleRequest();

        TEnviNFe enviNFe = assembler.build(request);

        assertThat(enviNFe).isNotNull();
        assertThat(enviNFe.getIdLote()).isEqualTo("1");
        assertThat(enviNFe.getIndSinc()).isEqualTo("1");
        assertThat(enviNFe.getNFe()).hasSize(1);
        assertThat(enviNFe.getNFe().get(0).getInfNFe().getId()).startsWith("NFe");
    }

    @Test
    void shouldGenerateHomologationDestinationName() {
        NfeEmissionRequest request = sampleRequest();
        String xml = assembler.buildXmlString(request);

        assertThat(xml).contains("NF-E EMITIDA EM AMBIENTE DE HOMOLOGACAO - SEM VALOR FISCAL");
    }

    @Test
    void shouldOmitIeWhenBlank() {
        NfeXmlAssembler blankIeAssembler = createAssembler("");
        NfeEmissionRequest request = sampleRequest();
        String xml = blankIeAssembler.buildXmlString(request);

        assertThat(xml).doesNotContain("<IE>");
        assertThat(blankIeAssembler.build(request)).isNotNull();
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
