package br.com.costumerental.nfe.xml;

import br.com.costumerental.nfe.api.dto.NfeEmissionResponse;
import br.com.costumerental.nfe.domain.NfeStatus;
import br.com.costumerental.nfe.repository.SefazStatusRepository;
import br.com.swconsultoria.nfe.schema_4.enviNFe.TProtNFe;
import br.com.swconsultoria.nfe.schema_4.enviNFe.TRetEnviNFe;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThat;

@ExtendWith(MockitoExtension.class)
class NfeResponseMapperTest {

    @Mock
    private SefazStatusRepository sefazStatusRepository;

    private NfeResponseMapper mapper;

    @BeforeEach
    void setUp() {
        mapper = new NfeResponseMapper(new NfeStatusCodeResolver(sefazStatusRepository));
    }

    @Test
    void shouldMapNfceQrCodeAndConsultaUrl() {
        TRetEnviNFe retorno = new TRetEnviNFe();
        retorno.setCStat("100");
        retorno.setXMotivo("Autorizado o uso da NFC-e");

        TProtNFe protNFe = new TProtNFe();
        TProtNFe.InfProt infProt = new TProtNFe.InfProt();
        infProt.setCStat("100");
        infProt.setXMotivo("Autorizado o uso da NFC-e");
        infProt.setChNFe("35260800000000000000650010000000011000000010");
        infProt.setNProt("135260000000000");
        protNFe.setInfProt(infProt);
        retorno.setProtNFe(protNFe);

        NfeEmissionResponse response = mapper.map(retorno, "<signed/>", "<authorized/>",
                "https://www.homologacao.nfce.fazenda.sp.gov.br/qrcode?p=...|3|2",
                "https://www.homologacao.nfce.fazenda.sp.gov.br/consulta");

        assertThat(response.getStatus()).isEqualTo(NfeStatus.AUTHORIZED);
        assertThat(response.getQrCode()).contains("qrcode?p=");
        assertThat(response.getConsultaUrl()).isEqualTo("https://www.homologacao.nfce.fazenda.sp.gov.br/consulta");
    }
}
