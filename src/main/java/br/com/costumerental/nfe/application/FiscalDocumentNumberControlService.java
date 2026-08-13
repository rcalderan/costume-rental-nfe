package br.com.costumerental.nfe.application;

import br.com.costumerental.nfe.domain.FiscalDocumentNumberControl;
import br.com.costumerental.nfe.repository.FiscalDocumentNumberControlRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class FiscalDocumentNumberControlService {

    private final FiscalDocumentNumberControlRepository repository;

    public FiscalDocumentNumberControlService(FiscalDocumentNumberControlRepository repository) {
        this.repository = repository;
    }

    @Transactional
    public String reserveNextNumber(Long issuerId, String series) {
        FiscalDocumentNumberControl.PK pk = new FiscalDocumentNumberControl.PK(issuerId, series);
        FiscalDocumentNumberControl control = repository.findById(pk)
                .orElseGet(() -> {
                    FiscalDocumentNumberControl created = new FiscalDocumentNumberControl(issuerId, series, 0L);
                    return repository.save(created);
                });
        long next = control.getLastNumber() + 1;
        control.setLastNumber(next);
        repository.save(control);
        return String.valueOf(next);
    }
}
