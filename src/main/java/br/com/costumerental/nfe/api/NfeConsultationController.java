package br.com.costumerental.nfe.api;

import br.com.costumerental.nfe.api.dto.NfeEmissionResponse;
import br.com.costumerental.nfe.api.dto.StatusServicoResponse;
import br.com.costumerental.nfe.application.NfeConsultationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/nfe")
@Tag(name = "NF-e Consultas", description = "Consultas de status, situacao e recibo da NF-e na SEFAZ")
public class NfeConsultationController {

    private static final String NAO_ENCONTRADA_CSTAT = "217";
    private static final String LOTE_EM_PROCESSAMENTO_CSTAT = "105";

    private final NfeConsultationService consultationService;

    public NfeConsultationController(NfeConsultationService consultationService) {
        this.consultationService = consultationService;
    }

    @GetMapping("/status-servico")
    @Operation(summary = "Status do servico", description = "Consulta o status do servico da SEFAZ")
    public ResponseEntity<StatusServicoResponse> statusServico() {
        return ResponseEntity.ok(consultationService.consultarStatusServico());
    }

    @GetMapping("/recibo/{nRec}")
    @Operation(summary = "Consultar recibo", description = "Consulta o protocolo de autorizacao a partir do numero do recibo")
    public ResponseEntity<NfeEmissionResponse> consultarRecibo(@PathVariable String nRec) {
        NfeEmissionResponse response = consultationService.consultarRecibo(nRec);
        HttpStatus status = LOTE_EM_PROCESSAMENTO_CSTAT.equals(response.getStatusCode())
                ? HttpStatus.ACCEPTED
                : HttpStatus.OK;
        return ResponseEntity.status(status).body(response);
    }

    @GetMapping("/{chave}")
    @Operation(summary = "Consultar NF-e", description = "Consulta a situacao da NF-e na SEFAZ pela chave de acesso")
    public ResponseEntity<NfeEmissionResponse> consultarPorChave(@PathVariable String chave) {
        NfeEmissionResponse response = consultationService.consultarXml(chave);
        HttpStatus status = NAO_ENCONTRADA_CSTAT.equals(response.getStatusCode())
                ? HttpStatus.NOT_FOUND
                : HttpStatus.OK;
        return ResponseEntity.status(status).body(response);
    }
}
