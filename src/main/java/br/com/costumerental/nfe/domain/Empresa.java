package br.com.costumerental.nfe.domain;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.PrePersist;
import javax.persistence.PreUpdate;
import javax.persistence.Table;
import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(name = "empresa")
public class Empresa {

    @Id
    @Column(length = 8, nullable = false)
    private String rootCnpj;

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
}
