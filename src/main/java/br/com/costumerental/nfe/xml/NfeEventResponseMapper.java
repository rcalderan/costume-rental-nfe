package br.com.costumerental.nfe.xml;

import br.com.costumerental.nfe.api.dto.ConsultaCadastroResponse;
import br.com.costumerental.nfe.api.dto.DistribuicaoDfeResponse;
import br.com.costumerental.nfe.api.dto.NfeEventResponse;
import br.com.costumerental.nfe.api.dto.NfeInutilizacaoResponse;
import br.com.costumerental.nfe.domain.NfeEventType;
import br.com.costumerental.nfe.domain.NfeStatus;
import br.com.swconsultoria.nfe.schema.envEventoCancNFe.TRetEvento;
import br.com.swconsultoria.nfe.schema.retConsCad.TRetConsCad;
import br.com.swconsultoria.nfe.schema.retdistdfeint.RetDistDFeInt;
import br.com.swconsultoria.nfe.schema_4.inutNFe.TRetInutNFe;
import org.springframework.stereotype.Component;

import java.util.Base64;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Component
public class NfeEventResponseMapper {

    private final NfeStatusCodeResolver statusCodeResolver;

    public NfeEventResponseMapper(NfeStatusCodeResolver statusCodeResolver) {
        this.statusCodeResolver = statusCodeResolver;
    }

    public NfeEventResponse mapCancelamento(br.com.swconsultoria.nfe.schema.envEventoCancNFe.TRetEnvEvento retorno) {
        TRetEvento.InfEvento inf = firstEvent(retorno.getRetEvento());
        return NfeEventResponse.builder()
                .accessKey(inf != null ? inf.getChNFe() : null)
                .eventType(NfeEventType.CANCELAMENTO)
                .sequence(inf != null ? inf.getNSeqEvento() : null)
                .protocol(inf != null ? inf.getNProt() : null)
                .status(resolveStatus(retorno.getCStat(), inf))
                .statusCode(resolveCode(retorno.getCStat(), inf))
                .statusMessage(resolveMessage(retorno.getCStat(), retorno.getXMotivo(), inf))
                .build();
    }

    public NfeEventResponse mapCartaCorrecao(br.com.swconsultoria.nfe.schema.envcce.TRetEnvEvento retorno) {
        br.com.swconsultoria.nfe.schema.envcce.TretEvento.InfEvento inf = firstCceEvent(retorno.getRetEvento());
        return NfeEventResponse.builder()
                .accessKey(inf != null ? inf.getChNFe() : null)
                .eventType(NfeEventType.CARTA_CORRECAO)
                .sequence(inf != null ? inf.getNSeqEvento() : null)
                .protocol(inf != null ? inf.getNProt() : null)
                .status(resolveStatus(retorno.getCStat(), inf))
                .statusCode(resolveCode(retorno.getCStat(), inf))
                .statusMessage(resolveMessage(retorno.getCStat(), retorno.getXMotivo(), inf))
                .build();
    }

    public NfeEventResponse mapManifestacao(br.com.swconsultoria.nfe.schema.envConfRecebto.TRetEnvEvento retorno) {
        br.com.swconsultoria.nfe.schema.envConfRecebto.TretEvento.InfEvento inf = firstManifestacaoEvent(retorno.getRetEvento());
        return NfeEventResponse.builder()
                .accessKey(inf != null ? inf.getChNFe() : null)
                .eventType(NfeEventType.MANIFESTACAO_DESTINATARIO)
                .sequence(inf != null ? inf.getNSeqEvento() : null)
                .protocol(inf != null ? inf.getNProt() : null)
                .status(resolveStatus(retorno.getCStat(), inf))
                .statusCode(resolveCode(retorno.getCStat(), inf))
                .statusMessage(resolveMessage(retorno.getCStat(), retorno.getXMotivo(), inf))
                .build();
    }

    public NfeInutilizacaoResponse mapInutilizacao(TRetInutNFe retorno) {
        TRetInutNFe.InfInut inf = retorno.getInfInut();
        NfeStatus status = inf != null ? statusCodeResolver.resolve(inf.getCStat()) : statusCodeResolver.resolve(null);
        return NfeInutilizacaoResponse.builder()
                .year(inf != null ? inf.getAno() : null)
                .cnpj(inf != null ? inf.getCNPJ() : null)
                .model(inf != null ? inf.getMod() : null)
                .series(inf != null ? inf.getSerie() : null)
                .initialNumber(inf != null ? inf.getNNFIni() : null)
                .finalNumber(inf != null ? inf.getNNFFin() : null)
                .protocol(inf != null ? inf.getNProt() : null)
                .status(status)
                .statusCode(inf != null ? inf.getCStat() : null)
                .statusMessage(inf != null ? inf.getXMotivo() : null)
                .build();
    }

