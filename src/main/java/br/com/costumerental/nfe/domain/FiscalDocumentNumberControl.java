package br.com.costumerental.nfe.domain;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.IdClass;
import javax.persistence.Table;
import java.io.Serializable;
import java.util.Objects;

@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(name = "fiscal_document_number_control")
@IdClass(FiscalDocumentNumberControl.PK.class)
public class FiscalDocumentNumberControl {

    @Id
    @Column(name = "issuer_id", nullable = false)
    private Long issuerId;

    @Id
    @Column(name = "series", nullable = false, length = 3)
    private String series;

    @Column(name = "last_number", nullable = false)
    private Long lastNumber;

    public FiscalDocumentNumberControl(Long issuerId, String series, Long lastNumber) {
        this.issuerId = issuerId;
        this.series = series;
        this.lastNumber = lastNumber;
    }

    @Getter
    @Setter
    @NoArgsConstructor
    public static class PK implements Serializable {
        private Long issuerId;
        private String series;

        public PK(Long issuerId, String series) {
            this.issuerId = issuerId;
            this.series = series;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (!(o instanceof PK pk)) return false;
            return Objects.equals(issuerId, pk.issuerId) && Objects.equals(series, pk.series);
        }

        @Override
        public int hashCode() {
            return Objects.hash(issuerId, series);
        }
    }
}
