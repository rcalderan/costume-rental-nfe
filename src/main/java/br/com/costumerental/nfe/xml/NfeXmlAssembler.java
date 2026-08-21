package br.com.costumerental.nfe.xml;

import br.com.costumerental.nfe.api.dto.CustomerInfo;
import br.com.costumerental.nfe.api.dto.NfeEmissionRequest;
import br.com.costumerental.nfe.api.dto.NfeItemRequest;
import br.com.costumerental.nfe.api.dto.PaymentInfo;
import br.com.costumerental.nfe.config.NfeProperties;
import br.com.costumerental.nfe.domain.NfeIssuer;
import br.com.costumerental.nfe.exception.NfeBusinessException;
import br.com.swconsultoria.nfe.schema_4.enviNFe.TEnviNFe;
import br.com.swconsultoria.nfe.util.NFCeUtil;
import br.com.swconsultoria.nfe.util.XmlNfeUtil;
import org.springframework.stereotype.Component;

import static org.springframework.util.StringUtils.hasText;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Set;
import java.util.regex.Pattern;

@Component
public class NfeXmlAssembler {

    private static final String NFE_NS = "http://www.portalfiscal.inf.br/nfe";
    static final DateTimeFormatter ISO_FMT = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ssXXX");
    private static final Pattern LONG_DASHES = Pattern.compile("[\\u2011\\u2013\\u2014]");
    private static final Pattern OUTSIDE_NFE_RANGE = Pattern.compile("[^\\x20-\\xFF]");
    private static final String XNOME_DEST_HOMOLOGACAO = "NF-E EMITIDA EM AMBIENTE DE HOMOLOGACAO - SEM VALOR FISCAL";
    private static final String XPROD_HOMOLOGACAO = "NOTA FISCAL EMITIDA EM AMBIENTE DE HOMOLOGACAO - SEM VALOR FISCAL";

    private final NfeProperties properties;
    private final AccessKeyGenerator accessKeyGenerator;
    private final IbgeCityCodeResolver cityCodeResolver;

    public NfeXmlAssembler(NfeProperties properties, AccessKeyGenerator accessKeyGenerator, IbgeCityCodeResolver cityCodeResolver) {
        this.properties = properties;
        this.accessKeyGenerator = accessKeyGenerator;
        this.cityCodeResolver = cityCodeResolver;
    }

    public TEnviNFe build(NfeEmissionRequest request, NfeIssuer issuer) {
        String xml = buildXmlString(request, issuer);
        return XmlNfeUtil.xmlToObject(xml, TEnviNFe.class);
    }

    String buildXmlString(NfeEmissionRequest request, NfeIssuer issuer) {
        OffsetDateTime issueDate = OffsetDateTime.now(ZoneId.of("America/Sao_Paulo"));
        String invoiceNumber = accessKeyGenerator.generateInvoiceNumber();
        String cnf = accessKeyGenerator.generateCNF();
        String modelo = resolveModelo(request);
        String accessKey = accessKeyGenerator.generate(issueDate, issuer, modelo, properties.getSerie(), invoiceNumber, cnf);
        String cDV = accessKey.substring(accessKey.length() - 1);

        StringBuilder sb = new StringBuilder();
        sb.append("<?xml version=\"1.0\" encoding=\"UTF-8\"?>");
        sb.append("<enviNFe xmlns=\"").append(NFE_NS).append("\" versao=\"4.00\">");
        sb.append("<idLote>1</idLote>");
        sb.append("<indSinc>1</indSinc>");
        sb.append("<NFe>");
        sb.append("<infNFe Id=\"NFe").append(accessKey).append("\" versao=\"4.00\">");
        sb.append(buildIde(issueDate, invoiceNumber, cnf, cDV, request, issuer, request.getCustomer()));
        sb.append(buildEmit(issuer));
        if (request.getCustomer() != null) {
            if (isNFCe(request) && !request.getCustomer().getState().equalsIgnoreCase(issuer.getUf())) {
                throw new NfeBusinessException(
                        "NFC-e so pode ser emitida para consumidor do mesmo estado do emitente. "
                                + "Para cliente de outro estado, utilize NF-e (modelo 55). "
                                + "UF do consumidor: " + request.getCustomer().getState()
                                + ", UF do emitente: " + issuer.getUf());
            }
            sb.append(buildDest(request.getCustomer()));
        } else if (!isNFCe(request)) {
            throw new NfeBusinessException("Dados do destinatario sao obrigatorios para NF-e (modelo 55)");
        }

        int itemNumber = 1;
        for (NfeItemRequest item : request.getItems()) {
            sb.append(buildDet(item, itemNumber++, issuer, request));
        }

        sb.append(buildTotal(request.getItems(), request));
        sb.append("<transp><modFrete>9</modFrete></transp>");
        if (!isNFCe(request)) {
            sb.append(buildCobr(invoiceNumber, request.getItems()));
        }
        sb.append(buildPag(request));
        sb.append("</infNFe>");
        if (isNFCe(request)) {
            sb.append(buildInfNFeSupl(accessKey));
        }
        sb.append("</NFe>");
        sb.append("</enviNFe>");
        return sb.toString();
    }

