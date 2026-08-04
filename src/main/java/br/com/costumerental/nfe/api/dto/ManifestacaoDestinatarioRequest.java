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
public class ManifestacaoDestinatarioRequest implements Serializable {

    @NotBlank(message = "Tipo de manifestacao e obrigatorio")
    @Pattern(regexp = "210200|210210|210220|210240", message = "Tipo de manifestacao deve ser 210200 (confirmacao), 210210 (ciencia), 210220 (desconhecimento) ou 210240 (operacao nao realizada)")
    private String eventCode;
}
