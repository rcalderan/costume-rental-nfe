package br.com.costumerental.nfe.xml;

import br.com.costumerental.nfe.api.dto.NfeEmissionResponse;
import br.com.swconsultoria.nfe.schema_4.consReciNFe.TRetConsReciNFe;
import br.com.swconsultoria.nfe.schema_4.consSitNFe.TRetConsSitNFe;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class NfeConsultaResponseMapper {

    private final NfeStatusCodeResolver statusCodeResolver;

    public NfeConsultaResponseMapper(NfeStatusCodeResolver statusCodeResolver) {
        this.statusCodeResolver = statusCodeResolver;
    }

    public NfeEmissionResponse mapSituacao(TRetConsSitNFe retorno) {
        String protocol = null;
        if (retorno.getProtNFe() != null && retorno.getProtNFe().getInfProt() != null) {
            protocol = retorno.getProtNFe().getInfProt().getNProt();
        }
        return NfeEmissionResponse.builder()
                .accessKey(retorno.getChNFe())
                .protocol(protocol)
                .status(statusCodeResolver.resolve(retorno.getCStat()))
                .statusCode(retorno.getCStat())
                .statusMessage(retorno.getXMotivo())
                .build();
    }

    public NfeEmissionResponse mapRecibo(TRetConsReciNFe retorno) {
        List<br.com.swconsultoria.nfe.schema_4.consReciNFe.TProtNFe> protocolos = retorno.getProtNFe();
        br.com.swconsultoria.nfe.schema_4.consReciNFe.TProtNFe.InfProt infProt =
                protocolos != null && !protocolos.isEmpty() ? protocolos.get(0).getInfProt() : null;

        String accessKey = infProt != null ? infProt.getChNFe() : null;
        String protocol = infProt != null ? infProt.getNProt() : null;
        String cStat = infProt != null ? infProt.getCStat() : retorno.getCStat();
        String xMotivo = infProt != null ? infProt.getXMotivo() : retorno.getXMotivo();

        return NfeEmissionResponse.builder()
                .accessKey(accessKey)
                .receiptNumber(retorno.getNRec())
                .protocol(protocol)
                .status(statusCodeResolver.resolve(cStat))
                .statusCode(cStat)
                .statusMessage(xMotivo)
                .build();
    }
}
