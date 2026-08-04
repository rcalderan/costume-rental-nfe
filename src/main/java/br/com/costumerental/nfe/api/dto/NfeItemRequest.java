package br.com.costumerental.nfe.api.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.Min;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.io.Serializable;
import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class NfeItemRequest implements Serializable {

    @NotBlank(message = "Codigo do produto e obrigatorio")
    private String productCode;

    @NotBlank(message = "Descricao do produto e obrigatoria")
    private String description;

    @NotBlank(message = "NCM e obrigatorio")
    private String ncm;

    @NotBlank(message = "CFOP e obrigatorio")
    private String cfop;

    @NotBlank(message = "Unidade e obrigatoria")
    private String unit;

    @NotNull(message = "Quantidade e obrigatoria")
    @Min(value = 1, message = "Quantidade deve ser no minimo 1")
    private BigDecimal quantity;

    @NotNull(message = "Valor unitario e obrigatorio")
    @Min(value = 0, message = "Valor unitario nao pode ser negativo")
    private BigDecimal unitValue;

    public BigDecimal totalValue() {
        return quantity.multiply(unitValue);
    }
}
