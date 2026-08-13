package br.com.costumerental.nfe.application;

import br.com.costumerental.nfe.api.dto.NfeEmissionRequest;
import br.com.costumerental.nfe.api.dto.NfeEmissionResponse;
import br.com.costumerental.nfe.domain.Cnpj;
import br.com.costumerental.nfe.domain.FiscalDocument;
import br.com.costumerental.nfe.domain.NfeIssuer;
import br.com.costumerental.nfe.infrastructure.sefaz.NfeLibraryAdapter;
import br.com.costumerental.nfe.infrastructure.sefaz.NfeSefazConfigProvider;
import br.com.costumerental.nfe.xml.NfeResponseMapper;
import br.com.costumerental.nfe.xml.NfeXmlAssembler;
import br.com.swconsultoria.certificado.exception.CertificadoException;
import br.com.swconsultoria.nfe.dom.ConfiguracoesNfe;
import br.com.swconsultoria.nfe.exception.NfeException;
import br.com.swconsultoria.nfe.schema_4.enviNFe.TEnviNFe;
import br.com.swconsultoria.nfe.schema_4.enviNFe.TNFe;
import br.com.swconsultoria.nfe.schema_4.enviNFe.TRetEnviNFe;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class NfeEmissionService {

    private final NfeSefazConfigProvider configProvider;
    private final NfeXmlAssembler xmlAssembler;
    private final NfeLibraryAdapter libraryAdapter;
    private final NfeResponseMapper responseMapper;
    private final FiscalDocumentService fiscalDocumentService;
    private final IssuerConfigService issuerConfigService;

    public NfeEmissionService(NfeSefazConfigProvider configProvider,
                              NfeXmlAssembler xmlAssembler,
                              NfeLibraryAdapter libraryAdapter,
                              NfeResponseMapper responseMapper,
                              FiscalDocumentService fiscalDocumentService,
                              IssuerConfigService issuerConfigService) {
        this.configProvider = configProvider;
        this.xmlAssembler = xmlAssembler;
        this.libraryAdapter = libraryAdapter;
        this.responseMapper = responseMapper;
        this.fiscalDocumentService = fiscalDocumentService;
        this.issuerConfigService = issuerConfigService;
    }

    public NfeEmissionResponse emit(NfeEmissionRequest request) {
        NfeIssuer issuer = resolveIssuer(request);
        ensureIssuerConfigured(issuer);
        try {
            ConfiguracoesNfe config = configProvider.buildConfig();
            TEnviNFe enviNFe = xmlAssembler.build(request, issuer);
            TEnviNFe signedEnviNFe = libraryAdapter.signAndValidate(config, enviNFe);
            String signedXml = libraryAdapter.toXml(signedEnviNFe);
            FiscalDocument document = fiscalDocumentService.saveSigned(extractAccessKey(signedEnviNFe), signedXml, request);

            TRetEnviNFe retorno;
            try {
                retorno = libraryAdapter.send(config, signedEnviNFe);
            } catch (NfeException sendException) {
                throw new br.com.costumerental.nfe.exception.NfeBusinessException(
                        "Erro na comunicacao com a SEFAZ: " + sendException.getMessage(), signedXml, sendException);
            }
            String authorizedXml = responseMapper.isAuthorized(retorno)
                    ? libraryAdapter.buildNfeProc(signedEnviNFe, retorno)
                    : null;
            NfeEmissionResponse response = responseMapper.map(retorno, signedXml, authorizedXml);
            fillNumberAndSeries(response, signedEnviNFe);
            fiscalDocumentService.updateAfterSefaz(document, response);
            return response;
        } catch (CertificadoException e) {
            throw new br.com.costumerental.nfe.exception.NfeBusinessException("Erro no certificado digital: " + e.getMessage(), e);
        } catch (NfeException e) {
            throw new br.com.costumerental.nfe.exception.NfeBusinessException("Erro ao assinar/validar XML: " + e.getMessage(), e);
        }
    }

    private NfeIssuer resolveIssuer(NfeEmissionRequest request) {
        if (request.getIssuerCnpj() != null && !request.getIssuerCnpj().isBlank()) {
            Optional<NfeIssuer> issuer = issuerConfigService.findByCnpj(request.getIssuerCnpj());
            if (issuer.isEmpty()) {
                throw new br.com.costumerental.nfe.exception.NfeBusinessException(
                        "Emitente nao encontrado para o CNPJ: " + request.getIssuerCnpj());
            }
            if (!issuer.get().isActive()) {
                throw new br.com.costumerental.nfe.exception.NfeBusinessException(
                        "Emitente inativo para o CNPJ: " + request.getIssuerCnpj());
            }
            return issuer.get();
        }
        return issuerConfigService.findCurrentIssuer()
                .orElseThrow(() -> new br.com.costumerental.nfe.exception.NfeBusinessException(
                        "Nenhum emitente ativo encontrado. Configure o CNPJ do emitente antes de emitir notas."));
    }

    private void ensureIssuerConfigured(NfeIssuer issuer) {
        if (issuer == null) {
            throw new br.com.costumerental.nfe.exception.NfeBusinessException(
                    "Emitente nao configurado. Configure o CNPJ do emitente antes de emitir notas.");
        }
    }

    private String extractAccessKey(TEnviNFe signedEnviNFe) {
        String id = signedEnviNFe.getNFe().get(0).getInfNFe().getId();
        return id.substring(3);
    }

    private void fillNumberAndSeries(NfeEmissionResponse response, TEnviNFe signedEnviNFe) {
        TNFe.InfNFe.Ide ide = signedEnviNFe.getNFe().get(0).getInfNFe().getIde();
        response.setNumber(ide.getNNF());
        response.setSeries(ide.getSerie());
    }

    public NfeEmissionResponse buildAndSign(NfeEmissionRequest request) {
        NfeIssuer issuer = resolveIssuer(request);
        ensureIssuerConfigured(issuer);
        try {
            ConfiguracoesNfe config = configProvider.buildConfig();
            TEnviNFe enviNFe = xmlAssembler.build(request, issuer);
            TEnviNFe signedEnviNFe = libraryAdapter.signAndValidate(config, enviNFe);
            String signedXml = libraryAdapter.toXml(signedEnviNFe);
            return NfeEmissionResponse.builder()
                    .signedXml(signedXml)
                    .status(br.com.costumerental.nfe.domain.NfeStatus.PROCESSING)
                    .build();
        } catch (CertificadoException e) {
            throw new br.com.costumerental.nfe.exception.NfeBusinessException("Erro no certificado digital: " + e.getMessage(), e);
        } catch (NfeException e) {
            throw new br.com.costumerental.nfe.exception.NfeBusinessException("Erro ao assinar/validar XML: " + e.getMessage(), e);
        }
    }
}
