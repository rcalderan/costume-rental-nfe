package br.com.costumerental.nfe.api.dto;

import br.com.costumerental.nfe.domain.NfeStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class NfeInutilizacaoResponse implements Serializable {

    private String year;
    private String cnpj;
    private String model;
    private String series;
    private String initialNumber;
    private String finalNumber;
    private String protocol;
    private NfeStatus status;
    private String statusCode;
    private String statusMessage;
    private String responseXml;
}
