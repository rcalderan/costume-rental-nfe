package br.com.costumerental.nfe.infrastructure.security;

import com.auth0.jwt.JWT;
import com.auth0.jwt.algorithms.Algorithm;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.Date;

import static org.assertj.core.api.Assertions.assertThat;

class TokenValidationServiceTest {

    private static final String SECRET = "test-secret-key-for-unit-testing-32-chars";
    private static final String ISSUER = "rentafit-api-test";

    private TokenValidationService tokenValidationService;

    @BeforeEach
    void setUp() {
        tokenValidationService = new TokenValidationService();
        ReflectionTestUtils.setField(tokenValidationService, "secret", SECRET);
        ReflectionTestUtils.setField(tokenValidationService, "issuer", ISSUER);
    }

    @Test
    @DisplayName("Should validate a token signed with the same secret and issuer")
    void validateToken_valid() {
        String token = JWT.create()
                .withIssuer(ISSUER)
                .withSubject("rentafit-user")
                .withExpiresAt(new Date(System.currentTimeMillis() + 60_000))
                .sign(Algorithm.HMAC256(SECRET));

        String subject = tokenValidationService.validateToken(token);

        assertThat(subject).isEqualTo("rentafit-user");
    }

    @Test
    @DisplayName("Should reject a token with a different issuer")
    void validateToken_wrongIssuer() {
        String token = JWT.create()
                .withIssuer("outro-issuer")
                .withSubject("rentafit-user")
                .withExpiresAt(new Date(System.currentTimeMillis() + 60_000))
                .sign(Algorithm.HMAC256(SECRET));

        String subject = tokenValidationService.validateToken(token);

        assertThat(subject).isNull();
    }

    @Test
    @DisplayName("Should reject an expired token")
    void validateToken_expired() {
        String token = JWT.create()
                .withIssuer(ISSUER)
                .withSubject("rentafit-user")
                .withExpiresAt(new Date(System.currentTimeMillis() - 60_000))
                .sign(Algorithm.HMAC256(SECRET));

        String subject = tokenValidationService.validateToken(token);

        assertThat(subject).isNull();
    }

    @Test
    @DisplayName("Should reject a token signed with a different secret")
    void validateToken_wrongSecret() {
        String token = JWT.create()
                .withIssuer(ISSUER)
                .withSubject("rentafit-user")
                .withExpiresAt(new Date(System.currentTimeMillis() + 60_000))
                .sign(Algorithm.HMAC256("outro-secret-completamente-diferente-32-chars"));

        String subject = tokenValidationService.validateToken(token);

        assertThat(subject).isNull();
    }

    @Test
    @DisplayName("Should reject a malformed token")
    void validateToken_malformed() {
        String subject = tokenValidationService.validateToken("token-invalido");

        assertThat(subject).isNull();
    }
}
