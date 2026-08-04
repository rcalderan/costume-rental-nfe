package br.com.costumerental.nfe.api.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class StatusServicoResponse implements Serializable {

    private String ambiente;
    private String statusCode;
    private String statusMessage;
    private String uf;
    private String dataHoraRecebimento;
    private String tempoMedioResposta;
    private String dataHoraRetorno;
    private String observacao;
}
