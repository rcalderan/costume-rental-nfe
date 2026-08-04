package br.com.costumerental.nfe.api.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DistribuicaoDfeResponse implements Serializable {

    private String statusCode;
    private String statusMessage;
    private String ultimoNsu;
    private String maximoNsu;
    private List<DocumentoZip> documents;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class DocumentoZip implements Serializable {
        private String nsu;
        private String schema;
        private String base64Content;
    }
}
