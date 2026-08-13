package br.com.costumerental.nfe.api;

import br.com.costumerental.nfe.api.dto.IssuerResponse;
import br.com.costumerental.nfe.application.IssuerBranchSetupRequest;
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
import java.util.List;

@RestController
@RequestMapping("/api/v1/issuer")
@Tag(name = "Emitente", description = "Configuracao do emitente da NF-e (matriz e filiais)")
public class IssuerController {

    private final IssuerConfigService issuerConfigService;

    public IssuerController(IssuerConfigService issuerConfigService) {
        this.issuerConfigService = issuerConfigService;
    }

    @GetMapping
    @Operation(summary = "Consultar emitente ativo", description = "Retorna os dados do emitente ativo (matriz ou filial) configurado no NFe")
    public ResponseEntity<IssuerResponse> getCurrentIssuer() {
        return issuerConfigService.findCurrentIssuer()
                .map(this::toResponse)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/branches")
    @Operation(summary = "Listar filiais", description = "Lista matriz e filiais do mesmo CNPJ raiz do emitente ativo")
    public ResponseEntity<List<IssuerResponse>> listBranches() {
        return issuerConfigService.findCurrentIssuer()
                .map(current -> issuerConfigService.findBranchesOf(current.getRootCnpj()))
                .map(issuers -> issuers.stream().map(this::toResponse).toList())
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping("/setup")
    @Operation(summary = "Configurar matriz", description = "Cadastra/atualiza a matriz (CNPJ sufixo 0001) a partir dos dados enviados pelo frontend")
    public ResponseEntity<IssuerResponse> setupIssuer(@Valid @RequestBody IssuerSetupRequest request) {
        NfeIssuer issuer = issuerConfigService.configureIssuer(request);
        return ResponseEntity.ok(toResponse(issuer));
    }

    @PostMapping("/branch")
    @Operation(summary = "Cadastrar filial", description = "Cadastra uma filial (CNPJ sufixo > 0001) vinculada a matriz ja cadastrada")
    public ResponseEntity<IssuerResponse> setupBranch(@Valid @RequestBody IssuerBranchSetupRequest request) {
        NfeIssuer issuer = issuerConfigService.configureBranch(request);
        return ResponseEntity.ok(toResponse(issuer));
    }

    private IssuerResponse toResponse(NfeIssuer issuer) {
        return IssuerResponse.builder()
                .cnpj(issuer.getCnpj())
                .rootCnpj(issuer.getRootCnpj())
                .branchOrder(issuer.getBranchOrder())
                .digitoControle(issuer.getDigitoControle())
                .matriz(issuer.isMatriz())
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
