package br.com.costumerental.nfe.api;

import br.com.costumerental.nfe.api.dto.CancelNfeRequest;
import br.com.costumerental.nfe.api.dto.ConsultaCadastroRequest;
import br.com.costumerental.nfe.api.dto.CorrectionLetterRequest;
import br.com.costumerental.nfe.api.dto.DistribuicaoDfeRequest;
import br.com.costumerental.nfe.api.dto.ManifestacaoDestinatarioRequest;
import br.com.costumerental.nfe.api.dto.NfeEventResponse;
import br.com.costumerental.nfe.api.dto.NfeInutilizacaoRequest;
import br.com.costumerental.nfe.api.dto.NfeInutilizacaoResponse;
import br.com.costumerental.nfe.api.dto.ConsultaCadastroResponse;
import br.com.costumerental.nfe.api.dto.DistribuicaoDfeResponse;
import br.com.costumerental.nfe.application.NfeEventService;
import br.com.costumerental.nfe.domain.NfeStatus;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.validation.Valid;

@RestController
@RequestMapping("/api/v1/nfe")
@Tag(name = "NF-e Eventos", description = "Eventos da NF-e: cancelamento, carta de correcao, inutilizacao, manifestacao e consultas")
public class NfeEventController {

    private final NfeEventService eventService;

    public NfeEventController(NfeEventService eventService) {
        this.eventService = eventService;
    }

    @PostMapping("/{chave}/cancelar")
    @Operation(summary = "Cancelar NF-e", description = "Envia evento de cancelamento para uma NF-e autorizada")
    public ResponseEntity<NfeEventResponse> cancelar(@PathVariable String chave,
                                                     @Valid @RequestBody CancelNfeRequest request) {
        NfeEventResponse response = eventService.cancelar(chave, request);
        return ResponseEntity.status(statusFor(response)).body(response);
    }

    @PostMapping("/{chave}/carta-correcao")
    @Operation(summary = "Carta de Correcao", description = "Envia evento de carta de correcao para uma NF-e autorizada")
    public ResponseEntity<NfeEventResponse> cartaCorrecao(@PathVariable String chave,
                                                          @Valid @RequestBody CorrectionLetterRequest request) {
        NfeEventResponse response = eventService.cartaCorrecao(chave, request);
        return ResponseEntity.status(statusFor(response)).body(response);
    }

    @PostMapping("/inutilizar")
    @Operation(summary = "Inutilizar numeracao", description = "Solicita inutilizacao de numeracao de NF-e")
    public ResponseEntity<NfeInutilizacaoResponse> inutilizar(@Valid @RequestBody NfeInutilizacaoRequest request) {
        NfeInutilizacaoResponse response = eventService.inutilizar(request);
        return ResponseEntity.status(statusFor(response)).body(response);
    }

    @PostMapping("/{chave}/manifestacao")
    @Operation(summary = "Manifestacao do destinatario", description = "Envia manifestacao do destinatario para uma NF-e")
    public ResponseEntity<NfeEventResponse> manifestacao(@PathVariable String chave,
                                                         @Valid @RequestBody ManifestacaoDestinatarioRequest request) {
        NfeEventResponse response = eventService.manifestacaoDestinatario(chave, request);
        return ResponseEntity.status(statusFor(response)).body(response);
    }

    @PostMapping("/consulta-cadastro")
    @Operation(summary = "Consulta cadastro", description = "Consulta cadastro de contribuinte na SEFAZ")
    public ResponseEntity<ConsultaCadastroResponse> consultaCadastro(@Valid @RequestBody ConsultaCadastroRequest request) {
        return ResponseEntity.ok(eventService.consultaCadastro(request));
    }

    @PostMapping("/distribuicao-dfe")
    @Operation(summary = "Distribuicao DFe", description = "Consulta documentos fiscais eletronicos distribuidos")
    public ResponseEntity<DistribuicaoDfeResponse> distribuicaoDfe(@Valid @RequestBody DistribuicaoDfeRequest request) {
        return ResponseEntity.ok(eventService.distribuicaoDfe(request));
    }

    private HttpStatus statusFor(NfeEventResponse response) {
        return response.getStatus() == NfeStatus.AUTHORIZED ? HttpStatus.OK : HttpStatus.UNPROCESSABLE_ENTITY;
    }

    private HttpStatus statusFor(NfeInutilizacaoResponse response) {
        return response.getStatus() == NfeStatus.AUTHORIZED ? HttpStatus.OK : HttpStatus.UNPROCESSABLE_ENTITY;
    }
}
