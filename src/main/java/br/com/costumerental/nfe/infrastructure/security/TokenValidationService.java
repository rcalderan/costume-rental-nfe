package br.com.costumerental.nfe.infrastructure.security;

import com.auth0.jwt.JWT;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.exceptions.JWTVerificationException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

/**
 * Mirror simplificado do TokenService do Rentafit: aqui so validamos, nunca emitimos token
 * (este servico nunca faz login, so verifica o Bearer token emitido pelo Rentafit).
 */
@Service
public class TokenValidationService {

    @Value("${nfe.security.jwt.secret}")
    private String secret;

    @Value("${nfe.security.jwt.issuer:rentafit-api}")
    private String issuer;

    public String validateToken(String token) {
        try {
            Algorithm algorithm = Algorithm.HMAC256(secret);
            return JWT.require(algorithm)
                    .withIssuer(issuer)
                    .build()
                    .verify(token)
                    .getSubject();
        } catch (JWTVerificationException exception) {
            return null;
        }
    }
}
