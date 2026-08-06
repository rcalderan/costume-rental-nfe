package br.com.costumerental.nfe.api;

import br.com.costumerental.nfe.api.dto.CertificateDetailsResponse;
import br.com.costumerental.nfe.config.NfeProperties;
import br.com.costumerental.nfe.infrastructure.certificado.CertificateLoader;
import br.com.swconsultoria.certificado.Certificado;
import br.com.swconsultoria.certificado.exception.CertificadoException;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;

@RestController
@RequestMapping("/api/v1/certificates")
@Tag(name = "Certificado Digital", description = "Upload e consulta do certificado A1 usado na comunicacao com a SEFAZ")
public class CertificateController {

    private final CertificateLoader certificateLoader;
    private final NfeProperties properties;

    public CertificateController(CertificateLoader certificateLoader, NfeProperties properties) {
        this.certificateLoader = certificateLoader;
        this.properties = properties;
    }

    @PostMapping(value = "/upload", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @Operation(summary = "Enviar certificado", description = "Faz upload de um arquivo .pfx e senha, valida e ativa o certificado")
    public ResponseEntity<CertificateDetailsResponse> upload(
            @RequestParam("file") MultipartFile file,
            @RequestParam("password") String password) throws CertificadoException {
        String originalName = file.getOriginalFilename();
        if (originalName == null || !originalName.toLowerCase().endsWith(".pfx")) {
            throw new CertificadoException("O arquivo deve estar no formato .pfx. Nome recebido: " + originalName);
        }

        Path targetPath = resolveTargetPath(originalName);
        Path targetDir = targetPath.getParent();
        if (targetDir == null) {
            throw new CertificadoException("Nao foi possivel determinar o diretorio do certificado a partir de " + targetPath);
        }

        Path tempPath = null;
        try {
            tempPath = Files.createTempFile(targetDir, "upload", ".pfx");
            Files.write(tempPath, file.getBytes());
            Certificado certificado = certificateLoader.load(tempPath.toString(), password);
            Files.move(tempPath, targetPath, StandardCopyOption.REPLACE_EXISTING);
            properties.getCertificate().setPath(targetPath.toString());
            properties.getCertificate().setPassword(password);
            return ResponseEntity.ok(toResponse(certificado));
        } catch (CertificadoException e) {
            deleteQuietly(tempPath);
            throw e;
        } catch (IOException e) {
            deleteQuietly(tempPath);
            throw new CertificadoException("Falha ao salvar o certificado: " + e.getMessage(), e);
        }
    }

    @GetMapping("/status")
    @Operation(summary = "Status do certificado", description = "Retorna CNPJ/CPF, vencimento e dias restantes do certificado ativo")
    public ResponseEntity<CertificateDetailsResponse> status() throws CertificadoException {
        Certificado certificado = certificateLoader.load();
        return ResponseEntity.ok(toResponse(certificado));
    }

    @ExceptionHandler(CertificadoException.class)
    public ResponseEntity<CertificateDetailsResponse> handleCertificateException(CertificadoException ex) {
        CertificateDetailsResponse response = CertificateDetailsResponse.builder()
                .valido(false)
                .erro(ex.getMessage())
                .build();
        return ResponseEntity.unprocessableEntity().body(response);
    }

    private Path resolveTargetPath(String originalName) {
        String configuredPath = properties.getCertificate().getPath();
        Path basePath = Paths.get(configuredPath).toAbsolutePath();
        Path targetDir = basePath.getParent();
        if (targetDir == null) {
            targetDir = Paths.get("").toAbsolutePath();
        }
        String safeName = sanitizeFileName(originalName);
        return targetDir.resolve(safeName);
    }

    private String sanitizeFileName(String originalName) {
        String name = originalName.replaceAll("[^a-zA-Z0-9._-]", "_");
        if (!name.toLowerCase().endsWith(".pfx")) {
            name = name + ".pfx";
        }
        return name;
    }

    private void deleteQuietly(Path path) {
        if (path == null) {
            return;
        }
        try {
            Files.deleteIfExists(path);
        } catch (IOException ignored) {
            // Best-effort cleanup; the validation error is already reported.
        }
    }

    private CertificateDetailsResponse toResponse(Certificado certificado) {
        return CertificateDetailsResponse.builder()
                .valido(certificado.isValido())
                .cnpjCpf(certificado.getCnpjCpf())
                .vencimento(certificado.getVencimento())
                .diasRestantes(certificado.getDiasRestantes())
                .build();
    }
}
