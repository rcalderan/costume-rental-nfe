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
import javax.persistence.Transient;
import java.time.LocalDateTime;
import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(name = "nfe_issuer")
public class NfeIssuer {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Column(updatable = false, nullable = false)
    private UUID id;

    @OneToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "firm_id", nullable = false, unique = true)
    private Firm firm;

    @Column(length = 255)
    private String nomeFantasia;

    @Column(length = 20)
    private String ie;

    @Column(length = 20)
    private String im;

    @Column(length = 20)
    private String fone;

    @Column(nullable = false, length = 255)
    private String logradouro;

    @Column(nullable = false, length = 255)
    private String numero;

    @Column(nullable = false, length = 255)
    private String bairro;

    @Column(nullable = false, length = 255)
    private String municipioCodigo;

    @Column(nullable = false, length = 255)
    private String municipioNome;

    @Column(nullable = false, length = 2)
    private String uf;

    @Column(nullable = false, length = 8)
    private String cep;

    @Column(length = 500)
    private String certificatePath;

    @Column(length = 500)
    private String encryptedPassword;

    @Column(length = 10)
    private String certificateTipo;

    @Column(nullable = false)
    private boolean active = true;

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

    @Transient
    public String getCnpj() {
        return firm.getCnpj();
    }

    @Transient
    public String getRootCnpj() {
        return firm.getRootCnpj();
    }

    @Transient
    public String getBranchOrder() {
        return firm.getBranchOrder();
    }

    @Transient
    public String getDigitoControle() {
        return firm.getDigit();
    }

    @Transient
    public String getRazaoSocial() {
        return firm.getRazaoSocial();
    }

    @Transient
    public String getCrt() {
        return firm.getCrt();
    }

    @Transient
    public String getPaisCodigo() {
        return firm.getPaisCodigo();
    }

    @Transient
    public String getPaisNome() {
        return firm.getPaisNome();
    }

    @Transient
    public boolean isMatriz() {
        return firm.isMatriz();
    }
}
