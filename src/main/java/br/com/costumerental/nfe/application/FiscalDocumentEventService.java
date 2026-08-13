package br.com.costumerental.nfe.application;

import br.com.costumerental.nfe.api.dto.NfeEventResponse;
import br.com.costumerental.nfe.api.dto.NfeInutilizacaoRequest;
import br.com.costumerental.nfe.api.dto.NfeInutilizacaoResponse;
import br.com.costumerental.nfe.domain.Cnpj;
import br.com.costumerental.nfe.domain.FiscalDocument;
import br.com.costumerental.nfe.domain.FiscalDocumentEvent;
import br.com.costumerental.nfe.domain.FiscalDocumentInutilization;
import br.com.costumerental.nfe.domain.FiscalEventXml;
import br.com.costumerental.nfe.domain.NfeEventType;
import br.com.costumerental.nfe.domain.NfeEventTypeEntity;
import br.com.costumerental.nfe.domain.NfeIssuer;
import br.com.costumerental.nfe.domain.NfeStatus;
import br.com.costumerental.nfe.repository.FiscalDocumentEventRepository;
import br.com.costumerental.nfe.repository.FiscalDocumentInutilizationRepository;
import br.com.costumerental.nfe.repository.FiscalEventXmlRepository;
import br.com.costumerental.nfe.repository.NfeEventTypeRepository;
import br.com.costumerental.nfe.repository.NfeIssuerRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Service
public class FiscalDocumentEventService {

    private final FiscalDocumentEventRepository eventRepository;
    private final FiscalDocumentInutilizationRepository inutilizationRepository;
    private final FiscalDocumentService fiscalDocumentService;
    private final NfeEventTypeRepository eventTypeRepository;
    private final FiscalEventXmlRepository eventXmlRepository;
    private final NfeIssuerRepository issuerRepository;

    public FiscalDocumentEventService(FiscalDocumentEventRepository eventRepository,
                                       FiscalDocumentInutilizationRepository inutilizationRepository,
                                       FiscalDocumentService fiscalDocumentService,
                                       NfeEventTypeRepository eventTypeRepository,
                                       FiscalEventXmlRepository eventXmlRepository,
                                       NfeIssuerRepository issuerRepository) {
        this.eventRepository = eventRepository;
        this.inutilizationRepository = inutilizationRepository;
        this.fiscalDocumentService = fiscalDocumentService;
        this.eventTypeRepository = eventTypeRepository;
        this.eventXmlRepository = eventXmlRepository;
        this.issuerRepository = issuerRepository;
    }

    @Transactional
    public FiscalDocumentEvent saveEvent(String accessKey, NfeEventType eventType, String sequence,
                                          String eventXml, NfeEventResponse response) {
        FiscalDocument document = fiscalDocumentService.findByAccessKey(accessKey)
                .orElseThrow(() -> new IllegalStateException("Documento fiscal nao encontrado para accessKey: " + accessKey));
        NfeEventTypeEntity eventTypeEntity = eventTypeRepository.findByCode(eventTypeCode(eventType))
                .orElseThrow(() -> new IllegalStateException("Tipo de evento nao encontrado: " + eventType));
        FiscalEventXml eventXmlEntity = eventXmlRepository.save(new FiscalEventXml("EVENT", eventXml));
        FiscalEventXml responseXmlEntity = response.getResponseXml() != null
                ? eventXmlRepository.save(new FiscalEventXml("RESPONSE", response.getResponseXml()))
                : null;

        FiscalDocumentEvent event = new FiscalDocumentEvent();
        event.setFiscalDocument(document);
        event.setEventType(eventTypeEntity);
        event.setSequence(sequence);
        event.setEventXml(eventXmlEntity);
        event.setResponseXml(responseXmlEntity);
        event.setSefazStatusCode(response.getStatusCode());
        event.setProtocol(response.getProtocol());
        return eventRepository.save(event);
    }

    @Transactional
    public void updateDocumentAfterCancellation(String accessKey, NfeEventResponse response) {
        if (response.getStatus() != NfeStatus.CANCELLED) {
            return;
        }
        fiscalDocumentService.findByAccessKey(accessKey).ifPresent(doc -> {
            FiscalDocumentXmlResolver resolver = new FiscalDocumentXmlResolver(doc);
            fiscalDocumentService.updateFromConsultation(doc, NfeStatus.CANCELLED, response.getStatusCode(),
                    response.getProtocol(), resolver.getAuthorizedXml());
        });
    }

    @Transactional
    public FiscalDocumentInutilization saveInutilization(String eventXml, NfeInutilizacaoRequest request,
                                                          NfeInutilizacaoResponse response) {
        NfeIssuer issuer = issuerRepository.findByEmpresaRootCnpjAndBranchOrder(
                        Cnpj.parse(request.getCnpj()).root(),
                        Cnpj.parse(request.getCnpj()).branch())
                .orElseThrow(() -> new IllegalStateException("Emitente nao encontrado para CNPJ: " + request.getCnpj()));
        FiscalEventXml responseXmlEntity = response.getResponseXml() != null
                ? eventXmlRepository.save(new FiscalEventXml("RESPONSE", response.getResponseXml()))
                : null;

        FiscalDocumentInutilization inutilization = new FiscalDocumentInutilization();
        inutilization.setIssuer(issuer);
        inutilization.setYear(request.getYear());
        inutilization.setModel(request.getModel());
        inutilization.setSeries(request.getSeries());
        inutilization.setInitialNumber(request.getInitialNumber());
        inutilization.setFinalNumber(request.getFinalNumber());
        inutilization.setJustification(request.getJustification());
        inutilization.setResponseXml(responseXmlEntity);
        inutilization.setSefazStatusCode(response.getStatusCode());
        inutilization.setProtocol(response.getProtocol());
        return inutilizationRepository.save(inutilization);
    }

    private String eventTypeCode(NfeEventType eventType) {
        return switch (eventType) {
            case CANCELAMENTO -> "CANC";
            case CARTA_CORRECAO -> "CCE";
            case MANIFESTACAO_DESTINATARIO -> "MANIF";
            case INUTILIZACAO -> "INUT";
        };
    }

    private static class FiscalDocumentXmlResolver {
        private final FiscalDocument document;

        FiscalDocumentXmlResolver(FiscalDocument document) {
            this.document = document;
        }

        String getAuthorizedXml() {
            return document.getAuthorizedXml() != null ? document.getAuthorizedXml().getContent() : null;
        }
    }
}
