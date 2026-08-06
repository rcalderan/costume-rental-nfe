package br.com.costumerental.nfe.application;

import br.com.costumerental.nfe.api.dto.NfeEmissionRequest;
import br.com.costumerental.nfe.api.dto.NfeEmissionResponse;
import br.com.costumerental.nfe.domain.FiscalDocument;
import br.com.costumerental.nfe.domain.FiscalDocumentType;
import br.com.costumerental.nfe.domain.NfeStatus;
import br.com.costumerental.nfe.repository.FiscalDocumentRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Optional;

@Service
public class FiscalDocumentService {

    private final FiscalDocumentRepository repository;

    public FiscalDocumentService(FiscalDocumentRepository repository) {
        this.repository = repository;
    }

    @Transactional
    public FiscalDocument saveSigned(String accessKey, String signedXml, NfeEmissionRequest request) {
        FiscalDocument document = new FiscalDocument();
        document.setAccessKey(accessKey);
        document.setDocumentType(FiscalDocumentType.NFE);
        document.setStatus(NfeStatus.PROCESSING);
        document.setSignedXml(signedXml);
        document.setIssueDate(LocalDateTime.now());
        document.setOriginId(request.getOriginId());
        document.setOrigin(request.getOrigin());
        return repository.save(document);
    }

    @Transactional
    public void updateAfterSefaz(FiscalDocument document, NfeEmissionResponse response) {
        document.setStatus(response.getStatus());
        document.setProtocol(response.getProtocol());
        document.setAuthorizedXml(response.getAuthorizedXml());
        document.setRejectionReason(response.getStatus() == NfeStatus.AUTHORIZED ? null : response.getStatusMessage());
        repository.save(document);
    }

    @Transactional
    public Optional<FiscalDocument> findByAccessKey(String accessKey) {
        return repository.findByAccessKey(accessKey);
    }

    @Transactional
    public void updateFromConsultation(FiscalDocument document, NfeStatus status, String protocol,
                                        String authorizedXml, String rejectionReason) {
        document.setStatus(status);
        document.setProtocol(protocol != null ? protocol : document.getProtocol());
        document.setAuthorizedXml(authorizedXml != null ? authorizedXml : document.getAuthorizedXml());
        document.setRejectionReason(rejectionReason);
        repository.save(document);
    }
}
