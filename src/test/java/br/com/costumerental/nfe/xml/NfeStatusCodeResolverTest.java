package br.com.costumerental.nfe.xml;

import br.com.costumerental.nfe.domain.NfeStatus;
import br.com.costumerental.nfe.domain.SefazStatus;
import br.com.costumerental.nfe.repository.SefazStatusRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class NfeStatusCodeResolverTest {

    @Mock
    private SefazStatusRepository sefazStatusRepository;

    private NfeStatusCodeResolver resolver;

    @BeforeEach
    void setUp() {
        resolver = new NfeStatusCodeResolver(sefazStatusRepository);
    }

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

    @Test
    void shouldUpsertSefazStatusWhenCodeNotExists() {
        when(sefazStatusRepository.existsById("999")).thenReturn(false);

        resolver.upsertSefazStatus("999", "Novo status");

        verify(sefazStatusRepository).save(any(SefazStatus.class));
    }

    @Test
    void shouldNotUpsertSefazStatusWhenCodeAlreadyExists() {
        when(sefazStatusRepository.existsById("100")).thenReturn(true);

        resolver.upsertSefazStatus("100", "Autorizado");

        verify(sefazStatusRepository, never()).save(any());
    }

    @Test
    void shouldNotUpsertWhenCodeIsNull() {
        resolver.upsertSefazStatus(null, "mensagem");

        verify(sefazStatusRepository, never()).existsById(any());
        verify(sefazStatusRepository, never()).save(any());
    }
}
