package br.com.costumerental.nfe.api;

import br.com.costumerental.nfe.api.dto.IssuerResponse;
import br.com.costumerental.nfe.application.IssuerConfigService;
import br.com.costumerental.nfe.application.IssuerSetupRequest;
import br.com.costumerental.nfe.domain.NfeIssuer;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.validation.Valid;

@RestController
@RequestMapping("/api/v1/issuer")
@Tag(name = "Emitente", description = "Configuração do emitente da NF-e")
public class IssuerController {

    private final IssuerConfigService issuerConfigService;

    public IssuerController(IssuerConfigService issuerConfigService) {
        this.issuerConfigService = issuerConfigService;
    }

    @GetMapping
    @Operation(summary = "Consultar emitente ativo", description = "Retorna os dados do emitente ativo configurado no NFe")
    public ResponseEntity<IssuerResponse> getCurrentIssuer() {
        return issuerConfigService.findCurrentIssuer()
                .map(this::toResponse)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping("/setup")
    @Operation(summary = "Configurar emitente", description = "Cadastra/atualiza o emitente ativo a partir dos dados enviados pelo frontend")
    public ResponseEntity<IssuerResponse> setupIssuer(@Valid @RequestBody IssuerSetupRequest request) {
        NfeIssuer issuer = issuerConfigService.configureIssuer(request);
        return ResponseEntity.ok(toResponse(issuer));
    }

    private IssuerResponse toResponse(NfeIssuer issuer) {
        return IssuerResponse.builder()
                .cnpj(issuer.getCnpj())
                .razaoSocial(issuer.getRazaoSocial())
                .nomeFantasia(issuer.getNomeFantasia())
                .ie(issuer.getIe())
                .im(issuer.getIm())
                .crt(issuer.getCrt())
                .fone(issuer.getFone())
                .logradouro(issuer.getLogradouro())
                .numero(issuer.getNumero())
                .bairro(issuer.getBairro())
                .municipioCodigo(issuer.getMunicipioCodigo())
                .municipioNome(issuer.getMunicipioNome())
                .uf(issuer.getUf())
                .cep(issuer.getCep())
                .paisCodigo(issuer.getPaisCodigo())
                .paisNome(issuer.getPaisNome())
                .certificateConfigured(issuer.getCertificatePath() != null && !issuer.getCertificatePath().isBlank())
                .build();
    }
}
