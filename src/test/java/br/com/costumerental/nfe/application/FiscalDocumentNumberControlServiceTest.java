package br.com.costumerental.nfe.application;

import br.com.costumerental.nfe.domain.FiscalDocumentNumberControl;
import br.com.costumerental.nfe.repository.FiscalDocumentNumberControlRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class FiscalDocumentNumberControlServiceTest {

    @Mock
    private FiscalDocumentNumberControlRepository repository;

    private FiscalDocumentNumberControlService service;

    @BeforeEach
    void setUp() {
        service = new FiscalDocumentNumberControlService(repository);
    }

    @Test
    void shouldCreateControlWhenNotExistsAndReturnFirstNumber() {
        UUID issuerId = UUID.randomUUID();
        FiscalDocumentNumberControl.PK pk = new FiscalDocumentNumberControl.PK(issuerId, "1");
        when(repository.findById(pk)).thenReturn(Optional.empty());
        when(repository.save(any(FiscalDocumentNumberControl.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        String number = service.reserveNextNumber(issuerId, "1");

        assertThat(number).isEqualTo("1");
        verify(repository, times(2)).save(any(FiscalDocumentNumberControl.class));
    }

    @Test
    void shouldIncrementExistingControlAndReturnNextNumber() {
        UUID issuerId = UUID.randomUUID();
        FiscalDocumentNumberControl.PK pk = new FiscalDocumentNumberControl.PK(issuerId, "1");
        FiscalDocumentNumberControl existing = new FiscalDocumentNumberControl(issuerId, "1", 5L);
        when(repository.findById(pk)).thenReturn(Optional.of(existing));
        when(repository.save(any(FiscalDocumentNumberControl.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        String number = service.reserveNextNumber(issuerId, "1");

        assertThat(number).isEqualTo("6");
        assertThat(existing.getLastNumber()).isEqualTo(6L);
    }

    @Test
    void shouldReturnSequentialNumbersAcrossCalls() {
        UUID issuerId = UUID.randomUUID();
        FiscalDocumentNumberControl.PK pk = new FiscalDocumentNumberControl.PK(issuerId, "001");
        FiscalDocumentNumberControl existing = new FiscalDocumentNumberControl(issuerId, "001", 99L);
        when(repository.findById(pk)).thenReturn(Optional.of(existing));
        when(repository.save(any(FiscalDocumentNumberControl.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        String first = service.reserveNextNumber(issuerId, "001");
        String second = service.reserveNextNumber(issuerId, "001");

        assertThat(first).isEqualTo("100");
        assertThat(second).isEqualTo("101");
    }
}
