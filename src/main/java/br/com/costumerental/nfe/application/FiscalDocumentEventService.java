package br.com.costumerental.nfe.application;

import br.com.costumerental.nfe.api.dto.NfeEventResponse;
import br.com.costumerental.nfe.api.dto.NfeInutilizacaoRequest;
import br.com.costumerental.nfe.api.dto.NfeInutilizacaoResponse;
import br.com.costumerental.nfe.domain.FiscalDocumentEvent;
import br.com.costumerental.nfe.domain.FiscalDocumentInutilization;
import br.com.costumerental.nfe.domain.NfeEventType;
import br.com.costumerental.nfe.domain.NfeStatus;
import br.com.costumerental.nfe.repository.FiscalDocumentEventRepository;
import br.com.costumerental.nfe.repository.FiscalDocumentInutilizationRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class FiscalDocumentEventService {

    private final FiscalDocumentEventRepository eventRepository;
    private final FiscalDocumentInutilizationRepository inutilizationRepository;
    private final FiscalDocumentService fiscalDocumentService;

    public FiscalDocumentEventService(FiscalDocumentEventRepository eventRepository,
                                       FiscalDocumentInutilizationRepository inutilizationRepository,
                                       FiscalDocumentService fiscalDocumentService) {
        this.eventRepository = eventRepository;
        this.inutilizationRepository = inutilizationRepository;
        this.fiscalDocumentService = fiscalDocumentService;
    }

    @Transactional
    public FiscalDocumentEvent saveEvent(String accessKey, NfeEventType eventType, String sequence,
                                          String eventXml, NfeEventResponse response) {
        FiscalDocumentEvent event = new FiscalDocumentEvent();
        event.setAccessKey(accessKey);
        event.setEventType(eventType);
        event.setSequence(sequence);
        event.setEventXml(eventXml);
        event.setResponseXml(response.getResponseXml());
        event.setStatusCode(response.getStatusCode());
        event.setStatusMessage(response.getStatusMessage());
        event.setProtocol(response.getProtocol());
        return eventRepository.save(event);
    }

    @Transactional
    public void updateDocumentAfterCancellation(String accessKey, NfeEventResponse response) {
        if (response.getStatus() != NfeStatus.CANCELLED) {
            return;
        }
        fiscalDocumentService.findByAccessKey(accessKey).ifPresent(doc -> {
            doc.setStatus(NfeStatus.CANCELLED);
            fiscalDocumentService.updateFromConsultation(doc, NfeStatus.CANCELLED, response.getProtocol(),
                    doc.getAuthorizedXml(), response.getStatusMessage());
        });
    }

    @Transactional
    public FiscalDocumentInutilization saveInutilization(String eventXml, NfeInutilizacaoRequest request,
                                                          NfeInutilizacaoResponse response) {
        FiscalDocumentInutilization inutilization = new FiscalDocumentInutilization();
        inutilization.setYear(request.getYear());
        inutilization.setCnpj(request.getCnpj());
        inutilization.setModel(request.getModel());
        inutilization.setSeries(request.getSeries());
        inutilization.setInitialNumber(request.getInitialNumber());
        inutilization.setFinalNumber(request.getFinalNumber());
        inutilization.setJustification(request.getJustification());
        inutilization.setResponseXml(response.getResponseXml());
        inutilization.setStatusCode(response.getStatusCode());
        inutilization.setStatusMessage(response.getStatusMessage());
        inutilization.setProtocol(response.getProtocol());
        return inutilizationRepository.save(inutilization);
    }
}
