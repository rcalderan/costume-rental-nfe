package br.com.costumerental.nfe.xml;

import br.com.costumerental.nfe.api.dto.CustomerInfo;
import br.com.costumerental.nfe.api.dto.NfeEmissionRequest;
import br.com.costumerental.nfe.api.dto.NfeItemRequest;
import br.com.costumerental.nfe.config.NfeProperties;
import br.com.swconsultoria.nfe.schema_4.enviNFe.TEnviNFe;
import br.com.swconsultoria.nfe.util.XmlNfeUtil;
import org.springframework.stereotype.Component;

import static org.springframework.util.StringUtils.hasText;

import javax.xml.bind.JAXBException;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.regex.Pattern;

@Component
public class NfeXmlAssembler {

    private static final String NFE_NS = "http://www.portalfiscal.inf.br/nfe";
    static final DateTimeFormatter ISO_FMT = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ssXXX");
    private static final Pattern LONG_DASHES = Pattern.compile("[\\u2011\\u2013\\u2014]");
    private static final Pattern OUTSIDE_NFE_RANGE = Pattern.compile("[^\\x20-\\xFF]");
    private static final String XNOME_DEST_HOMOLOGACAO = "NF-E EMITIDA EM AMBIENTE DE HOMOLOGACAO - SEM VALOR FISCAL";

    private final NfeProperties properties;
    private final AccessKeyGenerator accessKeyGenerator;
    private final IbgeCityCodeResolver cityCodeResolver;

    public NfeXmlAssembler(NfeProperties properties, AccessKeyGenerator accessKeyGenerator, IbgeCityCodeResolver cityCodeResolver) {
        this.properties = properties;
        this.accessKeyGenerator = accessKeyGenerator;
        this.cityCodeResolver = cityCodeResolver;
    }

    public TEnviNFe build(NfeEmissionRequest request) {
        String xml = buildXmlString(request);
        try {
            return XmlNfeUtil.xmlToObject(xml, TEnviNFe.class);
        } catch (JAXBException e) {
            throw new IllegalStateException("Falha ao converter XML da NF-e para objeto: " + e.getMessage(), e);
        }
    }

    String buildXmlString(NfeEmissionRequest request) {
        OffsetDateTime issueDate = OffsetDateTime.now(ZoneId.of("America/Sao_Paulo"));
        String invoiceNumber = accessKeyGenerator.generateInvoiceNumber();
        String cnf = accessKeyGenerator.generateCNF();
        String accessKey = accessKeyGenerator.generate(issueDate, properties.getSerie(), invoiceNumber, cnf);
        String cDV = accessKey.substring(accessKey.length() - 1);

        StringBuilder sb = new StringBuilder();
        sb.append("<?xml version=\"1.0\" encoding=\"UTF-8\"?>");
        sb.append("<enviNFe xmlns=\"").append(NFE_NS).append("\" versao=\"4.00\">");
        sb.append("<idLote>1</idLote>");
        sb.append("<indSinc>1</indSinc>");
        sb.append("<NFe>");
        sb.append("<infNFe Id=\"NFe").append(accessKey).append("\" versao=\"4.00\">");
        sb.append(buildIde(issueDate, invoiceNumber, cnf, cDV, request));
        sb.append(buildEmit());
        sb.append(buildDest(request.getCustomer()));

        int itemNumber = 1;
        for (NfeItemRequest item : request.getItems()) {
            sb.append(buildDet(item, itemNumber++));
        }

        sb.append(buildTotal(request.getItems()));
        sb.append("<transp><modFrete>9</modFrete></transp>");
        sb.append(buildCobr(invoiceNumber, request.getItems()));
        sb.append(buildPag(request.getItems()));
        sb.append("</infNFe>");
        sb.append("</NFe>");
        sb.append("</enviNFe>");
        return sb.toString();
    }

