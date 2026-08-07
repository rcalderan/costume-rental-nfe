package br.com.costumerental.nfe.application;

import br.com.costumerental.nfe.api.dto.NfeEmissionRequest;
import br.com.costumerental.nfe.domain.FiscalDocument;
import br.com.costumerental.nfe.exception.NfeBusinessException;
import br.com.costumerental.nfe.infrastructure.sefaz.NfeLibraryAdapter;
import br.com.costumerental.nfe.infrastructure.sefaz.NfeSefazConfigProvider;
import br.com.costumerental.nfe.xml.NfeResponseMapper;
import br.com.costumerental.nfe.xml.NfeXmlAssembler;
import br.com.swconsultoria.nfe.dom.ConfiguracoesNfe;
import br.com.swconsultoria.nfe.exception.NfeException;
import br.com.swconsultoria.nfe.schema_4.enviNFe.TEnviNFe;
import br.com.swconsultoria.nfe.schema_4.enviNFe.TNFe;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import java.util.List;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class NfeEmissionServiceTest {

    @Mock
    private NfeSefazConfigProvider configProvider;

    @Mock
    private NfeXmlAssembler xmlAssembler;

    @Mock
    private NfeLibraryAdapter libraryAdapter;

    @Mock
    private NfeResponseMapper responseMapper;

    @Mock
    private FiscalDocumentService fiscalDocumentService;

    private NfeEmissionService service;

    @BeforeEach
    void setUp() {
        service = new NfeEmissionService(configProvider, xmlAssembler, libraryAdapter, responseMapper,
                fiscalDocumentService);
    }

    @Test
    void shouldIncludeSignedXmlWhenSefazSendFails() throws Exception {
        NfeEmissionRequest request = NfeEmissionRequest.builder().build();
        ConfiguracoesNfe config = new ConfiguracoesNfe();
        TEnviNFe enviNFe = mock(TEnviNFe.class);
        TEnviNFe signedEnviNFe = mock(TEnviNFe.class);
        String signedXml = "<signedXml/>";

        TNFe nfe = mock(TNFe.class);
        TNFe.InfNFe infNFe = mock(TNFe.InfNFe.class);
        when(infNFe.getId()).thenReturn("NFe123");
        when(nfe.getInfNFe()).thenReturn(infNFe);

        when(configProvider.buildConfig()).thenReturn(config);
        when(xmlAssembler.build(request)).thenReturn(enviNFe);
        when(libraryAdapter.signAndValidate(config, enviNFe)).thenReturn(signedEnviNFe);
        when(libraryAdapter.toXml(signedEnviNFe)).thenReturn(signedXml);
        when(signedEnviNFe.getNFe()).thenReturn(java.util.List.of(nfe));
        when(fiscalDocumentService.saveSigned(any(), eq(signedXml), eq(request))).thenReturn(new FiscalDocument());
        when(libraryAdapter.send(config, signedEnviNFe))
                .thenThrow(new NfeException("Erro na comunicacao com a SEFAZ: rejeicao teste"));

        assertThatThrownBy(() -> service.emit(request))
                .isInstanceOf(NfeBusinessException.class)
                .satisfies(ex -> {
                    NfeBusinessException businessException = (NfeBusinessException) ex;
                    assertThat(businessException.getXml()).isEqualTo(signedXml);
                    assertThat(businessException.getMessage()).contains("Erro na comunicacao com a SEFAZ");
                });
    }
}
