package br.com.costumerental.nfe.xml;

import br.com.costumerental.nfe.api.dto.NfeEmissionResponse;
import br.com.costumerental.nfe.domain.NfeStatus;
import br.com.costumerental.nfe.repository.SefazStatusRepository;
import br.com.swconsultoria.nfe.schema_4.retConsReciNFe.TRetConsReciNFe;
import br.com.swconsultoria.nfe.schema_4.retConsSitNFe.TProtNFe;
import br.com.swconsultoria.nfe.schema_4.retConsSitNFe.TRetConsSitNFe;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThat;

@ExtendWith(MockitoExtension.class)
class NfeConsultaResponseMapperTest {

    @Mock
    private SefazStatusRepository sefazStatusRepository;

    private NfeConsultaResponseMapper mapper;

    @BeforeEach
    void setUp() {
        mapper = new NfeConsultaResponseMapper(new NfeStatusCodeResolver(sefazStatusRepository));
    }

    @Test
    void shouldMapAuthorizedSituacao() {
        TRetConsSitNFe retorno = new TRetConsSitNFe();
        retorno.setCStat("100");
        retorno.setXMotivo("Autorizado o uso da NF-e");
        retorno.setChNFe("35260800000000000000550010000000011000000010");

        TProtNFe protNFe = new TProtNFe();
        TProtNFe.InfProt infProt = new TProtNFe.InfProt();
        infProt.setNProt("135260000000000");
        protNFe.setInfProt(infProt);
        retorno.setProtNFe(protNFe);

        NfeEmissionResponse response = mapper.mapSituacao(retorno);

        assertThat(response.getStatus()).isEqualTo(NfeStatus.AUTHORIZED);
        assertThat(response.getProtocol()).isEqualTo("135260000000000");
        assertThat(response.getAccessKey()).isEqualTo("35260800000000000000550010000000011000000010");
    }

    @Test
    void shouldMapNaoEncontradaSituacao() {
        TRetConsSitNFe retorno = new TRetConsSitNFe();
        retorno.setCStat("217");
        retorno.setXMotivo("NF-e nao consta na base de dados da SEFAZ");

        NfeEmissionResponse response = mapper.mapSituacao(retorno);

        assertThat(response.getStatus()).isEqualTo(NfeStatus.REJECTED);
        assertThat(response.getProtocol()).isNull();
    }

    @Test
    void shouldMapRecibo() {
        TRetConsReciNFe retorno = new TRetConsReciNFe();
        retorno.setCStat("104");
        retorno.setXMotivo("Lote processado");
        retorno.setNRec("351000000000000");

        br.com.swconsultoria.nfe.schema_4.retConsReciNFe.TProtNFe protNFe =
                new br.com.swconsultoria.nfe.schema_4.retConsReciNFe.TProtNFe();
        br.com.swconsultoria.nfe.schema_4.retConsReciNFe.TProtNFe.InfProt infProt =
                new br.com.swconsultoria.nfe.schema_4.retConsReciNFe.TProtNFe.InfProt();
        infProt.setChNFe("35260800000000000000550010000000011000000010");
        infProt.setNProt("135260000000000");
        protNFe.setInfProt(infProt);
        retorno.getProtNFe().add(protNFe);

        NfeEmissionResponse response = mapper.mapRecibo(retorno);

        assertThat(response.getReceiptNumber()).isEqualTo("351000000000000");
        assertThat(response.getAccessKey()).isEqualTo("35260800000000000000550010000000011000000010");
        assertThat(response.getProtocol()).isEqualTo("135260000000000");
    }
}