    private String resolveModelo(NfeEmissionRequest request) {
        String modelo = request.getModelo();
        return modelo != null && !modelo.isBlank() ? modelo : properties.getModelo();
    }

    private boolean isNFCe(NfeEmissionRequest request) {
        return "65".equals(resolveModelo(request));
    }

    private String tpImpForIde(NfeEmissionRequest request) {
        if (!isNFCe(request)) {
            return "1";
        }
        return Boolean.FALSE.equals(request.getPrintReceipt()) ? "5" : "4";
    }

    private String resolveIdDest(CustomerInfo customer, NfeIssuer issuer) {
        if (customer == null || customer.getState() == null) {
            return "1";
        }
        return customer.getState().equalsIgnoreCase(issuer.getUf()) ? "1" : "2";
    }

    /**
     * Grupo suplementar exigido para NFC-e (modelo 65): QRCode (padrao V3
     * online, NT 2025-001) e URL de consulta por chave de acesso.
     */
    private String buildInfNFeSupl(String accessKey) {
        NfeProperties.NfceProperties nfce = properties.getNfce();
        if (nfce == null || !hasText(nfce.getQrcodeUrl()) || !hasText(nfce.getConsultaUrl())) {
            throw new IllegalStateException(
                    "Configuracao de NFC-e incompleta: defina nfe.nfce.qrcode-url e nfe.nfce.consulta-url");
        }
        String qrCode = NFCeUtil.getCodeQRCodeV3(accessKey, properties.getAmbiente(), nfce.getQrcodeUrl());
        StringBuilder sb = new StringBuilder();
        sb.append("<infNFeSupl>");
        sb.append("<qrCode><![CDATA[").append(qrCode).append("]]></qrCode>");
        sb.append("<urlChave>").append(escape(nfce.getConsultaUrl())).append("</urlChave>");
        sb.append("</infNFeSupl>");
        return sb.toString();
    }

    private String buildIde(OffsetDateTime issueDate, String nNF, String cNF, String cDV, NfeEmissionRequest request, NfeIssuer issuer, CustomerInfo customer) {
        StringBuilder sb = new StringBuilder();
        sb.append("<ide>");
        sb.append("<cUF>").append(UfMapper.codeFor(issuer.getUf())).append("</cUF>");
        sb.append("<cNF>").append(cNF).append("</cNF>");
        sb.append("<natOp>").append(escape(request.getNatureOperation())).append("</natOp>");
        sb.append("<mod>").append(escape(resolveModelo(request))).append("</mod>");
        sb.append("<serie>").append(escape(properties.getSerie())).append("</serie>");
        sb.append("<nNF>").append(nNF).append("</nNF>");
        sb.append("<dhEmi>").append(ISO_FMT.format(issueDate)).append("</dhEmi>");
        sb.append("<tpNF>1</tpNF>");
        sb.append("<idDest>").append(resolveIdDest(customer, issuer)).append("</idDest>");
        sb.append("<cMunFG>").append(escape(issuer.getMunicipioCodigo())).append("</cMunFG>");
        sb.append("<tpImp>").append(tpImpForIde(request)).append("</tpImp>");
        sb.append("<tpEmis>1</tpEmis>");
        sb.append("<cDV>").append(cDV).append("</cDV>");
        sb.append("<tpAmb>").append(properties.getAmbiente()).append("</tpAmb>");
        sb.append("<finNFe>1</finNFe>");
        sb.append("<indFinal>1</indFinal>");
        sb.append("<indPres>").append(isNFCe(request) ? "1" : "0").append("</indPres>");
        sb.append("<procEmi>0</procEmi>");
        sb.append("<verProc>").append(escape(properties.getProcessoVersao())).append("</verProc>");
        sb.append("</ide>");
        return sb.toString();
    }

