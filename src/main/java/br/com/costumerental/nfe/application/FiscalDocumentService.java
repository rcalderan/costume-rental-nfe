package br.com.costumerental.nfe.application;

import br.com.costumerental.nfe.api.dto.NfeEmissionRequest;
import br.com.costumerental.nfe.api.dto.NfeEmissionResponse;
import br.com.costumerental.nfe.domain.FiscalDocument;
import br.com.costumerental.nfe.domain.FiscalDocumentXml;
import br.com.costumerental.nfe.domain.NfeDocumentTypeEntity;
import br.com.costumerental.nfe.domain.NfeIssuer;
import br.com.costumerental.nfe.domain.NfeStatus;
import br.com.costumerental.nfe.domain.NfeStatusEntity;
import br.com.costumerental.nfe.repository.FiscalDocumentRepository;
import br.com.costumerental.nfe.repository.FiscalDocumentXmlRepository;
import br.com.costumerental.nfe.repository.NfeDocumentTypeRepository;
import br.com.costumerental.nfe.repository.NfeIssuerRepository;
import br.com.costumerental.nfe.repository.NfeStatusRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Optional;

@Service
public class FiscalDocumentService {

    private static final String DOCUMENT_TYPE_NFE = "NFE";

    private final FiscalDocumentRepository repository;
    private final NfeStatusRepository statusRepository;
    private final NfeDocumentTypeRepository documentTypeRepository;
    private final NfeIssuerRepository issuerRepository;
    private final FiscalDocumentXmlRepository xmlRepository;

    public FiscalDocumentService(FiscalDocumentRepository repository,
                                 NfeStatusRepository statusRepository,
                                 NfeDocumentTypeRepository documentTypeRepository,
                                 NfeIssuerRepository issuerRepository,
                                 FiscalDocumentXmlRepository xmlRepository) {
        this.repository = repository;
        this.statusRepository = statusRepository;
        this.documentTypeRepository = documentTypeRepository;
        this.issuerRepository = issuerRepository;
        this.xmlRepository = xmlRepository;
    }

    @Transactional
    public FiscalDocument saveSigned(String accessKey, String signedXml, NfeEmissionRequest request) {
        NfeIssuer issuer = issuerRepository.findFirstByActiveTrueOrderByBranchOrderAsc()
                .orElseThrow(() -> new IllegalStateException("Nenhum emitente ativo encontrado para salvar documento fiscal"));
        NfeStatusEntity processingStatus = statusEntity(NfeStatus.PROCESSING);
        NfeDocumentTypeEntity docType = documentTypeRepository.findByCode(DOCUMENT_TYPE_NFE)
                .orElseThrow(() -> new IllegalStateException("Tipo de documento NFE nao encontrado na base"));
        FiscalDocumentXml signedXmlEntity = xmlRepository.save(new FiscalDocumentXml("SIGNED", signedXml));

        FiscalDocument document = new FiscalDocument();
        document.setAccessKey(accessKey);
        document.setIssuer(issuer);
        document.setDocumentType(docType);
        document.setStatus(processingStatus);
        document.setSignedXml(signedXmlEntity);
        document.setIssueDate(LocalDateTime.now());
        document.setOriginId(request.getOriginId());
        document.setOrigin(request.getOrigin());
        return repository.save(document);
    }

    @Transactional
    public void updateAfterSefaz(FiscalDocument document, NfeEmissionResponse response) {
        document.setStatus(statusEntity(response.getStatus()));
        document.setSefazStatusCode(response.getStatusCode());
        document.setProtocol(response.getProtocol());
        if (response.getAuthorizedXml() != null && !response.getAuthorizedXml().isBlank()) {
            FiscalDocumentXml authorizedXmlEntity = xmlRepository.save(
                    new FiscalDocumentXml("AUTHORIZED", response.getAuthorizedXml()));
            document.setAuthorizedXml(authorizedXmlEntity);
        }
        repository.save(document);
    }

    @Transactional
    public Optional<FiscalDocument> findByAccessKey(String accessKey) {
        return repository.findByAccessKey(accessKey);
    }

    @Transactional
    public void updateFromConsultation(FiscalDocument document, NfeStatus status, String statusCode,
                                        String protocol, String authorizedXml) {
        document.setStatus(statusEntity(status));
        if (statusCode != null) {
            document.setSefazStatusCode(statusCode);
        }
        document.setProtocol(protocol != null ? protocol : document.getProtocol());
        if (authorizedXml != null && !authorizedXml.isBlank()) {
            FiscalDocumentXml xmlEntity = xmlRepository.save(new FiscalDocumentXml("AUTHORIZED", authorizedXml));
            document.setAuthorizedXml(xmlEntity);
        }
        repository.save(document);
    }

    private NfeStatusEntity statusEntity(NfeStatus status) {
        String code = statusToCode(status);
        return statusRepository.findByCode(code)
                .orElseThrow(() -> new IllegalStateException("Status nao encontrado na base: " + code));
    }

    private String statusToCode(NfeStatus status) {
        return switch (status) {
            case AUTHORIZED -> "AUT";
            case DENIED -> "DEN";
            case REJECTED -> "REJ";
            case PROCESSING -> "PRC";
            case CANCELLED -> "CAN";
            case ERROR -> "ERR";
        };
    }
}
