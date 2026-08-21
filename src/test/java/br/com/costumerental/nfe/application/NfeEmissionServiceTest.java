package br.com.costumerental.nfe.application;

import br.com.costumerental.nfe.api.dto.NfeEmissionRequest;
import br.com.costumerental.nfe.domain.Firm;
import br.com.costumerental.nfe.domain.FiscalDocument;
import br.com.costumerental.nfe.domain.NfeIssuer;
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
import org.mockito.Mockito;
import org.mockito.quality.Strictness;
import org.mockito.junit.jupiter.MockitoSettings;

import java.util.List;
import java.util.Optional;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
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
    @Mock
    private IssuerConfigService issuerConfigService;

    private NfeEmissionService service;
    private NfeIssuer issuer;

    @BeforeEach
    void setUp() {
        service = new NfeEmissionService(configProvider, xmlAssembler, libraryAdapter, responseMapper,
                fiscalDocumentService, issuerConfigService);
        issuer = buildIssuer("08299621000120");
        when(issuerConfigService.findCurrentIssuer()).thenReturn(Optional.of(issuer));
    }

    private NfeIssuer buildIssuer(String cnpj14) {
        Firm firm = new Firm();
        firm.setRootCnpj(cnpj14.substring(0, 8));
        firm.setBranchOrder(cnpj14.substring(8, 12));
        firm.setDigit(cnpj14.substring(12, 14));
        firm.setRazaoSocial("Emitente Teste");
        firm.setCrt("1");
        firm.setPaisCodigo("1058");
        firm.setPaisNome("BRASIL");
        NfeIssuer iss = new NfeIssuer();
        iss.setFirm(firm);
        iss.setUf("SP");
        iss.setActive(true);
        return iss;
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

        when(configProvider.buildConfig(issuer)).thenReturn(config);
        when(xmlAssembler.build(request, issuer)).thenReturn(enviNFe);
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

    @Test
    void shouldResolveIssuerByCnpjWhenProvided() throws Exception {
        NfeIssuer filial = buildIssuer("08299621000200");
        NfeEmissionRequest request = NfeEmissionRequest.builder()
                .issuerCnpj("08299621000200")
                .build();
        ConfiguracoesNfe config = new ConfiguracoesNfe();
        TEnviNFe enviNFe = mock(TEnviNFe.class);
        TEnviNFe signedEnviNFe = mock(TEnviNFe.class);
        String signedXml = "<signedXml/>";

        TNFe nfe = mock(TNFe.class);
        TNFe.InfNFe infNFe = mock(TNFe.InfNFe.class);
        when(infNFe.getId()).thenReturn("NFe456");
        when(nfe.getInfNFe()).thenReturn(infNFe);

        when(issuerConfigService.findByCnpj("08299621000200")).thenReturn(Optional.of(filial));
        when(configProvider.buildConfig(filial)).thenReturn(config);
        when(xmlAssembler.build(request, filial)).thenReturn(enviNFe);
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
                });
    }
}
