package br.com.costumerental.nfe.xml;

import br.com.costumerental.nfe.api.dto.ConsultaCadastroResponse;
import br.com.costumerental.nfe.api.dto.DistribuicaoDfeResponse;
import br.com.costumerental.nfe.api.dto.NfeEventResponse;
import br.com.costumerental.nfe.api.dto.NfeInutilizacaoResponse;
import br.com.costumerental.nfe.domain.NfeEventType;
import br.com.costumerental.nfe.domain.NfeStatus;
import br.com.swconsultoria.nfe.schema.envEventoCancNFe.TRetEvento;
import br.com.swconsultoria.nfe.schema.envEventoCancNFe.TRetEnvEvento;
import br.com.swconsultoria.nfe.schema.retConsCad.TRetConsCad;
import br.com.swconsultoria.nfe.schema.retConsCad.TRetConsCad.InfCons.InfCad;
import br.com.swconsultoria.nfe.schema.retConsCad.TUfCons;
import br.com.swconsultoria.nfe.schema.retdistdfeint.RetDistDFeInt;
import br.com.swconsultoria.nfe.schema_4.inutNFe.TRetInutNFe;
import org.junit.jupiter.api.Test;

import javax.xml.datatype.DatatypeFactory;
import java.util.GregorianCalendar;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class NfeEventResponseMapperTest {

    private final NfeEventResponseMapper mapper = new NfeEventResponseMapper(new NfeStatusCodeResolver());

    @Test
    void shouldMapCancelamentoEventoAutorizado() {
        TRetEnvEvento retorno = new TRetEnvEvento();
        retorno.setCStat("128");
        retorno.setXMotivo("Lote de evento processado");

        TRetEvento retEvento = new TRetEvento();
        TRetEvento.InfEvento inf = new TRetEvento.InfEvento();
        inf.setCStat("135");
        inf.setXMotivo("Evento registrado");
        inf.setChNFe("35260800000000000000550010000000011000000010");
        inf.setNSeqEvento("01");
        inf.setNProt("135260000000001");
        retEvento.setInfEvento(inf);
        retorno.getRetEvento().add(retEvento);

        NfeEventResponse response = mapper.mapCancelamento(retorno);

        assertThat(response.getEventType()).isEqualTo(NfeEventType.CANCELAMENTO);
        assertThat(response.getStatus()).isEqualTo(NfeStatus.CANCELLED);
        assertThat(response.getStatusCode()).isEqualTo("135");
        assertThat(response.getProtocol()).isEqualTo("135260000000001");
    }

    @Test
    void shouldMapInutilizacaoAutorizada() {
        TRetInutNFe retorno = new TRetInutNFe();
        TRetInutNFe.InfInut inf = new TRetInutNFe.InfInut();
        inf.setCStat("102");
        inf.setXMotivo("Inutilizacao de numero homologado");
        inf.setAno("26");
        inf.setCNPJ("00000000000191");
        inf.setMod("55");
        inf.setSerie("1");
        inf.setNNFIni("000000010");
        inf.setNNFFin("000000015");
        inf.setNProt("135260000000002");
        retorno.setInfInut(inf);

        NfeInutilizacaoResponse response = mapper.mapInutilizacao(retorno);

        assertThat(response.getStatus()).isEqualTo(NfeStatus.AUTHORIZED);
        assertThat(response.getStatusCode()).isEqualTo("102");
        assertThat(response.getProtocol()).isEqualTo("135260000000002");
    }

    @Test
    void shouldMapConsultaCadastroWithOneRegistration() throws Exception {
        TRetConsCad retorno = new TRetConsCad();
        TRetConsCad.InfCons inf = new TRetConsCad.InfCons();
        inf.setCStat("111");
        inf.setXMotivo("Consulta cadastro com uma ocorrencia");
        inf.setUF(TUfCons.SP);
        inf.setCNPJ("00000000000191");
        inf.setIE("111111111111");
        inf.setDhCons(DatatypeFactory.newInstance().newXMLGregorianCalendar(new GregorianCalendar()));

        InfCad cad = new InfCad();
        cad.setIE("111111111111");
        cad.setCNPJ("00000000000191");
        cad.setXNome("Emitente Homologacao");
        cad.setUF(br.com.swconsultoria.nfe.schema.retConsCad.TUf.SP);
        inf.getInfCad().add(cad);

        retorno.setInfCons(inf);

        ConsultaCadastroResponse response = mapper.mapConsultaCadastro(retorno);

        assertThat(response.getStatusCode()).isEqualTo("111");
        assertThat(response.getRegistrations()).hasSize(1);
        assertThat(response.getRegistrations().get(0).getRazaoSocial()).isEqualTo("Emitente Homologacao");
    }

    @Test
    void shouldMapDistribuicaoDfeWithDocuments() {
        RetDistDFeInt retorno = new RetDistDFeInt();
        retorno.setCStat("138");
        retorno.setXMotivo("Documentos localizados");
        retorno.setUltNSU("000000000000001");
        retorno.setMaxNSU("000000000000010");

        RetDistDFeInt.LoteDistDFeInt lote = new RetDistDFeInt.LoteDistDFeInt();
        RetDistDFeInt.LoteDistDFeInt.DocZip doc = new RetDistDFeInt.LoteDistDFeInt.DocZip();
        doc.setNSU("000000000000001");
        doc.setSchema("resNFe");
        doc.setValue("<xml/>".getBytes());
        lote.getDocZip().add(doc);
        retorno.setLoteDistDFeInt(lote);

        DistribuicaoDfeResponse response = mapper.mapDistribuicaoDfe(retorno);

        assertThat(response.getStatusCode()).isEqualTo("138");
        assertThat(response.getDocuments()).hasSize(1);
        assertThat(response.getDocuments().get(0).getNsu()).isEqualTo("000000000000001");
    }
}
