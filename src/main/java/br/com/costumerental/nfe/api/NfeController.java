package br.com.costumerental.nfe.api;

import br.com.costumerental.nfe.api.dto.NfeEmissionRequest;
import br.com.costumerental.nfe.api.dto.NfeEmissionResponse;
import br.com.costumerental.nfe.application.NfeEmissionService;
import br.com.costumerental.nfe.domain.NfeStatus;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.validation.Valid;

@RestController
@RequestMapping("/api/v1/nfe")
@Tag(name = "NF-e", description = "Emissao de Nota Fiscal Eletronica modelo 55 via SEFAZ")
public class NfeController {

    private final NfeEmissionService emissionService;

    public NfeController(NfeEmissionService emissionService) {
        this.emissionService = emissionService;
    }

    @PostMapping("/emit")
    @Operation(summary = "Emitir NF-e", description = "Assina, valida e envia a NF-e a SEFAZ")
    public ResponseEntity<NfeEmissionResponse> emit(@Valid @RequestBody NfeEmissionRequest request) {
        NfeEmissionResponse response = emissionService.emit(request);
        HttpStatus status = response.getStatus() == NfeStatus.AUTHORIZED
                ? HttpStatus.CREATED
                : HttpStatus.UNPROCESSABLE_ENTITY;
        return ResponseEntity.status(status).body(response);
    }

    @PostMapping("/sign-only")
    @Operation(summary = "Assinar XML", description = "Construi e assina o XML da NF-e sem enviar a SEFAZ")
    public ResponseEntity<NfeEmissionResponse> signOnly(@Valid @RequestBody NfeEmissionRequest request) {
        NfeEmissionResponse response = emissionService.buildAndSign(request);
        return ResponseEntity.ok(response);
    }
}
