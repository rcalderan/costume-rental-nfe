package br.com.costumerental.nfe.infrastructure.config;

import br.com.costumerental.nfe.config.NfeProperties;
import br.com.costumerental.nfe.domain.Environment;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.stereotype.Component;

import java.util.Set;

@Component
public class ProductionEnvironmentGuard implements InitializingBean {

    private static final Set<String> NON_PRODUCTION_PROFILES = Set.of("local", "test");

    private final NfeProperties properties;
    private final org.springframework.core.env.Environment springEnvironment;

    public ProductionEnvironmentGuard(NfeProperties properties, org.springframework.core.env.Environment springEnvironment) {
        this.properties = properties;
        this.springEnvironment = springEnvironment;
    }

    @Override
    public void afterPropertiesSet() {
        boolean isProductionAmbiente = Environment.fromCode(properties.getAmbiente()) == Environment.PRODUCTION;
        boolean isNonProductionProfile = Set.of(springEnvironment.getActiveProfiles()).stream()
                .anyMatch(NON_PRODUCTION_PROFILES::contains);
        if (isProductionAmbiente && isNonProductionProfile) {
            throw new IllegalStateException(
                    "NFE_AMBIENTE=1 (producao) nao e permitido nos profiles " + NON_PRODUCTION_PROFILES
                            + ". Use o profile 'prod' para emitir NF-e em producao.");
        }
    }
}
