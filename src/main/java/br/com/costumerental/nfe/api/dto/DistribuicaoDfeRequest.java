package br.com.costumerental.nfe.api.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Pattern;
import java.io.Serializable;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DistribuicaoDfeRequest implements Serializable {

    @NotBlank(message = "Tipo de consulta e obrigatorio")
    @Pattern(regexp = "NSU|NSU_UNICO|CHAVE", message = "Tipo de consulta deve ser NSU, NSU_UNICO ou CHAVE")
    private String queryType;

    @NotBlank(message = "CPF/CNPJ do interessado e obrigatorio")
    @Pattern(regexp = "\\d{11}|\\d{14}", message = "Documento deve ser CPF (11 digitos) ou CNPJ (14 digitos)")
    private String document;

    @NotBlank(message = "NSU ou chave de acesso e obrigatorio")
    private String nsuOrKey;
}
