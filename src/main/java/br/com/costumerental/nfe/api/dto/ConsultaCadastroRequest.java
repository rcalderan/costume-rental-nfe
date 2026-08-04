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
public class ConsultaCadastroRequest implements Serializable {

    @NotBlank(message = "UF e obrigatoria")
    @Pattern(regexp = "[A-Z]{2}", message = "UF deve ser sigla de 2 letras maiusculas")
    private String uf;

    @NotBlank(message = "CPF/CNPJ e obrigatorio")
    @Pattern(regexp = "\\d{11}|\\d{14}", message = "Documento deve ser CPF (11 digitos) ou CNPJ (14 digitos)")
    private String document;
}
