package br.com.costumerental.nfe.application;

import br.com.costumerental.nfe.api.dto.CancelNfeRequest;
import br.com.costumerental.nfe.api.dto.ConsultaCadastroRequest;
import br.com.costumerental.nfe.api.dto.CorrectionLetterRequest;
import br.com.costumerental.nfe.api.dto.DistribuicaoDfeRequest;
import br.com.costumerental.nfe.api.dto.ManifestacaoDestinatarioRequest;
import br.com.costumerental.nfe.api.dto.NfeEventResponse;
import br.com.costumerental.nfe.api.dto.NfeInutilizacaoRequest;
import br.com.costumerental.nfe.api.dto.NfeInutilizacaoResponse;
import br.com.costumerental.nfe.api.dto.ConsultaCadastroResponse;
import br.com.costumerental.nfe.api.dto.DistribuicaoDfeResponse;
import br.com.costumerental.nfe.config.NfeProperties;
import br.com.costumerental.nfe.domain.ManifestacaoDestinatarioType;
import br.com.costumerental.nfe.domain.NfeEventType;
import br.com.costumerental.nfe.exception.NfeBusinessException;
import br.com.costumerental.nfe.infrastructure.sefaz.NfeLibraryAdapter;
import br.com.costumerental.nfe.infrastructure.sefaz.NfeSefazConfigProvider;
import br.com.costumerental.nfe.xml.NfeEventResponseMapper;
import br.com.costumerental.nfe.xml.NfeEventXmlAssembler;
import br.com.swconsultoria.certificado.exception.CertificadoException;
import br.com.swconsultoria.nfe.dom.ConfiguracoesNfe;
import br.com.swconsultoria.nfe.dom.enuns.ConsultaDFeEnum;
import br.com.swconsultoria.nfe.dom.enuns.EstadosEnum;
import br.com.swconsultoria.nfe.dom.enuns.PessoaEnum;
import br.com.swconsultoria.nfe.exception.NfeException;
import br.com.swconsultoria.nfe.schema.retdistdfeint.RetDistDFeInt;
import org.springframework.stereotype.Service;

@Service
public class NfeEventService {

    private final NfeSefazConfigProvider configProvider;
    private final NfeLibraryAdapter libraryAdapter;
    private final NfeEventXmlAssembler eventAssembler;
    private final NfeEventResponseMapper eventResponseMapper;
    private final FiscalDocumentEventService fiscalDocumentEventService;
    private final NfeProperties properties;

    public NfeEventService(NfeSefazConfigProvider configProvider,
                           NfeLibraryAdapter libraryAdapter,
                           NfeEventXmlAssembler eventAssembler,
                           NfeEventResponseMapper eventResponseMapper,
                           FiscalDocumentEventService fiscalDocumentEventService,
                           NfeProperties properties) {
        this.configProvider = configProvider;
        this.libraryAdapter = libraryAdapter;
        this.eventAssembler = eventAssembler;
        this.eventResponseMapper = eventResponseMapper;
        this.fiscalDocumentEventService = fiscalDocumentEventService;
        this.properties = properties;
    }

    public NfeEventResponse cancelar(String accessKey, CancelNfeRequest request) {
        try {
            ConfiguracoesNfe config = configProvider.buildConfig();
            br.com.swconsultoria.nfe.schema.envEventoCancNFe.TEnvEvento envEvento = eventAssembler.buildCancelamento(accessKey, request);
            String eventXml = libraryAdapter.toXml(envEvento);

            br.com.swconsultoria.nfe.schema.envEventoCancNFe.TRetEnvEvento retorno = libraryAdapter.cancelarNfe(config, envEvento);
            String responseXml = libraryAdapter.toXml(retorno);

            NfeEventResponse response = eventResponseMapper.mapCancelamento(retorno);
            response.setResponseXml(responseXml);

            fiscalDocumentEventService.saveEvent(accessKey, NfeEventType.CANCELAMENTO, request.getSequence(), eventXml, response);
            fiscalDocumentEventService.updateDocumentAfterCancellation(accessKey, response);
            return response;
        } catch (CertificadoException | NfeException e) {
            throw new NfeBusinessException("Erro ao cancelar NF-e: " + e.getMessage(), e);
        }
    }

