package br.com.costumerental.nfe.xml;

import br.com.costumerental.nfe.api.dto.CancelNfeRequest;
import br.com.costumerental.nfe.api.dto.CorrectionLetterRequest;
import br.com.costumerental.nfe.api.dto.NfeInutilizacaoRequest;
import br.com.costumerental.nfe.config.NfeProperties;
import br.com.costumerental.nfe.domain.Environment;
import br.com.costumerental.nfe.domain.ManifestacaoDestinatarioType;
import br.com.swconsultoria.nfe.schema.envEventoCancNFe.TEvento;
import br.com.swconsultoria.nfe.schema.envEventoCancNFe.TEnvEvento;
import br.com.swconsultoria.nfe.schema_4.inutNFe.TInutNFe;
import org.springframework.stereotype.Component;

import java.time.OffsetDateTime;
import java.time.ZoneId;

@Component
public class NfeEventXmlAssembler {

    private static final String EVENT_VERSION = "1.00";
    private static final String CCE_USAGE_TERMS = "A Carta de Correcao e disciplinada pelo paragrafo 1o-A do art. 7o do Convenio S/N, de 15 de dezembro de 1970 e pode ser utilizada para regularizar erro ocorrido na emissao de documento fiscal, desde que o erro nao esteja relacionado com: I - as variaveis que determinam o valor do imposto tais como: base de calculo, aliquota, diferenca de preco, quantidade, valor da operacao ou da prestacao; II - a correcao de dados cadastrais que implique mudanca do remetente ou do destinatario; III - a data de emissao ou de saida.";

    private final NfeProperties properties;

    public NfeEventXmlAssembler(NfeProperties properties) {
        this.properties = properties;
    }

    public TEnvEvento buildCancelamento(String accessKey, CancelNfeRequest request) {
        TEnvEvento envEvento = createEnvEvento();
        br.com.swconsultoria.nfe.schema.envEventoCancNFe.TEvento evento = new br.com.swconsultoria.nfe.schema.envEventoCancNFe.TEvento();
        evento.setVersao(EVENT_VERSION);

        br.com.swconsultoria.nfe.schema.envEventoCancNFe.TEvento.InfEvento inf = new br.com.swconsultoria.nfe.schema.envEventoCancNFe.TEvento.InfEvento();
        inf.setCOrgao(ufCodeFromAccessKey(accessKey));
        inf.setTpAmb(environmentCode());
        inf.setCNPJ(digitsOnly(properties.getEmit().getCnpj()));
        inf.setChNFe(accessKey);
        inf.setDhEvento(currentFormattedDateTime());
        inf.setTpEvento("110111");
        String eventSequence = normalizeEventSequence(request.getSequence());
        inf.setNSeqEvento(eventSequence);
        inf.setVerEvento(EVENT_VERSION);

        br.com.swconsultoria.nfe.schema.envEventoCancNFe.TEvento.InfEvento.DetEvento det = new br.com.swconsultoria.nfe.schema.envEventoCancNFe.TEvento.InfEvento.DetEvento();
        det.setDescEvento("Cancelamento");
        det.setNProt(request.getProtocol());
        det.setXJust(request.getJustification());
        det.setVersao(EVENT_VERSION);
        inf.setDetEvento(det);

        inf.setId(buildEventId("110111", accessKey, eventSequence));
        evento.setInfEvento(inf);
        envEvento.getEvento().add(evento);
        return envEvento;
    }

    public br.com.swconsultoria.nfe.schema.envcce.TEnvEvento buildCartaCorrecao(String accessKey, CorrectionLetterRequest request) {
        br.com.swconsultoria.nfe.schema.envcce.TEnvEvento envEvento = new br.com.swconsultoria.nfe.schema.envcce.TEnvEvento();
        envEvento.setIdLote("1");
        envEvento.setVersao(EVENT_VERSION);

        br.com.swconsultoria.nfe.schema.envcce.TEvento evento = new br.com.swconsultoria.nfe.schema.envcce.TEvento();
        evento.setVersao(EVENT_VERSION);

        br.com.swconsultoria.nfe.schema.envcce.TEvento.InfEvento inf = new br.com.swconsultoria.nfe.schema.envcce.TEvento.InfEvento();
        inf.setCOrgao(ufCodeFromAccessKey(accessKey));
        inf.setTpAmb(environmentCode());
        inf.setCNPJ(digitsOnly(properties.getEmit().getCnpj()));
        inf.setChNFe(accessKey);
        inf.setDhEvento(currentFormattedDateTime());
        inf.setTpEvento("110110");
        String eventSequence = normalizeEventSequence(request.getSequence());
        inf.setNSeqEvento(eventSequence);
        inf.setVerEvento(EVENT_VERSION);

        br.com.swconsultoria.nfe.schema.envcce.TEvento.InfEvento.DetEvento det = new br.com.swconsultoria.nfe.schema.envcce.TEvento.InfEvento.DetEvento();
        det.setDescEvento("Carta de Correcao");
        det.setXCorrecao(request.getCorrection());
        det.setXCondUso(CCE_USAGE_TERMS);
        det.setVersao(EVENT_VERSION);
        inf.setDetEvento(det);

        inf.setId(buildEventId("110110", accessKey, eventSequence));
        evento.setInfEvento(inf);
        envEvento.getEvento().add(evento);
        return envEvento;
    }

