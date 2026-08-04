package br.com.costumerental.nfe.api.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Pattern;
import javax.validation.constraints.Size;
import java.io.Serializable;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class NfeInutilizacaoRequest implements Serializable {

    @NotBlank(message = "Ano e obrigatorio")
    @Pattern(regexp = "\\d{2}", message = "Ano deve ter 2 digitos")
    private String year;

    @NotBlank(message = "CNPJ e obrigatorio")
    @Pattern(regexp = "\\d{14}", message = "CNPJ deve conter 14 digitos")
    private String cnpj;

    @NotBlank(message = "Modelo e obrigatorio")
    @Pattern(regexp = "55|65", message = "Modelo deve ser 55 (NF-e) ou 65 (NFC-e)")
    private String model;

    @NotBlank(message = "Serie e obrigatoria")
    @Pattern(regexp = "\\d{1,3}", message = "Serie deve ser numerica de 1 a 3 digitos")
    private String series;

    @NotBlank(message = "Numero inicial e obrigatorio")
    @Pattern(regexp = "\\d{1,9}", message = "Numero inicial deve ser numerico de ate 9 digitos")
    private String initialNumber;

    @NotBlank(message = "Numero final e obrigatorio")
    @Pattern(regexp = "\\d{1,9}", message = "Numero final deve ser numerico de ate 9 digitos")
    private String finalNumber;

    @NotBlank(message = "Justificativa e obrigatoria")
    @Size(min = 15, max = 255, message = "Justificativa deve ter entre 15 e 255 caracteres")
    private String justification;
}
