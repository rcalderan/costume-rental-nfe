package br.com.costumerental.nfe.xml;

import br.com.costumerental.nfe.application.FiscalDocumentNumberControlService;
import br.com.costumerental.nfe.config.NfeProperties;
import br.com.costumerental.nfe.domain.NfeIssuer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.time.OffsetDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Random;

@Component
public class AccessKeyGenerator {

    private static final Random RANDOM = new Random();
    private static final DateTimeFormatter AAMM = DateTimeFormatter.ofPattern("yyMM");

    private final NfeProperties properties;
    private final FiscalDocumentNumberControlService numberControlService;

    @Autowired
    public AccessKeyGenerator(NfeProperties properties,
                              FiscalDocumentNumberControlService numberControlService) {
        this.properties = properties;
        this.numberControlService = numberControlService;
    }

    public AccessKeyGenerator(NfeProperties properties) {
        this.properties = properties;
        this.numberControlService = null;
    }

    public String generate(OffsetDateTime issueDate, NfeIssuer issuer, String serie, String invoiceNumber, String cnf) {
        String ufCode = UfMapper.codeFor(issuer.getUf());
        String aamm = AAMM.format(issueDate);
        String cnpj = digitsOnly(issuer.getCnpj(), 14);
        String mod = "55";
        String seriePadded = leftPad(serie, 3, '0');
        String nNF = leftPad(invoiceNumber, 9, '0');

        String partial = ufCode + aamm + cnpj + mod + seriePadded + nNF + "1" + cnf;
        int dv = calculateDV(partial);
        return partial + dv;
    }

    public String generate(OffsetDateTime issueDate, String serie, String invoiceNumber, String cnf) {
        String ufCode = UfMapper.codeFor(properties.getEmit().getUf());
        String aamm = AAMM.format(issueDate);
        String cnpj = digitsOnly(properties.getEmit().getCnpj(), 14);
        String mod = "55";
        String seriePadded = leftPad(serie, 3, '0');
        String nNF = leftPad(invoiceNumber, 9, '0');

        String partial = ufCode + aamm + cnpj + mod + seriePadded + nNF + "1" + cnf;
        int dv = calculateDV(partial);
        return partial + dv;
    }

    public String generateCNF() {
        return leftPad(String.valueOf(RANDOM.nextInt(99999999)), 8, '0');
    }

    public String generateInvoiceNumber(Long issuerId, String series) {
        if (numberControlService != null && issuerId != null) {
            return numberControlService.reserveNextNumber(issuerId, series);
        }
        return String.valueOf(RANDOM.nextInt(900000000) + 100000000);
    }

    public String generateInvoiceNumber() {
        return String.valueOf(RANDOM.nextInt(900000000) + 100000000);
    }

    private static int calculateDV(String chave) {
        int[] weights = {4, 3, 2, 9, 8, 7, 6, 5};
        int sum = 0;
        for (int i = 0; i < chave.length(); i++) {
            int digit = Character.getNumericValue(chave.charAt(i));
            sum += digit * weights[i % weights.length];
        }
        int remainder = sum % 11;
        int dv = 11 - remainder;
        if (dv >= 10) {
            dv = 0;
        }
        return dv;
    }

    private static String leftPad(String value, int length, char pad) {
        StringBuilder sb = new StringBuilder();
        for (int i = value.length(); i < length; i++) {
            sb.append(pad);
        }
        sb.append(value);
        return sb.toString();
    }

    private static String digitsOnly(String value, int length) {
        String digits = value.replaceAll("\\D", "");
        if (digits.length() != length) {
            throw new IllegalArgumentException("Valor deve conter exatamente " + length + " digitos, mas tinha " + digits.length() + ": " + value);
        }
        return digits;
    }
}
