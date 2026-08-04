package br.com.costumerental.nfe.api.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotBlank;
import java.io.Serializable;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CustomerInfo implements Serializable {

    @NotBlank(message = "Nome do destinatario e obrigatorio")
    private String name;

    @NotBlank(message = "Documento do destinatario e obrigatorio")
    private String document;

    private String ie;

    @NotBlank(message = "Logradouro e obrigatorio")
    private String street;

    @NotBlank(message = "Numero e obrigatorio")
    private String number;

    private String complement;

    @NotBlank(message = "Bairro e obrigatorio")
    private String neighborhood;

    @NotBlank(message = "Codigo do municipio e obrigatorio")
    private String cityCode;

    @NotBlank(message = "Nome do municipio e obrigatorio")
    private String cityName;

    @NotBlank(message = "UF e obrigatoria")
    private String state;

    @NotBlank(message = "CEP e obrigatorio")
    private String zipCode;

    private String phone;
}
