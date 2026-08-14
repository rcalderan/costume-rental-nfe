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
import javax.persistence.Table;
import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(name = "fiscal_document_xml")
public class FiscalDocumentXml {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 20)
    private String xmlType;

    @Lob
    private String content;

    @Column(nullable = false)
    private LocalDateTime createdAt;

    public FiscalDocumentXml(String xmlType, String content) {
        this.xmlType = xmlType;
        this.content = content;
        this.createdAt = LocalDateTime.now();
    }
}
