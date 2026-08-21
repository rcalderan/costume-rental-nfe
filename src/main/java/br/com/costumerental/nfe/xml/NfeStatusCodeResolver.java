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
    private static final Set<String> PROCESSING_CODES = Set.of("103", "104", "105");

    private final SefazStatusRepository sefazStatusRepository;

    public NfeStatusCodeResolver(SefazStatusRepository sefazStatusRepository) {
        this.sefazStatusRepository = sefazStatusRepository;
    }

    public boolean isAuthorized(String cStat) {
        return AUTHORIZED_CODES.contains(sanitizeCStat(cStat));
    }

    public NfeStatus resolve(String cStat) {
        String code = sanitizeCStat(cStat);
        if (code == null) {
            return NfeStatus.ERROR;
        }
        if (AUTHORIZED_CODES.contains(code)) {
            return NfeStatus.AUTHORIZED;
        }
        if (CANCELLED_CODES.contains(code)) {
            return NfeStatus.CANCELLED;
        }
        if (PROCESSING_CODES.contains(code)) {
            return NfeStatus.PROCESSING;
        }
        return NfeStatus.REJECTED;
    }

    public void upsertSefazStatus(String cStat, String xMotivo) {
        String code = sanitizeCStat(cStat);
        if (code == null || code.isBlank()) {
            return;
        }
        if (!sefazStatusRepository.existsById(code)) {
            NfeStatus category = resolve(code);
            String message = xMotivo != null && xMotivo.length() > 500 ? xMotivo.substring(0, 500) : xMotivo;
            sefazStatusRepository.save(new SefazStatus(code, message, category.name()));
        }
    }

    private String sanitizeCStat(String cStat) {
        return cStat == null ? null : cStat.trim().replaceAll("\\D", "");
    }
}
