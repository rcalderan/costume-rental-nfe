package br.com.costumerental.nfe.infrastructure.certificado;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.Cipher;
import javax.crypto.spec.GCMParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.security.SecureRandom;
import java.util.Base64;

@Component
public class CertificateEncryption {

    private static final String ALGORITHM = "AES";
    private static final String TRANSFORMATION = "AES/GCM/NoPadding";
    private static final int GCM_IV_LENGTH = 12;
    private static final int GCM_TAG_LENGTH = 16;
    private static final int KEY_LENGTH_BYTES = 32;

    private final SecretKeySpec secretKey;

    public CertificateEncryption(@Value("${CERT_ENCRYPTION_KEY:}") String key) {
        this.secretKey = buildKey(key);
    }

    private SecretKeySpec buildKey(String key) {
        if (key == null || key.isBlank()) {
            throw new IllegalStateException("CERT_ENCRYPTION_KEY não está configurada.");
        }
        byte[] decoded = decodeKey(key);
        return new SecretKeySpec(decoded, ALGORITHM);
    }

    private byte[] decodeKey(String key) {
        try {
            byte[] decoded = Base64.getDecoder().decode(key);
            if (decoded.length != KEY_LENGTH_BYTES) {
                throw new IllegalArgumentException(
                        "CERT_ENCRYPTION_KEY decodificada tem " + decoded.length
                                + " bytes; esperado " + KEY_LENGTH_BYTES + " bytes.");
            }
            return decoded;
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("CERT_ENCRYPTION_KEY não é base64 válido. "
                    + "Configure 32 bytes codificados em base64.", e);
        }
    }

    public String encrypt(String plaintext) {
        if (plaintext == null) {
            throw new IllegalArgumentException("Texto plano para cifrar não pode ser nulo.");
        }
        try {
            byte[] iv = new byte[GCM_IV_LENGTH];
            SecureRandom.getInstanceStrong().nextBytes(iv);
            Cipher cipher = Cipher.getInstance(TRANSFORMATION);
            cipher.init(Cipher.ENCRYPT_MODE, secretKey, new GCMParameterSpec(GCM_TAG_LENGTH * 8, iv));
            byte[] cipherText = cipher.doFinal(plaintext.getBytes(StandardCharsets.UTF_8));
            ByteBuffer buffer = ByteBuffer.allocate(iv.length + cipherText.length);
            buffer.put(iv);
            buffer.put(cipherText);
            return Base64.getEncoder().encodeToString(buffer.array());
        } catch (Exception e) {
            throw new IllegalStateException("Falha ao cifrar a senha do certificado: " + e.getMessage(), e);
        }
    }

    public String decrypt(String encrypted) {
        if (encrypted == null) {
            throw new IllegalArgumentException("Texto cifrado para decifrar não pode ser nulo.");
        }
        try {
            byte[] decoded = Base64.getDecoder().decode(encrypted);
            if (decoded.length < GCM_IV_LENGTH) {
                throw new IllegalArgumentException("Texto cifrado está corrompido: tamanho menor que o IV.");
            }
            ByteBuffer buffer = ByteBuffer.wrap(decoded);
            byte[] iv = new byte[GCM_IV_LENGTH];
            buffer.get(iv);
            byte[] cipherText = new byte[buffer.remaining()];
            buffer.get(cipherText);
            Cipher cipher = Cipher.getInstance(TRANSFORMATION);
            cipher.init(Cipher.DECRYPT_MODE, secretKey, new GCMParameterSpec(GCM_TAG_LENGTH * 8, iv));
            return new String(cipher.doFinal(cipherText), StandardCharsets.UTF_8);
        } catch (Exception e) {
            throw new IllegalStateException("Falha ao decifrar a senha do certificado: " + e.getMessage(), e);
        }
    }
}
