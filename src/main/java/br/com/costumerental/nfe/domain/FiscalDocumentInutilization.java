package br.com.costumerental.nfe.domain;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Lob;
import javax.persistence.PrePersist;
import javax.persistence.PreUpdate;
import javax.persistence.Table;
import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(name = "fiscal_document_inutilization")
public class FiscalDocumentInutilization {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "inutilization_year", nullable = false, length = 2)
    private String year;

    @Column(nullable = false, length = 14)
    private String cnpj;

    @Column(nullable = false, length = 2)
    private String model;

    @Column(nullable = false, length = 3)
    private String series;

    @Column(nullable = false, length = 9)
    private String initialNumber;

    @Column(nullable = false, length = 9)
    private String finalNumber;

    @Column(nullable = false, length = 255)
    private String justification;

    @Column(length = 3)
    private String statusCode;

    @Column(length = 500)
    private String statusMessage;

    @Column(length = 20)
    private String protocol;

    @Lob
    private String responseXml;

    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(nullable = false)
    private LocalDateTime updatedAt;

    @PrePersist
    void onCreate() {
        LocalDateTime now = LocalDateTime.now();
        createdAt = now;
        updatedAt = now;
    }

    @PreUpdate
    void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
}
