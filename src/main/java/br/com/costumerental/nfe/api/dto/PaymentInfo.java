package br.com.costumerental.nfe.api.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import com.fasterxml.jackson.annotation.JsonProperty;

import javax.validation.constraints.Pattern;
import java.io.Serializable;
import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PaymentInfo implements Serializable {

    @JsonProperty("tPag")
    @Pattern(regexp = "01|02|03|04|05|10|11|12|13|14|15|90|99",
            message = "Forma de pagamento invalida; consulte a documentacao da SEFAZ")
    private String tPag;

    @JsonProperty("vPag")
    private BigDecimal vPag;

    @JsonProperty("indPag")
    @Pattern(regexp = "0|1", message = "indPag deve ser 0 (a vista) ou 1 (a prazo)")
    private String indPag;

    @JsonProperty("tpIntegra")
    @Pattern(regexp = "1|2",
            message = "tpIntegra deve ser 1 (TEF) ou 2 (POS)")
    private String tpIntegra;

    @JsonProperty("cnpjCredenciadora")
    private String cnpjCredenciadora;

    @JsonProperty("tBand")
    @Pattern(regexp = "01|02|03|04|05|06|07|08|09|99",
            message = "tBand invalida")
    private String tBand;

    @JsonProperty("cAut")
    private String cAut;

    @JsonProperty("vTroco")
    private BigDecimal vTroco;
}
