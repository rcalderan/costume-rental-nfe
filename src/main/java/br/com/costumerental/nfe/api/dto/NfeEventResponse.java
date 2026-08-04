package br.com.costumerental.nfe.api.dto;

import br.com.costumerental.nfe.domain.NfeEventType;
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
public class NfeEventResponse implements Serializable {

    private String accessKey;
    private NfeEventType eventType;
    private String sequence;
    private String protocol;
    private NfeStatus status;
    private String statusCode;
    private String statusMessage;
    private String eventXml;
    private String responseXml;
}
