package br.com.costumerental.nfe.application;

import br.com.costumerental.nfe.api.dto.CancelNfeRequest;
import br.com.costumerental.nfe.api.dto.NfeEventResponse;
import br.com.costumerental.nfe.config.NfeProperties;
import br.com.costumerental.nfe.infrastructure.sefaz.NfeLibraryAdapter;
import br.com.costumerental.nfe.infrastructure.sefaz.NfeSefazConfigProvider;
import br.com.costumerental.nfe.xml.NfeEventResponseMapper;
import br.com.costumerental.nfe.xml.NfeEventXmlAssembler;
import br.com.swconsultoria.nfe.dom.ConfiguracoesNfe;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class NfeEventServiceTest {

    private static final String ACCESS_KEY = "35260800000000000000550010000000011000000010";

    @Mock
    private NfeSefazConfigProvider configProvider;

    @Mock
    private NfeLibraryAdapter libraryAdapter;

    @Mock
    private NfeEventXmlAssembler eventAssembler;

    @Mock
    private NfeEventResponseMapper eventResponseMapper;

    @Mock
    private FiscalDocumentEventService fiscalDocumentEventService;

    @Mock
    private NfeProperties properties;

    private NfeEventService service;

    @BeforeEach
    void setUp() {
        service = new NfeEventService(configProvider, libraryAdapter, eventAssembler, eventResponseMapper,
                fiscalDocumentEventService, properties);
    }

    @Test
    void shouldCancelNfeAndReturnAuthorizedResponse() throws Exception {
        CancelNfeRequest request = CancelNfeRequest.builder()
                .protocol("135260000000000")
                .justification("Cancelamento por erro")
                .sequence("1")
                .build();

        br.com.swconsultoria.nfe.schema.envEventoCancNFe.TEnvEvento envEvento = new br.com.swconsultoria.nfe.schema.envEventoCancNFe.TEnvEvento();
        when(configProvider.buildConfig()).thenReturn(new ConfiguracoesNfe());
        when(eventAssembler.buildCancelamento(ACCESS_KEY, request)).thenReturn(envEvento);
        when(libraryAdapter.toXml(any())).thenReturn("<xml/>");
        when(libraryAdapter.cancelarNfe(any(), any())).thenReturn(new br.com.swconsultoria.nfe.schema.envEventoCancNFe.TRetEnvEvento());

        NfeEventResponse expected = NfeEventResponse.builder()
                .status(br.com.costumerental.nfe.domain.NfeStatus.AUTHORIZED)
                .statusCode("135")
                .build();
        when(eventResponseMapper.mapCancelamento(any())).thenReturn(expected);

        NfeEventResponse response = service.cancelar(ACCESS_KEY, request);

        assertThat(response.getStatus()).isEqualTo(br.com.costumerental.nfe.domain.NfeStatus.AUTHORIZED);
    }
}
