package br.com.costumerental.nfe.api.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.Valid;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import java.io.Serializable;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class NfeEmissionRequest implements Serializable {

    @NotBlank(message = "Natureza da operacao e obrigatoria")
    private String natureOperation;

    @NotEmpty(message = "A NF-e deve conter ao menos um item")
    @Valid
    private List<NfeItemRequest> items;

    @Valid
    private CustomerInfo customer;

    private String originId;

    private String origin;

    /**
     * CNPJ do emitente (matriz ou filial) a ser usado na emissao.
     * Se omitido, usa a matriz ativa (compatibilidade com chamadas legadas).
     */
    private String issuerCnpj;

    /**
     * Indica se o consumidor deseja receber o DANFE NFC-e impresso (tpImp=4).
     * false envia por mensagem eletronica (tpImp=5). Aplica-se apenas ao
     * modelo 65; para NF-e o valor e ignorado. Padrao: true.
     */
    private Boolean printReceipt;

    @Valid
    private PaymentInfo payment;
}
