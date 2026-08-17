package br.com.costumerental.nfe.infrastructure.sefaz;

import br.com.costumerental.nfe.config.NfeProperties;
import br.com.swconsultoria.nfe.Nfe;
import br.com.swconsultoria.nfe.dom.ConfiguracoesNfe;
import br.com.swconsultoria.nfe.dom.enuns.ConsultaDFeEnum;
import br.com.swconsultoria.nfe.dom.enuns.DocumentoEnum;
import br.com.swconsultoria.nfe.dom.enuns.EstadosEnum;
import br.com.swconsultoria.nfe.dom.enuns.PessoaEnum;
import br.com.swconsultoria.nfe.exception.NfeException;
import br.com.swconsultoria.nfe.schema.envConfRecebto.TEnvEvento;
import br.com.swconsultoria.nfe.schema.retConsCad.TRetConsCad;
import br.com.swconsultoria.nfe.schema.retdistdfeint.RetDistDFeInt;
import br.com.swconsultoria.nfe.schema_4.enviNFe.TEnviNFe;
import br.com.swconsultoria.nfe.schema_4.enviNFe.TRetEnviNFe;
import br.com.swconsultoria.nfe.schema_4.inutNFe.TInutNFe;
import br.com.swconsultoria.nfe.schema_4.inutNFe.TRetInutNFe;
import br.com.swconsultoria.nfe.schema_4.retConsReciNFe.TRetConsReciNFe;
import br.com.swconsultoria.nfe.schema_4.retConsSitNFe.TRetConsSitNFe;
import br.com.swconsultoria.nfe.schema_4.retConsStatServ.TRetConsStatServ;
import br.com.swconsultoria.nfe.util.XmlNfeUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.springframework.stereotype.Component;

/**
 * Thin wrapper around the Java_NFe library, isolating all SEFAZ calls
 * and enriching each invocation with MDC context for structured logging.
 *
 * Usage: inject this component wherever you need to call the SEFAZ.
 */
@Component
public class NfeLibraryAdapter {

    private static final Logger log = LoggerFactory.getLogger(NfeLibraryAdapter.class);

    private final NfeProperties properties;

    public NfeLibraryAdapter(NfeProperties properties) {
        this.properties = properties;
    }

    public TEnviNFe signAndValidate(ConfiguracoesNfe config, TEnviNFe enviNFe) throws NfeException {
        return withMdc("signAndValidate", null, () -> Nfe.montaNfe(config, enviNFe, true));
    }

    public TRetEnviNFe send(ConfiguracoesNfe config, TEnviNFe signedEnviNFe) throws NfeException {
        String chave = extractAccessKey(signedEnviNFe);
        DocumentoEnum tipoDocumento = DocumentoEnumResolver.fromAccessKey(chave);
        TRetEnviNFe ret = withMdc("enviarNfe", chave, () -> Nfe.enviarNfe(config, signedEnviNFe, tipoDocumento));
        logSefazResponse(ret.getCStat(), ret.getXMotivo());
        return ret;
    }

    public TRetConsStatServ statusServico(ConfiguracoesNfe config) throws NfeException {
        DocumentoEnum tipoDocumento = configuredDocumentType();
        TRetConsStatServ ret = withMdc("statusServico", null, () -> Nfe.statusServico(config, tipoDocumento));
        logSefazResponse(ret.getCStat(), ret.getXMotivo());
        return ret;
    }

    public TRetConsSitNFe consultarXml(ConfiguracoesNfe config, String chaveAcesso) throws NfeException {
        DocumentoEnum tipoDocumento = DocumentoEnumResolver.fromAccessKey(chaveAcesso);
        TRetConsSitNFe ret = withMdc("consultarXml", chaveAcesso, () -> Nfe.consultaXml(config, chaveAcesso, tipoDocumento));
        logSefazResponse(ret.getCStat(), ret.getXMotivo());
        return ret;
    }

    public TRetConsReciNFe consultarRecibo(ConfiguracoesNfe config, String numeroRecibo) throws NfeException {
        DocumentoEnum tipoDocumento = configuredDocumentType();
        return withMdc("consultarRecibo", null, () -> Nfe.consultaRecibo(config, numeroRecibo, tipoDocumento));
    }