    private String buildIde(OffsetDateTime issueDate, String nNF, String cNF, String cDV, NfeEmissionRequest request) {
        StringBuilder sb = new StringBuilder();
        sb.append("<ide>");
        sb.append("<cUF>").append(UfMapper.codeFor(properties.getEmit().getUf())).append("</cUF>");
        sb.append("<cNF>").append(cNF).append("</cNF>");
        sb.append("<natOp>").append(escape(request.getNatureOperation())).append("</natOp>");
        sb.append("<mod>55</mod>");
        sb.append("<serie>").append(escape(properties.getSerie())).append("</serie>");
        sb.append("<nNF>").append(nNF).append("</nNF>");
        sb.append("<dhEmi>").append(ISO_FMT.format(issueDate)).append("</dhEmi>");
        sb.append("<tpNF>1</tpNF>");
        sb.append("<idDest>1</idDest>");
        sb.append("<cMunFG>").append(escape(properties.getEmit().getEndereco().getMunicipioCodigo())).append("</cMunFG>");
        sb.append("<tpImp>1</tpImp>");
        sb.append("<tpEmis>1</tpEmis>");
        sb.append("<cDV>").append(cDV).append("</cDV>");
        sb.append("<tpAmb>").append(properties.getAmbiente()).append("</tpAmb>");
        sb.append("<finNFe>1</finNFe>");
        sb.append("<indFinal>1</indFinal>");
        sb.append("<indPres>0</indPres>");
        sb.append("<procEmi>0</procEmi>");
        sb.append("<verProc>").append(escape(properties.getProcessoVersao())).append("</verProc>");
        sb.append("</ide>");
        return sb.toString();
    }

    private String buildEmit() {
        NfeProperties.EmitProperties emit = properties.getEmit();
        NfeProperties.EnderecoProperties end = emit.getEndereco();
        StringBuilder sb = new StringBuilder();
        sb.append("<emit>");
        sb.append("<CNPJ>").append(digitsOnly(emit.getCnpj())).append("</CNPJ>");
        sb.append("<xNome>").append(escape(emit.getRazaoSocial())).append("</xNome>");
        if (emit.getNomeFantasia() != null && !emit.getNomeFantasia().isBlank()) {
            sb.append("<xFant>").append(escape(emit.getNomeFantasia())).append("</xFant>");
        }
        sb.append("<enderEmit>");
        sb.append("<xLgr>").append(escape(end.getLogradouro())).append("</xLgr>");
        sb.append("<nro>").append(escape(end.getNumero())).append("</nro>");
        sb.append("<xBairro>").append(escape(end.getBairro())).append("</xBairro>");
        sb.append("<cMun>").append(escape(end.getMunicipioCodigo())).append("</cMun>");
        sb.append("<xMun>").append(escape(end.getMunicipioNome())).append("</xMun>");
        sb.append("<UF>").append(escape(end.getUf())).append("</UF>");
        sb.append("<CEP>").append(digitsOnly(end.getCep())).append("</CEP>");
        sb.append("<cPais>").append(escape(end.getPaisCodigo())).append("</cPais>");
        sb.append("<xPais>").append(escape(end.getPaisNome())).append("</xPais>");
        if (emit.getFone() != null && !emit.getFone().isBlank()) {
            sb.append("<fone>").append(digitsOnly(emit.getFone())).append("</fone>");
        }
        sb.append("</enderEmit>");
        String ie = emit.getIe() != null && !emit.getIe().isBlank() ? emit.getIe() : "ISENTO";
        sb.append("<IE>").append(escape(ie)).append("</IE>");
        sb.append("<CRT>").append(escape(emit.getCrt())).append("</CRT>");
        sb.append("</emit>");
        return sb.toString();
    }

    private String buildDest(CustomerInfo customer) {
        String cityCode = hasText(customer.getCityCode())
                ? customer.getCityCode()
                : cityCodeResolver.resolve(customer.getCityName(), customer.getState());
        if (!hasText(cityCode)) {
            throw new IllegalArgumentException("Codigo IBGE do municipio do destinatario nao pode ser vazio");
        }
        StringBuilder sb = new StringBuilder();
        sb.append("<dest>");
        String doc = digitsOnly(customer.getDocument());
        if (doc.length() == 14) {
            sb.append("<CNPJ>").append(doc).append("</CNPJ>");
        } else if (doc.length() == 11) {
            sb.append("<CPF>").append(doc).append("</CPF>");
        } else {
            sb.append("<idEstrangeiro>").append(escape(customer.getDocument())).append("</idEstrangeiro>");
        }
        String xNome = "2".equals(properties.getAmbiente()) ? XNOME_DEST_HOMOLOGACAO : escape(customer.getName());
        sb.append("<xNome>").append(xNome).append("</xNome>");
        sb.append("<enderDest>");
        sb.append("<xLgr>").append(escape(customer.getStreet())).append("</xLgr>");
        sb.append("<nro>").append(escape(customer.getNumber())).append("</nro>");
        sb.append("<xBairro>").append(escape(customer.getNeighborhood())).append("</xBairro>");
        sb.append("<cMun>").append(escape(cityCode)).append("</cMun>");
        sb.append("<xMun>").append(escape(customer.getCityName())).append("</xMun>");
        sb.append("<UF>").append(escape(customer.getState())).append("</UF>");
        sb.append("<CEP>").append(digitsOnly(customer.getZipCode())).append("</CEP>");
        sb.append("<cPais>1058</cPais>");
        sb.append("<xPais>BRASIL</xPais>");
        if (customer.getPhone() != null && !customer.getPhone().isBlank()) {
            sb.append("<fone>").append(digitsOnly(customer.getPhone())).append("</fone>");
        }
        sb.append("</enderDest>");
        sb.append("<indIEDest>9</indIEDest>");
        sb.append("</dest>");
        return sb.toString();
    }