    public br.com.swconsultoria.nfe.schema.envConfRecebto.TEnvEvento buildManifestacao(String accessKey, ManifestacaoDestinatarioType type) {
        br.com.swconsultoria.nfe.schema.envConfRecebto.TEnvEvento envEvento = new br.com.swconsultoria.nfe.schema.envConfRecebto.TEnvEvento();
        envEvento.setIdLote("1");
        envEvento.setVersao(EVENT_VERSION);

        br.com.swconsultoria.nfe.schema.envConfRecebto.TEvento evento = new br.com.swconsultoria.nfe.schema.envConfRecebto.TEvento();
        evento.setVersao(EVENT_VERSION);

        br.com.swconsultoria.nfe.schema.envConfRecebto.TEvento.InfEvento inf = new br.com.swconsultoria.nfe.schema.envConfRecebto.TEvento.InfEvento();
        inf.setCOrgao("91");
        inf.setTpAmb(environmentCode());
        inf.setCNPJ(digitsOnly(properties.getEmit().getCnpj()));
        inf.setChNFe(accessKey);
        inf.setDhEvento(currentFormattedDateTime());
        inf.setTpEvento(type.getEventCode());
        String eventSequence = "1";
        inf.setNSeqEvento(eventSequence);
        inf.setVerEvento(EVENT_VERSION);

        br.com.swconsultoria.nfe.schema.envConfRecebto.TEvento.InfEvento.DetEvento det = new br.com.swconsultoria.nfe.schema.envConfRecebto.TEvento.InfEvento.DetEvento();
        det.setDescEvento(type.getDescription());
        det.setVersao(EVENT_VERSION);
        inf.setDetEvento(det);

        inf.setId(buildEventId(type.getEventCode(), accessKey, eventSequence));
        evento.setInfEvento(inf);
        envEvento.getEvento().add(evento);
        return envEvento;
    }

    public TInutNFe buildInutilizacao(NfeInutilizacaoRequest request) {
        TInutNFe inutNFe = new TInutNFe();
        inutNFe.setVersao("4.00");

        TInutNFe.InfInut inf = new TInutNFe.InfInut();
        inf.setTpAmb(environmentCode());
        inf.setXServ("INUTILIZAR");
        inf.setCUF(UfMapper.codeFor(properties.getEmit().getUf()));
        inf.setAno(request.getYear());
        inf.setCNPJ(digitsOnly(request.getCnpj()));
        inf.setMod(request.getModel());
        inf.setSerie(request.getSeries());
        inf.setNNFIni(leftPad(request.getInitialNumber(), 9, '0'));
        inf.setNNFFin(leftPad(request.getFinalNumber(), 9, '0'));
        inf.setXJust(request.getJustification());
        inf.setId(buildInutilizacaoId(inf));

        inutNFe.setInfInut(inf);
        return inutNFe;
    }

    private TEnvEvento createEnvEvento() {
        TEnvEvento envEvento = new TEnvEvento();
        envEvento.setIdLote("1");
        envEvento.setVersao(EVENT_VERSION);
        return envEvento;
    }

    private String environmentCode() {
        return Environment.fromCode(properties.getAmbiente()).getCode();
    }

    private String ufCodeFromAccessKey(String accessKey) {
        if (accessKey == null || accessKey.length() < 2) {
            throw new IllegalArgumentException("Chave de acesso invalida para extrair UF: " + accessKey);
        }
        return accessKey.substring(0, 2);
    }

    private String currentFormattedDateTime() {
        return NfeXmlAssembler.ISO_FMT.format(OffsetDateTime.now(ZoneId.of("America/Sao_Paulo")));
    }

    private String normalizeEventSequence(String sequence) {
        String digits = digitsOnly(sequence != null ? sequence : "1");
        if (digits.isEmpty()) {
            return "1";
        }
        return String.valueOf(Integer.parseInt(digits));
    }

    private String buildEventId(String eventCode, String accessKey, String sequence) {
        return "ID" + eventCode + accessKey + leftPad(sequence, 2, '0');
    }

    private String buildInutilizacaoId(TInutNFe.InfInut inf) {
        return "ID"
                + inf.getCUF()
                + inf.getAno()
                + inf.getCNPJ()
                + inf.getMod()
                + leftPad(inf.getSerie(), 3, '0')
                + inf.getNNFIni()
                + inf.getNNFFin();
    }

    private String digitsOnly(String value) {
        return value.replaceAll("\\D", "");
    }

    private String leftPad(String value, int length, char pad) {
        StringBuilder sb = new StringBuilder();
        for (int i = value.length(); i < length; i++) {
            sb.append(pad);
        }
        sb.append(value);
        return sb.toString();
    }
}