    public ConsultaCadastroResponse mapConsultaCadastro(TRetConsCad retorno) {
        TRetConsCad.InfCons inf = retorno.getInfCons();
        List<ConsultaCadastroResponse.CadastroInfo> registrations = Optional.ofNullable(inf.getInfCad())
                .map(list -> list.stream()
                        .map(cad -> ConsultaCadastroResponse.CadastroInfo.builder()
                                .ie(cad.getIE())
                                .cnpj(cad.getCNPJ())
                                .cpf(cad.getCPF())
                                .razaoSocial(cad.getXNome())
                                .uf(cad.getUF() != null ? cad.getUF().value() : null)
                                .regimeApuracaoIcms(cad.getXRegApur())
                                .situacaoContribuinte(cad.getCSit())
                                .build())
                        .collect(Collectors.toList()))
                .orElse(List.of());

        return ConsultaCadastroResponse.builder()
                .statusCode(inf.getCStat())
                .statusMessage(inf.getXMotivo())
                .uf(inf.getUF() != null ? inf.getUF().value() : null)
                .ie(inf.getIE())
                .cnpj(inf.getCNPJ())
                .cpf(inf.getCPF())
                .registrations(registrations)
                .build();
    }

    public DistribuicaoDfeResponse mapDistribuicaoDfe(RetDistDFeInt retorno) {
        List<DistribuicaoDfeResponse.DocumentoZip> documents = Optional.ofNullable(retorno.getLoteDistDFeInt())
                .map(lote -> lote.getDocZip().stream()
                        .map(doc -> DistribuicaoDfeResponse.DocumentoZip.builder()
                                .nsu(doc.getNSU())
                                .schema(doc.getSchema())
                                .base64Content(Base64.getEncoder().encodeToString(doc.getValue()))
                                .build())
                        .collect(Collectors.toList()))
                .orElse(List.of());

        return DistribuicaoDfeResponse.builder()
                .statusCode(retorno.getCStat())
                .statusMessage(retorno.getXMotivo())
                .ultimoNsu(retorno.getUltNSU())
                .maximoNsu(retorno.getMaxNSU())
                .documents(documents)
                .build();
    }

    private TRetEvento.InfEvento firstEvent(List<TRetEvento> events) {
        if (events == null || events.isEmpty()) {
            return null;
        }
        return events.get(0).getInfEvento();
    }

    private br.com.swconsultoria.nfe.schema.envcce.TretEvento.InfEvento firstCceEvent(
            List<br.com.swconsultoria.nfe.schema.envcce.TretEvento> events) {
        if (events == null || events.isEmpty()) {
            return null;
        }
        return events.get(0).getInfEvento();
    }

    private br.com.swconsultoria.nfe.schema.envConfRecebto.TretEvento.InfEvento firstManifestacaoEvent(
            List<br.com.swconsultoria.nfe.schema.envConfRecebto.TretEvento> events) {
        if (events == null || events.isEmpty()) {
            return null;
        }
        return events.get(0).getInfEvento();
    }

    private NfeStatus resolveStatus(String headerStatus, Object infEvento) {
        String code = resolveCode(headerStatus, infEvento);
        return statusCodeResolver.resolve(code);
    }

    private String resolveCode(String headerStatus, Object infEvento) {
        String eventCode = extractEventCode(infEvento);
        return eventCode != null ? eventCode : headerStatus;
    }

    private String resolveMessage(String headerStatus, String headerMessage, Object infEvento) {
        String eventMessage = extractEventMessage(infEvento);
        return eventMessage != null ? eventMessage : headerMessage;
    }

    private String extractEventCode(Object infEvento) {
        if (infEvento == null) {
            return null;
        }
        if (infEvento instanceof TRetEvento.InfEvento) {
            return ((TRetEvento.InfEvento) infEvento).getCStat();
        }
        if (infEvento instanceof br.com.swconsultoria.nfe.schema.envcce.TretEvento.InfEvento) {
            return ((br.com.swconsultoria.nfe.schema.envcce.TretEvento.InfEvento) infEvento).getCStat();
        }
        if (infEvento instanceof br.com.swconsultoria.nfe.schema.envConfRecebto.TretEvento.InfEvento) {
            return ((br.com.swconsultoria.nfe.schema.envConfRecebto.TretEvento.InfEvento) infEvento).getCStat();
        }
        return null;
    }

    private String extractEventMessage(Object infEvento) {
        if (infEvento == null) {
            return null;
        }
        if (infEvento instanceof TRetEvento.InfEvento) {
            return ((TRetEvento.InfEvento) infEvento).getXMotivo();
        }
        if (infEvento instanceof br.com.swconsultoria.nfe.schema.envcce.TretEvento.InfEvento) {
            return ((br.com.swconsultoria.nfe.schema.envcce.TretEvento.InfEvento) infEvento).getXMotivo();
        }
        if (infEvento instanceof br.com.swconsultoria.nfe.schema.envConfRecebto.TretEvento.InfEvento) {
            return ((br.com.swconsultoria.nfe.schema.envConfRecebto.TretEvento.InfEvento) infEvento).getXMotivo();
        }
        return null;
    }
}
