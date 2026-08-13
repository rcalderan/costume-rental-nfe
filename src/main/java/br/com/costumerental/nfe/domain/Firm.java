package br.com.costumerental.nfe.domain;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.PrePersist;
import javax.persistence.PreUpdate;
import javax.persistence.Table;
import java.time.LocalDateTime;
import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(name = "firm")
public class Firm {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Column(updatable = false, nullable = false)
    private UUID id;

    @Column(nullable = false, length = 8)
    private String rootCnpj;

    @Column(nullable = false, length = 4)
    private String branchOrder;

    @Column(nullable = false, length = 2)
    private String digit;

    @Column(nullable = false, length = 255)
    private String razaoSocial;

    @Column(nullable = false, length = 1)
    private String crt;

    @Column(nullable = false, length = 255)
    private String paisCodigo;

    @Column(nullable = false, length = 255)
    private String paisNome;

    @Column(length = 14)
    private String matrizCnpj;

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

    public String getCnpj() {
        return rootCnpj + branchOrder + digit;
    }

    public boolean isMatriz() {
        return "0001".equals(branchOrder);
    }
}