    private String buildDet(NfeItemRequest item, int itemNumber) {
        BigDecimal qCom = round(item.getQuantity(), 4);
        BigDecimal vUnCom = round(item.getUnitValue(), 10);
        BigDecimal vProd = round(qCom.multiply(vUnCom), 2);

        StringBuilder sb = new StringBuilder();
        sb.append("<det nItem=\"").append(itemNumber).append("\">");
        sb.append("<prod>");
        sb.append("<cProd>").append(escape(item.getProductCode())).append("</cProd>");
        sb.append("<cEAN>SEM GTIN</cEAN>");
        sb.append("<xProd>").append(escape(item.getDescription())).append("</xProd>");
        sb.append("<NCM>").append(escape(item.getNcm())).append("</NCM>");
        sb.append("<CFOP>").append(escape(item.getCfop())).append("</CFOP>");
        sb.append("<uCom>").append(escape(item.getUnit())).append("</uCom>");
        sb.append("<qCom>").append(formatDecimal(qCom, 4)).append("</qCom>");
        sb.append("<vUnCom>").append(formatDecimal(vUnCom, 10)).append("</vUnCom>");
        sb.append("<vProd>").append(formatDecimal(vProd)).append("</vProd>");
        sb.append("<cEANTrib>SEM GTIN</cEANTrib>");
        sb.append("<uTrib>").append(escape(item.getUnit())).append("</uTrib>");
        sb.append("<qTrib>").append(formatDecimal(qCom, 4)).append("</qTrib>");
        sb.append("<vUnTrib>").append(formatDecimal(vUnCom, 10)).append("</vUnTrib>");
        sb.append("<indTot>1</indTot>");
        sb.append("</prod>");
        sb.append("<imposto>");
        sb.append("<vTotTrib>0.00</vTotTrib>");
        sb.append(buildIcms());
        sb.append(buildPis());
        sb.append(buildCofins());
        sb.append("</imposto>");
        sb.append("</det>");
        return sb.toString();
    }

    private String buildIcms() {
        StringBuilder sb = new StringBuilder();
        sb.append("<ICMS>");
        if (isRegimeNormal()) {
            sb.append("<ICMS40>");
            sb.append("<orig>0</orig>");
            sb.append("<CST>41</CST>");
            sb.append("</ICMS40>");
        } else {
            sb.append("<ICMSSN102>");
            sb.append("<orig>0</orig>");
            sb.append("<CSOSN>103</CSOSN>");
            sb.append("</ICMSSN102>");
        }
        sb.append("</ICMS>");
        return sb.toString();
    }

    private String buildPis() {
        StringBuilder sb = new StringBuilder();
        sb.append("<PIS>");
        if (isRegimeNormal()) {
            sb.append("<PISOutr>");
            sb.append("<CST>49</CST>");
            sb.append("<vBC>0.00</vBC>");
            sb.append("<pPIS>0.00</pPIS>");
            sb.append("<vPIS>0.00</vPIS>");
            sb.append("</PISOutr>");
        } else {
            sb.append("<PISNT>");
            sb.append("<CST>07</CST>");
            sb.append("</PISNT>");
        }
        sb.append("</PIS>");
        return sb.toString();
    }

