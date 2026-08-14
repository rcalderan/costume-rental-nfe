package br.com.costumerental.nfe.domain;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.OneToOne;
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

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "issuer_id", nullable = false)
    private NfeIssuer issuer;

    @Column(name = "inutilization_year", nullable = false, length = 2)
    private String year;

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

    @Column(name = "sefaz_status_code", length = 3)
    private String sefazStatusCode;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "response_xml_id")
    private FiscalEventXml responseXml;

    @Column(length = 20)
    private String protocol;

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
