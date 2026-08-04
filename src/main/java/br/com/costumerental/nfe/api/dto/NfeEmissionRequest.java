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

    @NotNull(message = "Dados do destinatario sao obrigatorios")
    @Valid
    private CustomerInfo customer;

    private String originId;

    private String origin;
}
