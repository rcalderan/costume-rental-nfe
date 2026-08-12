package br.com.costumerental.nfe.api;

import br.com.costumerental.nfe.api.dto.StatusServicoResponse;
import br.com.costumerental.nfe.application.NfeConsultationService;
import com.auth0.jwt.JWT;
import com.auth0.jwt.algorithms.Algorithm;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.HttpHeaders;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Date;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Confirma que o costume-nfe passa a exigir JWT proprio nos endpoints /api/v1/**,
 * sem depender do auth_request do nginx (ver plano de JWT nativo).
 */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class NfeConsultationControllerSecurityTest {

    private static final String SECRET = "test-jwt-secret-must-be-at-least-32-characters-long";
    private static final String ISSUER = "rentafit-api";

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private NfeConsultationService consultationService;

    @Test
    @DisplayName("Should return 401 when no token is provided")
    void statusServico_withoutToken() throws Exception {
        mockMvc.perform(get("/api/v1/nfe/status-servico"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("Should return 401 when token is invalid")
    void statusServico_withInvalidToken() throws Exception {
        mockMvc.perform(get("/api/v1/nfe/status-servico")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer invalid-token"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("Should return 200 when token is valid")
    void statusServico_withValidToken() throws Exception {
        when(consultationService.consultarStatusServico()).thenReturn(
                StatusServicoResponse.builder()
                        .ambiente("2")
                        .statusCode("107")
                        .statusMessage("Servico em Operacao")
                        .uf("SP")
                        .build()
        );

        mockMvc.perform(get("/api/v1/nfe/status-servico")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + validToken()))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("Should keep actuator health public without a token")
    void actuatorHealth_isPublic() throws Exception {
        mockMvc.perform(get("/actuator/health"))
                .andExpect(status().isOk());
    }

    private String validToken() {
        return JWT.create()
                .withIssuer(ISSUER)
                .withSubject("rentafit-user")
                .withExpiresAt(new Date(System.currentTimeMillis() + 60_000))
                .sign(Algorithm.HMAC256(SECRET));
    }
}
