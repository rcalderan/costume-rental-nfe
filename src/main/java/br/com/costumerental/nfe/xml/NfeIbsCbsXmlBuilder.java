package br.com.costumerental.nfe.xml;

import br.com.costumerental.nfe.api.dto.NfeItemRequest;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;

final class NfeIbsCbsXmlBuilder {

    private static final String CCLASS_TRIB_PADRAO = "000001";
    private static final String CST_IBS_CBS_TRIBUTADO = "000";
    private static final BigDecimal ALIQUOTA_IBS_PERCENTUAL = new BigDecimal("0.10");
    private static final BigDecimal ALIQUOTA_CBS_PERCENTUAL = new BigDecimal("0.90");
    private static final BigDecimal PERCENTUAL_BASE = new BigDecimal("100");

    private NfeIbsCbsXmlBuilder() {
    }

    static String buildItem(NfeItemRequest item) {
        BigDecimal vBC = baseCalculo(item);
        BigDecimal vIBSUF = valorIbsUf(vBC);
        BigDecimal vIBSMun = valorIbsMun(vBC);
        BigDecimal vIBS = vIBSUF.add(vIBSMun).setScale(2, RoundingMode.HALF_UP);
        BigDecimal vCBS = valorCbs(vBC);

        StringBuilder sb = new StringBuilder();
        sb.append("<IBSCBS>");
        sb.append("<CST>").append(CST_IBS_CBS_TRIBUTADO).append("</CST>");
        sb.append("<cClassTrib>").append(CCLASS_TRIB_PADRAO).append("</cClassTrib>");
        sb.append("<gIBSCBS>");
        sb.append("<vBC>").append(formatDecimal(vBC)).append("</vBC>");
        sb.append("<gIBSUF>");
        sb.append("<pIBSUF>").append(formatDecimal(ALIQUOTA_IBS_PERCENTUAL, 4)).append("</pIBSUF>");
        sb.append("<vIBSUF>").append(formatDecimal(vIBSUF)).append("</vIBSUF>");
        sb.append("</gIBSUF>");
        sb.append("<gIBSMun>");
        sb.append("<pIBSMun>").append(formatDecimal(BigDecimal.ZERO, 4)).append("</pIBSMun>");
        sb.append("<vIBSMun>").append(formatDecimal(vIBSMun)).append("</vIBSMun>");
        sb.append("</gIBSMun>");
        sb.append("<vIBS>").append(formatDecimal(vIBS)).append("</vIBS>");
        sb.append("<gCBS>");
        sb.append("<vBC>").append(formatDecimal(vBC)).append("</vBC>");
        sb.append("<pCBS>").append(formatDecimal(ALIQUOTA_CBS_PERCENTUAL, 4)).append("</pCBS>");
        sb.append("<vCBS>").append(formatDecimal(vCBS)).append("</vCBS>");
        sb.append("</gCBS>");
        sb.append("</gIBSCBS>");
        sb.append("</IBSCBS>");
        return sb.toString();
    }

    static String buildTotal(List<NfeItemRequest> items) {
        BigDecimal vBCIBSCBS = BigDecimal.ZERO;
        BigDecimal vIBSUF = BigDecimal.ZERO;
        BigDecimal vIBSMun = BigDecimal.ZERO;
        BigDecimal vIBS = BigDecimal.ZERO;
        BigDecimal vCBS = BigDecimal.ZERO;

        for (NfeItemRequest item : items) {
            BigDecimal base = baseCalculo(item);
            BigDecimal itemIbsUf = valorIbsUf(base);
            BigDecimal itemIbsMun = valorIbsMun(base);

            vBCIBSCBS = vBCIBSCBS.add(base);
            vIBSUF = vIBSUF.add(itemIbsUf);
            vIBSMun = vIBSMun.add(itemIbsMun);
            vIBS = vIBS.add(itemIbsUf.add(itemIbsMun));
            vCBS = vCBS.add(valorCbs(base));
        }

        StringBuilder sb = new StringBuilder();
        sb.append("<IBSCBSTot>");
        sb.append("<vBCIBSCBS>").append(formatDecimal(round(vBCIBSCBS, 2))).append("</vBCIBSCBS>");
        sb.append("<gIBS>");
        sb.append("<gIBSUF>");
        sb.append("<vDif>0.00</vDif>");
        sb.append("<vDevTrib>0.00</vDevTrib>");
        sb.append("<vIBSUF>").append(formatDecimal(round(vIBSUF, 2))).append("</vIBSUF>");
        sb.append("</gIBSUF>");
        sb.append("<gIBSMun>");
        sb.append("<vDif>0.00</vDif>");
        sb.append("<vDevTrib>0.00</vDevTrib>");
        sb.append("<vIBSMun>").append(formatDecimal(round(vIBSMun, 2))).append("</vIBSMun>");
        sb.append("</gIBSMun>");
        sb.append("<vIBS>").append(formatDecimal(round(vIBS, 2))).append("</vIBS>");
        sb.append("<vCredPres>0.00</vCredPres>");
        sb.append("<vCredPresCondSus>0.00</vCredPresCondSus>");
        sb.append("</gIBS>");
        sb.append("<gCBS>");
        sb.append("<vDif>0.00</vDif>");
        sb.append("<vDevTrib>0.00</vDevTrib>");
        sb.append("<vCBS>").append(formatDecimal(round(vCBS, 2))).append("</vCBS>");
        sb.append("<vCredPres>0.00</vCredPres>");
        sb.append("<vCredPresCondSus>0.00</vCredPresCondSus>");
        sb.append("</gCBS>");
        sb.append("</IBSCBSTot>");
        return sb.toString();
    }

    private static BigDecimal baseCalculo(NfeItemRequest item) {
        return round(item.getQuantity().multiply(item.getUnitValue()), 2);
    }

    private static BigDecimal valorIbsUf(BigDecimal base) {
        return base.multiply(ALIQUOTA_IBS_PERCENTUAL)
                .divide(PERCENTUAL_BASE, 2, RoundingMode.HALF_UP);
    }

    private static BigDecimal valorIbsMun(BigDecimal base) {
        return BigDecimal.ZERO.setScale(2);
    }

    private static BigDecimal valorCbs(BigDecimal base) {
        return base.multiply(ALIQUOTA_CBS_PERCENTUAL)
                .divide(PERCENTUAL_BASE, 2, RoundingMode.HALF_UP);
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
