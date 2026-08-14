package br.com.costumerental.nfe.xml;

import br.com.costumerental.nfe.domain.NfeStatus;
import br.com.costumerental.nfe.domain.SefazStatus;
import br.com.costumerental.nfe.repository.SefazStatusRepository;
import org.springframework.stereotype.Component;

import java.util.Set;

@Component
public class NfeStatusCodeResolver {

    private static final Set<String> AUTHORIZED_CODES = Set.of("100", "102", "135", "136", "150");
    private static final Set<String> CANCELLED_CODES = Set.of("101", "151");
    private static final String PROCESSING_CODE = "103";

    private final SefazStatusRepository sefazStatusRepository;

    public NfeStatusCodeResolver(SefazStatusRepository sefazStatusRepository) {
        this.sefazStatusRepository = sefazStatusRepository;
    }

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
        if (CANCELLED_CODES.contains(cStat)) {
            return NfeStatus.CANCELLED;
        }
        if (PROCESSING_CODE.equals(cStat) || cStat.startsWith("1")) {
            return NfeStatus.PROCESSING;
        }
        return NfeStatus.REJECTED;
    }

    public void upsertSefazStatus(String cStat, String xMotivo) {
        if (cStat == null || cStat.isBlank()) {
            return;
        }
        if (!sefazStatusRepository.existsById(cStat)) {
            NfeStatus category = resolve(cStat);
            sefazStatusRepository.save(new SefazStatus(cStat, xMotivo, category.name()));
        }
    }
}
