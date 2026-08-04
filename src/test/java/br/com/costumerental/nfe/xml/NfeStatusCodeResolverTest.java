package br.com.costumerental.nfe.xml;

import br.com.costumerental.nfe.domain.NfeStatus;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class NfeStatusCodeResolverTest {

    private final NfeStatusCodeResolver resolver = new NfeStatusCodeResolver();

    @Test
    void shouldResolveAuthorizedCodes() {
        assertThat(resolver.resolve("100")).isEqualTo(NfeStatus.AUTHORIZED);
        assertThat(resolver.resolve("150")).isEqualTo(NfeStatus.AUTHORIZED);
        assertThat(resolver.isAuthorized("100")).isTrue();
    }

    @Test
    void shouldResolveProcessingCodes() {
        assertThat(resolver.resolve("103")).isEqualTo(NfeStatus.PROCESSING);
        assertThat(resolver.resolve("105")).isEqualTo(NfeStatus.PROCESSING);
    }

    @Test
    void shouldResolveRejectedForOtherCodes() {
        assertThat(resolver.resolve("217")).isEqualTo(NfeStatus.REJECTED);
        assertThat(resolver.isAuthorized("217")).isFalse();
    }

    @Test
    void shouldResolveErrorWhenCodeIsNull() {
        assertThat(resolver.resolve(null)).isEqualTo(NfeStatus.ERROR);
    }
}
