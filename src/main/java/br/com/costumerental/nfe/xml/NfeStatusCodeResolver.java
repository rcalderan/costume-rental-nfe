package br.com.costumerental.nfe.xml;

import br.com.costumerental.nfe.domain.NfeStatus;
import org.springframework.stereotype.Component;

import java.util.Set;

@Component
public class NfeStatusCodeResolver {

    private static final Set<String> AUTHORIZED_CODES = Set.of("100", "102", "150");
    private static final String PROCESSING_CODE = "103";

    public boolean isAuthorized(String cStat) {
        return AUTHORIZED_CODES.contains(cStat);
    }

    public NfeStatus resolve(String cStat) {
        if (cStat == null) {
            return NfeStatus.ERROR;
        }
        if (AUTHORIZED_CODES.contains(cStat)) {
            return NfeStatus.AUTHORIZED;
        }
        if (PROCESSING_CODE.equals(cStat) || cStat.startsWith("1")) {
            return NfeStatus.PROCESSING;
        }
        return NfeStatus.REJECTED;
    }
}
