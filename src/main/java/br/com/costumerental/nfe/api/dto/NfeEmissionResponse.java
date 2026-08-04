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
public class NfeEmissionResponse implements Serializable {

    private String accessKey;
    private String protocol;
    private String receiptNumber;
    private NfeStatus status;
    private String statusCode;
    private String statusMessage;
    private String authorizedXml;
    private String signedXml;
}
