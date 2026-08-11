package br.com.costumerental.nfe.exception;

import br.com.swconsultoria.certificado.exception.CertificadoException;
import br.com.swconsultoria.nfe.exception.NfeException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.HashMap;
import java.util.Map;

@Slf4j
@RestControllerAdvice
public class NfeExceptionHandler {

    @ExceptionHandler(NfeBusinessException.class)
    public ResponseEntity<Map<String, Object>> handleBusinessException(NfeBusinessException ex) {
        Map<String, Object> error = new HashMap<>();
        error.put("error", ex.getMessage());
        if (ex.getXml() != null) {
            error.put("data", ex.getXml());
        }
        return ResponseEntity.status(HttpStatus.UNPROCESSABLE_ENTITY).body(error);
    }

    @ExceptionHandler({NfeException.class, CertificadoException.class})
    public ResponseEntity<Map<String, String>> handleNfeException(Exception ex) {
        Map<String, String> error = new HashMap<>();
        error.put("error", ex.getMessage());
        return ResponseEntity.status(HttpStatus.BAD_GATEWAY).body(error);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Map<String, String>> handleValidationException(MethodArgumentNotValidException ex) {
        Map<String, String> errors = new HashMap<>();
        ex.getBindingResult().getAllErrors().forEach(e -> {
            String field = e instanceof FieldError ? ((FieldError) e).getField() : e.getObjectName();
            errors.put(field, e.getDefaultMessage());
        });
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errors);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<Map<String, String>> handleGenericException(Exception ex) {
        log.error("Erro interno nao tratado: {}", ex.getClass().getName(), ex);
        Map<String, String> error = new HashMap<>();
        String msg = ex.getMessage();
        if (msg == null && ex.getCause() != null) {
            msg = ex.getCause().getClass().getSimpleName() + ": " + ex.getCause().getMessage();
        }
        error.put("error", "Erro interno: " + msg);
        error.put("type", ex.getClass().getSimpleName());
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
    }
}