    public br.com.swconsultoria.nfe.schema.envEventoCancNFe.TRetEnvEvento cancelarNfe(ConfiguracoesNfe config,
                                                                                     br.com.swconsultoria.nfe.schema.envEventoCancNFe.TEnvEvento envEvento) throws NfeException {
        DocumentoEnum tipoDocumento = DocumentoEnumResolver.fromAccessKey(chaveFrom(envEvento));
        return withMdc("cancelarNfe", null, () -> Nfe.cancelarNfe(config, envEvento, true, tipoDocumento));
    }

    public br.com.swconsultoria.nfe.schema.envcce.TRetEnvEvento cartaCorrecao(ConfiguracoesNfe config,
                                                                               br.com.swconsultoria.nfe.schema.envcce.TEnvEvento envEvento) throws NfeException {
        return withMdc("cartaCorrecao", null, () -> Nfe.cce(config, envEvento, true));
    }

    public br.com.swconsultoria.nfe.schema.envConfRecebto.TRetEnvEvento manifestacao(ConfiguracoesNfe config,
                                                                                      TEnvEvento envEvento) throws NfeException {
        return withMdc("manifestacao", null, () -> Nfe.manifestacao(config, envEvento, true));
    }

    public TRetInutNFe inutilizacao(ConfiguracoesNfe config, TInutNFe inutNFe) throws NfeException {
        DocumentoEnum tipoDocumento = DocumentoEnumResolver.fromModelo(inutNFe.getInfInut().getMod());
        return withMdc("inutilizacao", null, () -> Nfe.inutilizacao(config, inutNFe, tipoDocumento, true));
    }

    public TRetConsCad consultaCadastro(ConfiguracoesNfe config, PessoaEnum tipoPessoa, String cnpjCpf,
                                        EstadosEnum estado) throws NfeException {
        return withMdc("consultaCadastro", null, () -> Nfe.consultaCadastro(config, tipoPessoa, cnpjCpf, estado));
    }

    public RetDistDFeInt distribuicaoDfe(ConfiguracoesNfe config, PessoaEnum tipoPessoa, String cpfCnpj,
                                           ConsultaDFeEnum tipoConsulta, String nsuChave) throws NfeException {
        return withMdc("distribuicaoDfe", null, () -> Nfe.distribuicaoDfe(config, tipoPessoa, cpfCnpj, tipoConsulta, nsuChave));
    }

    public String buildNfeProc(TEnviNFe signedEnviNFe, Object retorno) throws NfeException {
        try {
            return XmlNfeUtil.criaNfeProc(signedEnviNFe, retorno);
        } catch (javax.xml.bind.JAXBException e) {
            throw new NfeException("Falha ao montar o XML autorizado (nfeProc)", e);
        }
    }

    public String toXml(Object jaxbObject) throws NfeException {
        try {
            return XmlNfeUtil.objectToXml(jaxbObject);
        } catch (javax.xml.bind.JAXBException e) {
            throw new NfeException("Falha ao serializar XML", e);
        }
    }

    // -- MDC helpers ----------------------------------------------------------

    /**
     * Wraps a SEFAZ call with MDC context (operacao + chaveAcesso),
     * ensuring cleanup even if the call throws.
     */
    private <T> T withMdc(String operacao, String chaveAcesso, SefazCall<T> call) throws NfeException {
        MDC.put("operacao", operacao);
        if (chaveAcesso != null) {
            MDC.put("chaveAcesso", chaveAcesso);
        }
        log.info("SEFAZ call started");
        try {
            return call.execute();
        } finally {
            MDC.remove("operacao");
            MDC.remove("chaveAcesso");
            MDC.remove("cStat");
            MDC.remove("xMotivo");
        }
    }

    private void logSefazResponse(String cStat, String xMotivo) {
        if (cStat != null) MDC.put("cStat", cStat);
        if (xMotivo != null) MDC.put("xMotivo", xMotivo);
        log.info("SEFAZ response received");
    }

    private String extractAccessKey(TEnviNFe enviNFe) {
        try {
            String id = enviNFe.getNFe().get(0).getInfNFe().getId();
            return id.startsWith("NFe") ? id.substring(3) : id;
        } catch (Exception e) {
            return null;
        }
    }

    private String chaveFrom(br.com.swconsultoria.nfe.schema.envEventoCancNFe.TEnvEvento envEvento) {
        return envEvento.getEvento().get(0).getInfEvento().getChNFe();
    }

    private DocumentoEnum configuredDocumentType() {
        return DocumentoEnumResolver.fromModelo(properties.getModelo());
    }

    @FunctionalInterface
    private interface SefazCall<T> {
        T execute() throws NfeException;
    }
}