    public NfeEventResponse cartaCorrecao(String accessKey, CorrectionLetterRequest request) {
        try {
            ConfiguracoesNfe config = configProvider.buildConfig();
            br.com.swconsultoria.nfe.schema.envcce.TEnvEvento envEvento = eventAssembler.buildCartaCorrecao(accessKey, request);
            String eventXml = libraryAdapter.toXml(envEvento);

            br.com.swconsultoria.nfe.schema.envcce.TRetEnvEvento retorno = libraryAdapter.cartaCorrecao(config, envEvento);
            String responseXml = libraryAdapter.toXml(retorno);

            NfeEventResponse response = eventResponseMapper.mapCartaCorrecao(retorno);
            response.setResponseXml(responseXml);

            fiscalDocumentEventService.saveEvent(accessKey, NfeEventType.CARTA_CORRECAO, request.getSequence(), eventXml, response);
            return response;
        } catch (CertificadoException | NfeException e) {
            throw new NfeBusinessException("Erro ao enviar carta de correcao: " + e.getMessage(), e);
        }
    }

    public NfeEventResponse manifestacaoDestinatario(String accessKey, ManifestacaoDestinatarioRequest request) {
        try {
            ManifestacaoDestinatarioType type = ManifestacaoDestinatarioType.fromCode(request.getEventCode());
            ConfiguracoesNfe config = configProvider.buildConfig();
            br.com.swconsultoria.nfe.schema.envConfRecebto.TEnvEvento envEvento = eventAssembler.buildManifestacao(accessKey, type);
            String eventXml = libraryAdapter.toXml(envEvento);

            br.com.swconsultoria.nfe.schema.envConfRecebto.TRetEnvEvento retorno = libraryAdapter.manifestacao(config, envEvento);
            String responseXml = libraryAdapter.toXml(retorno);

            NfeEventResponse response = eventResponseMapper.mapManifestacao(retorno);
            response.setResponseXml(responseXml);

            fiscalDocumentEventService.saveEvent(accessKey, NfeEventType.MANIFESTACAO_DESTINATARIO, "01", eventXml, response);
            return response;
        } catch (CertificadoException | NfeException e) {
            throw new NfeBusinessException("Erro ao enviar manifestacao do destinatario: " + e.getMessage(), e);
        }
    }

    public NfeInutilizacaoResponse inutilizar(NfeInutilizacaoRequest request) {
        try {
            ConfiguracoesNfe config = configProvider.buildConfig();
            br.com.swconsultoria.nfe.schema_4.inutNFe.TInutNFe inutNFe = eventAssembler.buildInutilizacao(request);
            String eventXml = libraryAdapter.toXml(inutNFe);

            br.com.swconsultoria.nfe.schema_4.inutNFe.TRetInutNFe retorno = libraryAdapter.inutilizacao(config, inutNFe);
            String responseXml = libraryAdapter.toXml(retorno);

            NfeInutilizacaoResponse response = eventResponseMapper.mapInutilizacao(retorno);
            response.setResponseXml(responseXml);

            fiscalDocumentEventService.saveInutilization(eventXml, request, response);
            return response;
        } catch (CertificadoException | NfeException e) {
            throw new NfeBusinessException("Erro ao inutilizar numeracao: " + e.getMessage(), e);
        }
    }

    public ConsultaCadastroResponse consultaCadastro(ConsultaCadastroRequest request) {
        try {
            ConfiguracoesNfe config = configProvider.buildConfig();
            PessoaEnum tipoPessoa = request.getDocument().length() == 11 ? PessoaEnum.FISICA : PessoaEnum.JURIDICA;
            EstadosEnum estado = EstadosEnum.valueOf(request.getUf());

            br.com.swconsultoria.nfe.schema.retConsCad.TRetConsCad retorno = libraryAdapter.consultaCadastro(config, tipoPessoa,
                    digitsOnly(request.getDocument()), estado);
            return eventResponseMapper.mapConsultaCadastro(retorno);
        } catch (CertificadoException | NfeException e) {
            throw new NfeBusinessException("Erro ao consultar cadastro: " + e.getMessage(), e);
        }
    }

    public DistribuicaoDfeResponse distribuicaoDfe(DistribuicaoDfeRequest request) {
        try {
            ConfiguracoesNfe config = configProvider.buildConfig();
            PessoaEnum tipoPessoa = request.getDocument().length() == 11 ? PessoaEnum.FISICA : PessoaEnum.JURIDICA;
            ConsultaDFeEnum tipoConsulta = ConsultaDFeEnum.valueOf(request.getQueryType());

            RetDistDFeInt retorno = libraryAdapter.distribuicaoDfe(config, tipoPessoa, digitsOnly(request.getDocument()),
                    tipoConsulta, request.getNsuOrKey());
            return eventResponseMapper.mapDistribuicaoDfe(retorno);
        } catch (CertificadoException | NfeException e) {
            throw new NfeBusinessException("Erro ao consultar distribuicao DFe: " + e.getMessage(), e);
        }
    }

    private String digitsOnly(String value) {
        return value.replaceAll("\\D", "");
    }
}
