package br.com.costumerental.nfe.application;

import br.com.costumerental.nfe.api.dto.NfeEventResponse;
import br.com.costumerental.nfe.api.dto.NfeInutilizacaoRequest;
import br.com.costumerental.nfe.api.dto.NfeInutilizacaoResponse;
import br.com.costumerental.nfe.domain.Cnpj;
import br.com.costumerental.nfe.domain.Empresa;
import br.com.costumerental.nfe.domain.FiscalDocument;
import br.com.costumerental.nfe.domain.FiscalDocumentEvent;
import br.com.costumerental.nfe.domain.FiscalDocumentInutilization;
import br.com.costumerental.nfe.domain.NfeEventType;
import br.com.costumerental.nfe.domain.NfeEventTypeEntity;
import br.com.costumerental.nfe.domain.NfeIssuer;
import br.com.costumerental.nfe.domain.NfeStatus;
import br.com.costumerental.nfe.repository.FiscalDocumentEventRepository;
import br.com.costumerental.nfe.repository.FiscalDocumentInutilizationRepository;
import br.com.costumerental.nfe.repository.FiscalEventXmlRepository;
import br.com.costumerental.nfe.repository.NfeEventTypeRepository;
import br.com.costumerental.nfe.repository.NfeIssuerRepository;
import br.com.costumerental.nfe.xml.NfeStatusCodeResolver;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class FiscalDocumentEventServiceTest {

    @Mock
    private FiscalDocumentEventRepository eventRepository;
    @Mock
    private FiscalDocumentInutilizationRepository inutilizationRepository;
    @Mock
    private FiscalDocumentService fiscalDocumentService;
    @Mock
    private NfeEventTypeRepository eventTypeRepository;
    @Mock
    private FiscalEventXmlRepository eventXmlRepository;
    @Mock
    private NfeIssuerRepository issuerRepository;
    @Mock
    private NfeStatusCodeResolver statusCodeResolver;

    private FiscalDocumentEventService service;

    @BeforeEach
    void setUp() {
        service = new FiscalDocumentEventService(eventRepository, inutilizationRepository, fiscalDocumentService,
                eventTypeRepository, eventXmlRepository, issuerRepository, statusCodeResolver);
    }

    @Test
    void saveEvent_shouldUpsertUnknownStatusCodeBeforeSaving() {
        FiscalDocument document = new FiscalDocument();
        document.setId(1L);
        when(fiscalDocumentService.findByAccessKey("123")).thenReturn(Optional.of(document));
        when(eventTypeRepository.findByCode("CANC")).thenReturn(Optional.of(new NfeEventTypeEntity("CANC", "Cancelamento")));
        when(eventRepository.save(any())).thenReturn(new FiscalDocumentEvent());

        NfeEventResponse response = NfeEventResponse.builder()
                .status(NfeStatus.CANCELLED)
                .statusCode("777")
                .statusMessage("Cancelamento homologado")
                .build();

        service.saveEvent("123", NfeEventType.CANCELAMENTO, "1", "<event/>", response);

        verify(statusCodeResolver).upsertSefazStatus("777", "Cancelamento homologado");
        verify(eventRepository).save(any(FiscalDocumentEvent.class));
    }

    @Test
    void saveInutilization_shouldUpsertUnknownStatusCodeBeforeSaving() {
        Empresa empresa = new Empresa();
        empresa.setRootCnpj("08299621");
        empresa.setRazaoSocial("Emitente");
        empresa.setCrt("1");
        empresa.setPaisCodigo("1058");
        empresa.setPaisNome("BRASIL");
        NfeIssuer issuer = new NfeIssuer();
        issuer.setId(1L);
        issuer.setEmpresa(empresa);
        issuer.setBranchOrder("0001");
        issuer.setDigitoControle("20");
        when(issuerRepository.findByEmpresaRootCnpjAndBranchOrder("08299621", "0001")).thenReturn(Optional.of(issuer));
        when(inutilizationRepository.save(any())).thenReturn(new FiscalDocumentInutilization());

        NfeInutilizacaoRequest request = NfeInutilizacaoRequest.builder()
                .cnpj("08299621000120")
                .year("2026")
                .model("55")
                .series("1")
                .initialNumber("1")
                .finalNumber("10")
                .justification("Erro")
                .build();
        NfeInutilizacaoResponse response = NfeInutilizacaoResponse.builder()
                .status(NfeStatus.AUTHORIZED)
                .statusCode("666")
                .statusMessage("Inutilizacao homologada")
                .build();

        service.saveInutilization("<inut/>", request, response);

        verify(statusCodeResolver).upsertSefazStatus("666", "Inutilizacao homologada");
        verify(inutilizationRepository).save(any(FiscalDocumentInutilization.class));
    }
}
