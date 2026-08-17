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
import br.com.costumerental.nfe.xml.NfeStatusCodeResolver;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Optional;

@Service
public class FiscalDocumentService {

    private static final String DOCUMENT_TYPE_NFE = "NFE";
    private static final String DOCUMENT_TYPE_NFCE = "NFCE";
    private static final int MODELO_START_INDEX = 20;
    private static final int MODELO_END_INDEX = 22;

    private final FiscalDocumentRepository repository;
    private final NfeStatusRepository statusRepository;
    private final NfeDocumentTypeRepository documentTypeRepository;
    private final NfeIssuerRepository issuerRepository;
    private final FiscalDocumentXmlRepository xmlRepository;
    private final NfeStatusCodeResolver statusCodeResolver;

    public FiscalDocumentService(FiscalDocumentRepository repository,
                                 NfeStatusRepository statusRepository,
                                 NfeDocumentTypeRepository documentTypeRepository,
                                 NfeIssuerRepository issuerRepository,
                                 FiscalDocumentXmlRepository xmlRepository,
                                 NfeStatusCodeResolver statusCodeResolver) {
        this.repository = repository;
        this.statusRepository = statusRepository;
        this.documentTypeRepository = documentTypeRepository;
        this.issuerRepository = issuerRepository;
        this.xmlRepository = xmlRepository;
        this.statusCodeResolver = statusCodeResolver;
    }

    @Transactional
    public FiscalDocument saveSigned(String accessKey, String signedXml, NfeEmissionRequest request) {
        NfeIssuer issuer = issuerRepository.findFirstByActiveTrueOrderByFirmBranchOrderAsc()
                .orElseThrow(() -> new IllegalStateException("Nenhum emitente ativo encontrado para salvar documento fiscal"));
        NfeStatusEntity processingStatus = statusEntity(NfeStatus.PROCESSING);
        String docTypeCode = documentTypeCodeFromAccessKey(accessKey);
        NfeDocumentTypeEntity docType = documentTypeRepository.findByCode(docTypeCode)
                .orElseThrow(() -> new IllegalStateException("Tipo de documento " + docTypeCode + " nao encontrado na base"));
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
        statusCodeResolver.upsertSefazStatus(response.getStatusCode(), response.getStatusMessage());
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
        statusCodeResolver.upsertSefazStatus(statusCode, null);
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

    /**
     * O modelo do documento fiscal ocupa as posicoes 21 e 22 da chave de
     * acesso de 44 digitos (55 = NF-e, 65 = NFC-e).
     */
    private String documentTypeCodeFromAccessKey(String accessKey) {
        if (accessKey == null || accessKey.length() < MODELO_END_INDEX) {
            throw new IllegalArgumentException("Chave de acesso invalida para identificar o modelo: " + accessKey);
        }
        String modelo = accessKey.substring(MODELO_START_INDEX, MODELO_END_INDEX);
        return "65".equals(modelo) ? DOCUMENT_TYPE_NFCE : DOCUMENT_TYPE_NFE;
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
