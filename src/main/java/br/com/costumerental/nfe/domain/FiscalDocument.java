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
@Table(name = "fiscal_document")
public class FiscalDocument {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 44)
    private String accessKey;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "issuer_id", nullable = false)
    private NfeIssuer issuer;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "document_type_id", nullable = false)
    private NfeDocumentTypeEntity documentType;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "status_id", nullable = false)
    private NfeStatusEntity status;

    @Column(name = "sefaz_status_code", length = 4)
    private String sefazStatusCode;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "signed_xml_id")
    private FiscalDocumentXml signedXml;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "authorized_xml_id")
    private FiscalDocumentXml authorizedXml;

    private LocalDateTime issueDate;

    private String protocol;

    private String originId;

    private String origin;

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
