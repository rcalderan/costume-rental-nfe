package br.com.costumerental.nfe;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

@SpringBootTest
@ActiveProfiles("test")
class CostumeRentalNfeApplicationTests {

    @Test
    void contextLoads() {
        // Verifica que todo o contexto Spring (incluindo JPA/H2 e os novos beans da Fase 2) sobe sem erros.
    }
}
