package br.com.costumerental.nfe.api.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.time.LocalDate;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CertificateDetailsResponse implements Serializable {

    private boolean valido;
    private String cnpjCpf;
    private LocalDate vencimento;
    private Long diasRestantes;
    private String erro;
}
