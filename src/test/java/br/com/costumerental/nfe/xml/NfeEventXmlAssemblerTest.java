package br.com.costumerental.nfe.xml;

import br.com.costumerental.nfe.api.dto.CancelNfeRequest;
import br.com.costumerental.nfe.api.dto.CorrectionLetterRequest;
import br.com.costumerental.nfe.api.dto.NfeInutilizacaoRequest;
import br.com.costumerental.nfe.config.NfeProperties;
import br.com.costumerental.nfe.domain.ManifestacaoDestinatarioType;
import br.com.swconsultoria.nfe.schema.envConfRecebto.TEnvEvento;
import br.com.swconsultoria.nfe.schema_4.inutNFe.TInutNFe;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class NfeEventXmlAssemblerTest {

    private static final String ACCESS_KEY = "35260800000000000191550010000000011000000010";

    private NfeEventXmlAssembler assembler;

    @BeforeEach
    void setUp() {
        NfeProperties properties = new NfeProperties();
        NfeProperties.EmitProperties emit = new NfeProperties.EmitProperties();
        emit.setCnpj("00000000000191");
        emit.setIe("111111111111");
        emit.setUf("SP");
        properties.setEmit(emit);
        properties.setAmbiente("2");
        assembler = new NfeEventXmlAssembler(properties);
    }

    @Test
    void shouldBuildCancelamentoEnvEvento() {
        CancelNfeRequest request = CancelNfeRequest.builder()
                .protocol("135260000000000")
                .justification("Cancelamento por erro na emissao")
                .sequence("1")
                .build();

        br.com.swconsultoria.nfe.schema.envEventoCancNFe.TEnvEvento envEvento = assembler.buildCancelamento(ACCESS_KEY, request);

        assertThat(envEvento.getIdLote()).isEqualTo("1");
        assertThat(envEvento.getVersao()).isEqualTo("1.00");
        assertThat(envEvento.getEvento()).hasSize(1);

        var inf = envEvento.getEvento().get(0).getInfEvento();
        assertThat(inf.getTpEvento()).isEqualTo("110111");
        assertThat(inf.getChNFe()).isEqualTo(ACCESS_KEY);
        assertThat(inf.getCNPJ()).isEqualTo("00000000000191");
        assertThat(inf.getDetEvento().getNProt()).isEqualTo("135260000000000");
        assertThat(inf.getNSeqEvento()).isEqualTo("1");
        assertThat(inf.getId()).isEqualTo("ID110111" + ACCESS_KEY + "01");
        assertThat(inf.getDhEvento()).matches("\\d{4}-\\d{2}-\\d{2}T\\d{2}:\\d{2}:\\d{2}[+-]\\d{2}:\\d{2}");
    }

    @Test
    void shouldBuildCartaCorrecaoEnvEvento() {
        CorrectionLetterRequest request = CorrectionLetterRequest.builder()
                .correction("Correcao da descricao do produto")
                .sequence("1")
                .build();

        br.com.swconsultoria.nfe.schema.envcce.TEnvEvento envEvento = assembler.buildCartaCorrecao(ACCESS_KEY, request);

        assertThat(envEvento.getIdLote()).isEqualTo("1");
        assertThat(envEvento.getVersao()).isEqualTo("1.00");
        assertThat(envEvento.getEvento()).hasSize(1);

        var inf = envEvento.getEvento().get(0).getInfEvento();
        assertThat(inf.getTpEvento()).isEqualTo("110110");
        assertThat(inf.getDetEvento().getXCorrecao()).isEqualTo("Correcao da descricao do produto");
    }

    @Test
    void shouldBuildManifestacaoEnvEvento() {
        TEnvEvento envEvento = assembler.buildManifestacao(ACCESS_KEY, ManifestacaoDestinatarioType.CIENCIA_OPERACAO);

        assertThat(envEvento.getIdLote()).isEqualTo("1");
        assertThat(envEvento.getEvento()).hasSize(1);

        var inf = envEvento.getEvento().get(0).getInfEvento();
        assertThat(inf.getTpEvento()).isEqualTo("210210");
        assertThat(inf.getCOrgao()).isEqualTo("91");
        assertThat(inf.getDetEvento().getDescEvento()).isEqualTo("Ciencia da Operacao");
    }

    @Test
    void shouldBuildInutilizacaoObject() {
        NfeInutilizacaoRequest request = NfeInutilizacaoRequest.builder()
                .year("26")
                .cnpj("00000000000191")
                .model("55")
                .series("1")
                .initialNumber("10")
                .finalNumber("15")
                .justification("Inutilizacao por erro na numeracao")
                .build();

        TInutNFe inutNFe = assembler.buildInutilizacao(request);

        assertThat(inutNFe.getVersao()).isEqualTo("4.00");
        assertThat(inutNFe.getInfInut().getXServ()).isEqualTo("INUTILIZAR");
        assertThat(inutNFe.getInfInut().getCUF()).isEqualTo("35");
        assertThat(inutNFe.getInfInut().getMod()).isEqualTo("55");
        assertThat(inutNFe.getInfInut().getNNFIni()).isEqualTo("000000010");
        assertThat(inutNFe.getInfInut().getId()).startsWith("ID35260000000000019155");
    }
}
