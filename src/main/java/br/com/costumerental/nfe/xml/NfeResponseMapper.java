package br.com.costumerental.nfe.xml;

import br.com.costumerental.nfe.api.dto.NfeEmissionResponse;
import br.com.swconsultoria.nfe.schema_4.enviNFe.TProtNFe;
import br.com.swconsultoria.nfe.schema_4.enviNFe.TRetEnviNFe;
import org.springframework.stereotype.Component;

@Component
public class NfeResponseMapper {

    private final NfeStatusCodeResolver statusCodeResolver;

    public NfeResponseMapper(NfeStatusCodeResolver statusCodeResolver) {
        this.statusCodeResolver = statusCodeResolver;
    }

    public NfeEmissionResponse map(TRetEnviNFe retEnviNFe, String signedXml) {
        return map(retEnviNFe, signedXml, null);
    }

    public NfeEmissionResponse map(TRetEnviNFe retEnviNFe, String signedXml, String authorizedXml) {
        TProtNFe.InfProt infProt = infProt(retEnviNFe);
        String cStat = infProt != null ? infProt.getCStat() : retEnviNFe.getCStat();
        String xMotivo = infProt != null ? infProt.getXMotivo() : retEnviNFe.getXMotivo();
        String receipt = retEnviNFe.getInfRec() != null ? retEnviNFe.getInfRec().getNRec() : null;

        String protocol = infProt != null ? infProt.getNProt() : null;
        String accessKey = infProt != null ? infProt.getChNFe() : null;

        return NfeEmissionResponse.builder()
                .accessKey(accessKey)
                .receiptNumber(receipt)
                .protocol(protocol)
                .status(statusCodeResolver.resolve(cStat))
                .statusCode(cStat)
                .statusMessage(xMotivo)
                .signedXml(signedXml)
                .authorizedXml(authorizedXml)
                .build();
    }

    public boolean isAuthorized(TRetEnviNFe retEnviNFe) {
        TProtNFe.InfProt infProt = infProt(retEnviNFe);
        String cStat = infProt != null ? infProt.getCStat() : retEnviNFe.getCStat();
        return statusCodeResolver.isAuthorized(cStat);
    }

    private TProtNFe.InfProt infProt(TRetEnviNFe retEnviNFe) {
        return retEnviNFe.getProtNFe() != null ? retEnviNFe.getProtNFe().getInfProt() : null;
    }
}