    private String buildCofins() {
        StringBuilder sb = new StringBuilder();
        sb.append("<COFINS>");
        if (isRegimeNormal()) {
            sb.append("<COFINSOutr>");
            sb.append("<CST>49</CST>");
            sb.append("<vBC>0.00</vBC>");
            sb.append("<pCOFINS>0.00</pCOFINS>");
            sb.append("<vCOFINS>0.00</vCOFINS>");
            sb.append("</COFINSOutr>");
        } else {
            sb.append("<COFINSNT>");
            sb.append("<CST>07</CST>");
            sb.append("</COFINSNT>");
        }
        sb.append("</COFINS>");
        return sb.toString();
    }

    private boolean isRegimeNormal() {
        return "3".equals(properties.getEmit().getCrt());
    }

    private String buildTotal(List<NfeItemRequest> items) {
        BigDecimal vProd = totalProducts(items);
        BigDecimal vNF = vProd;

        StringBuilder sb = new StringBuilder();
        sb.append("<total>");
        sb.append("<ICMSTot>");
        sb.append("<vBC>0.00</vBC>");
        sb.append("<vICMS>0.00</vICMS>");
        sb.append("<vICMSDeson>0.00</vICMSDeson>");
        sb.append("<vFCP>0.00</vFCP>");
        sb.append("<vBCST>0.00</vBCST>");
        sb.append("<vST>0.00</vST>");
        sb.append("<vFCPST>0.00</vFCPST>");
        sb.append("<vFCPSTRet>0.00</vFCPSTRet>");
        sb.append("<vProd>").append(formatDecimal(vProd)).append("</vProd>");
        sb.append("<vFrete>0.00</vFrete>");
        sb.append("<vSeg>0.00</vSeg>");
        sb.append("<vDesc>0.00</vDesc>");
        sb.append("<vII>0.00</vII>");
        sb.append("<vIPI>0.00</vIPI>");
        sb.append("<vIPIDevol>0.00</vIPIDevol>");
        sb.append("<vPIS>0.00</vPIS>");
        sb.append("<vCOFINS>0.00</vCOFINS>");
        sb.append("<vOutro>0.00</vOutro>");
        sb.append("<vNF>").append(formatDecimal(vNF)).append("</vNF>");
        sb.append("<vTotTrib>0.00</vTotTrib>");
        sb.append("</ICMSTot>");
        sb.append("</total>");
        return sb.toString();
    }

    private String buildCobr(String nNF, List<NfeItemRequest> items) {
        BigDecimal vNF = totalProducts(items);
        StringBuilder sb = new StringBuilder();
        sb.append("<cobr>");
        sb.append("<fat>");
        sb.append("<nFat>").append(nNF).append("</nFat>");
        sb.append("<vOrig>").append(formatDecimal(vNF)).append("</vOrig>");
        sb.append("<vLiq>").append(formatDecimal(vNF)).append("</vLiq>");
        sb.append("</fat>");
        sb.append("</cobr>");
        return sb.toString();
    }

    private String buildPag(List<NfeItemRequest> items) {
        BigDecimal vPag = totalProducts(items);
        StringBuilder sb = new StringBuilder();
        sb.append("<pag>");
        sb.append("<detPag>");
        sb.append("<tPag>01</tPag>");
        sb.append("<vPag>").append(formatDecimal(vPag)).append("</vPag>");
        sb.append("</detPag>");
        sb.append("</pag>");
        return sb.toString();
    }

    private BigDecimal totalProducts(List<NfeItemRequest> items) {
        return items.stream()
                .map(i -> i.getQuantity().multiply(i.getUnitValue()))
                .reduce(BigDecimal.ZERO, BigDecimal::add)
                .setScale(2, RoundingMode.HALF_UP);
    }

    private static String escape(String value) {
        String sanitized = value == null ? "" : LONG_DASHES.matcher(value).replaceAll("-");
        sanitized = OUTSIDE_NFE_RANGE.matcher(sanitized).replaceAll("");
        return sanitized.replace("&", "&amp;")
                .replace("<", "&lt;")
                .replace(">", "&gt;")
                .replace("\"", "&quot;")
                .replace("'", "&apos;");
    }

    private static String digitsOnly(String value) {
        if (value == null) {
            return "";
        }
        return value.replaceAll("\\D", "");
    }

    private static String formatDecimal(BigDecimal value) {
        return value.setScale(2, RoundingMode.HALF_UP).toPlainString();
    }

    private static String formatDecimal(BigDecimal value, int scale) {
        return value.setScale(scale, RoundingMode.HALF_UP).toPlainString();
    }

    private static BigDecimal round(BigDecimal value, int scale) {
        return value.setScale(scale, RoundingMode.HALF_UP);
    }
}
