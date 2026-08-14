package br.com.costumerental.nfe.application;

import br.com.costumerental.nfe.api.dto.NfeEmissionResponse;
import br.com.costumerental.nfe.api.dto.StatusServicoResponse;
import br.com.costumerental.nfe.domain.FiscalDocument;
import br.com.costumerental.nfe.exception.NfeBusinessException;
import br.com.costumerental.nfe.infrastructure.sefaz.NfeLibraryAdapter;
import br.com.costumerental.nfe.infrastructure.sefaz.NfeSefazConfigProvider;
import br.com.costumerental.nfe.xml.NfeConsultaResponseMapper;
import br.com.swconsultoria.certificado.exception.CertificadoException;
import br.com.swconsultoria.nfe.dom.ConfiguracoesNfe;
import br.com.swconsultoria.nfe.exception.NfeException;
import br.com.swconsultoria.nfe.schema_4.retConsReciNFe.TRetConsReciNFe;
import br.com.swconsultoria.nfe.schema_4.retConsSitNFe.TRetConsSitNFe;
import br.com.swconsultoria.nfe.schema_4.retConsStatServ.TRetConsStatServ;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class NfeConsultationService {

    private final NfeSefazConfigProvider configProvider;
    private final NfeLibraryAdapter libraryAdapter;
    private final NfeConsultaResponseMapper consultaResponseMapper;
    private final FiscalDocumentService fiscalDocumentService;

    public NfeConsultationService(NfeSefazConfigProvider configProvider,
                                   NfeLibraryAdapter libraryAdapter,
                                   NfeConsultaResponseMapper consultaResponseMapper,
                                   FiscalDocumentService fiscalDocumentService) {
        this.configProvider = configProvider;
        this.libraryAdapter = libraryAdapter;
        this.consultaResponseMapper = consultaResponseMapper;
        this.fiscalDocumentService = fiscalDocumentService;
    }

    public StatusServicoResponse consultarStatusServico() {
        try {
            ConfiguracoesNfe config = configProvider.buildConfig();
            TRetConsStatServ retorno = libraryAdapter.statusServico(config);
            return mapStatusServico(retorno);
        } catch (CertificadoException | NfeException e) {
            throw new NfeBusinessException("Erro ao consultar status do servico na SEFAZ: " + e.getMessage(), e);
        }
    }

    public NfeEmissionResponse consultarXml(String chaveAcesso) {
        try {
            ConfiguracoesNfe config = configProvider.buildConfig();
            TRetConsSitNFe retorno = libraryAdapter.consultarXml(config, chaveAcesso);
            NfeEmissionResponse response = consultaResponseMapper.mapSituacao(retorno);
            atualizarDocumento(chaveAcesso, response);
            return response;
        } catch (CertificadoException | NfeException e) {
            throw new NfeBusinessException("Erro ao consultar a NF-e na SEFAZ: " + e.getMessage(), e);
        }
    }

    public NfeEmissionResponse consultarRecibo(String numeroRecibo) {
        try {
            ConfiguracoesNfe config = configProvider.buildConfig();
            TRetConsReciNFe retorno = libraryAdapter.consultarRecibo(config, numeroRecibo);
            return consultaResponseMapper.mapRecibo(retorno);
        } catch (CertificadoException | NfeException e) {
            throw new NfeBusinessException("Erro ao consultar o recibo na SEFAZ: " + e.getMessage(), e);
        }
    }

    private void atualizarDocumento(String chaveAcesso, NfeEmissionResponse response) {
        Optional<FiscalDocument> document = fiscalDocumentService.findByAccessKey(chaveAcesso);
        document.ifPresent(doc -> fiscalDocumentService.updateFromConsultation(doc, response.getStatus(),
                response.getStatusCode(), response.getProtocol(), response.getAuthorizedXml()));
    }

    private StatusServicoResponse mapStatusServico(TRetConsStatServ retorno) {
        return StatusServicoResponse.builder()
                .ambiente(retorno.getTpAmb())
                .statusCode(retorno.getCStat())
                .statusMessage(retorno.getXMotivo())
                .uf(retorno.getCUF())
                .dataHoraRecebimento(retorno.getDhRecbto())
                .tempoMedioResposta(retorno.getTMed())
                .dataHoraRetorno(retorno.getDhRetorno())
                .observacao(retorno.getXObs())
                .build();
    }
}
