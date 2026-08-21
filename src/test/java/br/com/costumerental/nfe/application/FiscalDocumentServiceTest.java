package br.com.costumerental.nfe.application;

import br.com.costumerental.nfe.api.dto.NfeEmissionRequest;
import br.com.costumerental.nfe.api.dto.NfeEmissionResponse;
import br.com.costumerental.nfe.domain.FiscalDocument;
import br.com.costumerental.nfe.domain.FiscalDocumentXml;
import br.com.costumerental.nfe.domain.Firm;
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
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class FiscalDocumentServiceTest {

    @Mock
    private FiscalDocumentRepository repository;
    @Mock
    private NfeStatusRepository statusRepository;
    @Mock
    private NfeDocumentTypeRepository documentTypeRepository;
    @Mock
    private NfeIssuerRepository issuerRepository;
    @Mock
    private FiscalDocumentXmlRepository xmlRepository;
    @Mock
    private NfeStatusCodeResolver statusCodeResolver;

    private FiscalDocumentService service;

    @BeforeEach
    void setUp() {
        service = new FiscalDocumentService(repository, statusRepository, documentTypeRepository,
                issuerRepository, xmlRepository, statusCodeResolver);
    }

    @Test
    void updateAfterSefaz_shouldUpsertUnknownStatusCodeBeforeSaving() {
        FiscalDocument document = new FiscalDocument();
        when(statusRepository.findByCode("AUT")).thenReturn(Optional.of(new NfeStatusEntity("AUT", "Autorizado", "AUTHORIZED")));
        when(repository.save(any())).thenReturn(document);

        NfeEmissionResponse response = NfeEmissionResponse.builder()
                .status(NfeStatus.AUTHORIZED)
                .statusCode("999")
                .statusMessage("Status desconhecido")
                .build();

        service.updateAfterSefaz(document, response);

        verify(statusCodeResolver).upsertSefazStatus("999", "Status desconhecido");
        verify(repository).save(document);
    }

    @Test
    void saveSigned_shouldPersistNfceTypeForModel65AccessKey() {
        String nfceAccessKey = "35260800000000000000650010000000011000000010";
        NfeDocumentTypeEntity nfceType = new NfeDocumentTypeEntity("NFCE", "NFC-e");
        NfeStatusEntity processingStatus = new NfeStatusEntity("PRC", "Processando", "PROCESSING");
        Firm firm = new Firm();
        firm.setRootCnpj("00000000");
        firm.setBranchOrder("0001");
        firm.setDigit("91");
        NfeIssuer issuer = new NfeIssuer();
        issuer.setFirm(firm);
        issuer.setId(UUID.randomUUID());

        when(issuerRepository.findFirstByActiveTrueOrderByFirmBranchOrderAsc()).thenReturn(Optional.of(issuer));
        when(statusRepository.findByCode("PRC")).thenReturn(Optional.of(processingStatus));
        when(documentTypeRepository.findByCode("NFCE")).thenReturn(Optional.of(nfceType));
        when(xmlRepository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));
        when(repository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));

        FiscalDocument saved = service.saveSigned(nfceAccessKey, "<xml/>", NfeEmissionRequest.builder().build());

        assertThat(saved.getDocumentType().getCode()).isEqualTo("NFCE");
        verify(documentTypeRepository).findByCode("NFCE");
    }

    @Test
    void updateFromConsultation_shouldUpsertUnknownStatusCodeBeforeSaving() {
        FiscalDocument document = new FiscalDocument();
        when(statusRepository.findByCode("CAN")).thenReturn(Optional.of(new NfeStatusEntity("CAN", "Cancelado", "CANCELLED")));
        when(repository.save(any())).thenReturn(document);

        service.updateFromConsultation(document, NfeStatus.CANCELLED, "888", "123", null);

        verify(statusCodeResolver).upsertSefazStatus("888", null);
        verify(repository).save(document);
    }
}