    private String buildEmit(NfeIssuer issuer) {
        StringBuilder sb = new StringBuilder();
        sb.append("<emit>");
        sb.append("<CNPJ>").append(digitsOnly(issuer.getCnpj())).append("</CNPJ>");
        sb.append("<xNome>").append(escape(issuer.getRazaoSocial())).append("</xNome>");
        if (issuer.getNomeFantasia() != null && !issuer.getNomeFantasia().isBlank()) {
            sb.append("<xFant>").append(escape(issuer.getNomeFantasia())).append("</xFant>");
        }
        sb.append("<enderEmit>");
        sb.append("<xLgr>").append(escape(issuer.getLogradouro())).append("</xLgr>");
        sb.append("<nro>").append(escape(issuer.getNumero())).append("</nro>");
        sb.append("<xBairro>").append(escape(issuer.getBairro())).append("</xBairro>");
        sb.append("<cMun>").append(escape(issuer.getMunicipioCodigo())).append("</cMun>");
        sb.append("<xMun>").append(escape(issuer.getMunicipioNome())).append("</xMun>");
        sb.append("<UF>").append(escape(issuer.getUf())).append("</UF>");
        sb.append("<CEP>").append(digitsOnly(issuer.getCep())).append("</CEP>");
        sb.append("<cPais>").append(escape(issuer.getPaisCodigo())).append("</cPais>");
        sb.append("<xPais>").append(escape(issuer.getPaisNome())).append("</xPais>");
        if (issuer.getFone() != null && !issuer.getFone().isBlank()) {
            sb.append("<fone>").append(digitsOnly(issuer.getFone())).append("</fone>");
        }
        sb.append("</enderEmit>");
        String ie = issuer.getIe() != null && !issuer.getIe().isBlank() ? issuer.getIe() : "ISENTO";
        sb.append("<IE>").append(escape(ie)).append("</IE>");
        sb.append("<CRT>").append(escape(issuer.getCrt())).append("</CRT>");
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

    private String buildDet(NfeItemRequest item, int itemNumber, NfeIssuer issuer, NfeEmissionRequest request) {
        BigDecimal qCom = round(item.getQuantity(), 4);
        BigDecimal vUnCom = round(item.getUnitValue(), 10);
        BigDecimal vProd = round(qCom.multiply(vUnCom), 2);

        StringBuilder sb = new StringBuilder();
        sb.append("<det nItem=\"").append(itemNumber).append("\">");
        sb.append("<prod>");
        sb.append("<cProd>").append(escape(item.getProductCode())).append("</cProd>");
        sb.append("<cEAN>SEM GTIN</cEAN>");
        String xProd = isFirstItemHomologacaoNfce(itemNumber, request)
                ? XPROD_HOMOLOGACAO
                : escape(item.getDescription());
        sb.append("<xProd>").append(xProd).append("</xProd>");
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
        sb.append(buildIcms(issuer, request));
        sb.append(buildPis(issuer));
        sb.append(buildCofins(issuer));
        if (isNFCe(request)) {
            sb.append(NfeIbsCbsXmlBuilder.buildItem(item));
        }
        sb.append("</imposto>");
        sb.append("</det>");
        return sb.toString();
    }

    private boolean isFirstItemHomologacaoNfce(int itemNumber, NfeEmissionRequest request) {
        return itemNumber == 1 && isNFCe(request) && "2".equals(properties.getAmbiente());
    }

    private String buildIcms(NfeIssuer issuer, NfeEmissionRequest request) {
        StringBuilder sb = new StringBuilder();
        sb.append("<ICMS>");
        if (isRegimeNormal(issuer)) {
            sb.append("<ICMS40>");
            sb.append("<orig>0</orig>");
            sb.append("<CST>41</CST>");
            sb.append("</ICMS40>");
        } else {
            sb.append("<ICMSSN102>");
            sb.append("<orig>0</orig>");
            sb.append("<CSOSN>").append(isNFCe(request) ? "102" : "103").append("</CSOSN>");
            sb.append("</ICMSSN102>");
        }
        sb.append("</ICMS>");
        return sb.toString();
    }

    private String buildPis(NfeIssuer issuer) {
        StringBuilder sb = new StringBuilder();
        sb.append("<PIS>");
        if (isRegimeNormal(issuer)) {
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

    private String buildCofins(NfeIssuer issuer) {
        StringBuilder sb = new StringBuilder();
        sb.append("<COFINS>");
        if (isRegimeNormal(issuer)) {
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

    private boolean isRegimeNormal(NfeIssuer issuer) {
        return "3".equals(issuer.getCrt());
    }

    private String buildTotal(List<NfeItemRequest> items, NfeEmissionRequest request) {
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
        if (isNFCe(request)) {
            sb.append(NfeIbsCbsXmlBuilder.buildTotal(items));
        }
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

    private String buildPag(NfeEmissionRequest request) {
        BigDecimal total = totalProducts(request.getItems());
        PaymentInfo payment = request.getPayment();

        String tPag;
        BigDecimal vPag;
        if (payment == null) {
            tPag = "01";
            vPag = total;
            payment = PaymentInfo.builder().tPag(tPag).vPag(vPag).build();
        } else {
            tPag = payment.getTPag();
            vPag = payment.getVPag();
            if (tPag == null || tPag.isBlank()) {
                throw new IllegalArgumentException("tPag da forma de pagamento e obrigatorio");
            }
            validateTpag(tPag);
            if (vPag == null && !"90".equals(tPag)) {
                vPag = total;
            }
        }

        StringBuilder sb = new StringBuilder();
        sb.append("<pag>");
        sb.append("<detPag>");
        if (hasText(payment.getIndPag())) {
            sb.append("<indPag>").append(escape(payment.getIndPag())).append("</indPag>");
        }
        sb.append("<tPag>").append(escape(tPag)).append("</tPag>");
        if (vPag != null) {
            sb.append("<vPag>").append(formatDecimal(vPag)).append("</vPag>");
        }
        if (hasText(payment.getTpIntegra())) {
            sb.append(buildCard(payment));
        }
        sb.append("</detPag>");
        if (payment.getVTroco() != null) {
            sb.append("<vTroco>").append(formatDecimal(payment.getVTroco())).append("</vTroco>");
        }
        sb.append("</pag>");
        return sb.toString();
    }

    private void validateTpag(String tPag) {
        if (!Set.of("01", "02", "03", "04", "05", "10", "11", "12", "13", "14", "15", "90", "99").contains(tPag)) {
            throw new IllegalArgumentException("tPag invalido: " + tPag
                    + ". Valores aceitos: 01,02,03,04,05,10,11,12,13,14,15,90,99");
        }
    }

    private String buildCard(PaymentInfo payment) {
        StringBuilder sb = new StringBuilder();
        sb.append("<card>");
        sb.append("<tpIntegra>").append(escape(payment.getTpIntegra())).append("</tpIntegra>");
        if (hasText(payment.getCnpjCredenciadora())) {
            sb.append("<CNPJ>").append(digitsOnly(payment.getCnpjCredenciadora())).append("</CNPJ>");
        }
        if (hasText(payment.getTBand())) {
            sb.append("<tBand>").append(escape(payment.getTBand())).append("</tBand>");
        }
        if (hasText(payment.getCAut())) {
            sb.append("<cAut>").append(escape(payment.getCAut())).append("</cAut>");
        }
        sb.append("</card>");
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
