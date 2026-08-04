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
public class CorrectionLetterRequest implements Serializable {

    @NotBlank(message = "Correcao e obrigatoria")
    @Size(min = 15, max = 1000, message = "Correcao deve ter entre 15 e 1000 caracteres")
    private String correction;

    @NotBlank(message = "Sequencia do evento e obrigatoria")
    @Pattern(regexp = "\\d{1,3}", message = "Sequencia deve ser numerica de 1 a 3 digitos")
    private String sequence;
}
