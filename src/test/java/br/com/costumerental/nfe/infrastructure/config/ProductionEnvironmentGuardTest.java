package br.com.costumerental.nfe.infrastructure.config;

import br.com.costumerental.nfe.config.NfeProperties;
import org.junit.jupiter.api.Test;
import org.springframework.mock.env.MockEnvironment;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.assertj.core.api.Assertions.assertThatCode;

class ProductionEnvironmentGuardTest {

    @Test
    void shouldRejectProductionAmbienteOnLocalProfile() {
        NfeProperties properties = propertiesWithAmbiente("1");
        MockEnvironment environment = environmentWithProfile("local");

        assertThatThrownBy(() -> new ProductionEnvironmentGuard(properties, environment).afterPropertiesSet())
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("NFE_AMBIENTE=1");
    }

    @Test
    void shouldAllowProductionAmbienteOnProdProfile() {
        NfeProperties properties = propertiesWithAmbiente("1");
        MockEnvironment environment = environmentWithProfile("prod");

        assertThatCode(() -> new ProductionEnvironmentGuard(properties, environment).afterPropertiesSet())
                .doesNotThrowAnyException();
    }

    @Test
    void shouldAllowHomologationAmbienteOnLocalProfile() {
        NfeProperties properties = propertiesWithAmbiente("2");
        MockEnvironment environment = environmentWithProfile("local");

        assertThatCode(() -> new ProductionEnvironmentGuard(properties, environment).afterPropertiesSet())
                .doesNotThrowAnyException();
    }

    private NfeProperties propertiesWithAmbiente(String ambiente) {
        NfeProperties properties = new NfeProperties();
        properties.setAmbiente(ambiente);
        return properties;
    }

    private MockEnvironment environmentWithProfile(String profile) {
        MockEnvironment environment = new MockEnvironment();
        environment.setActiveProfiles(profile);
        return environment;
    }
}
