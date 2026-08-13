package br.com.costumerental.nfe.domain;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;
import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(name = "sefaz_status")
public class SefazStatus {

    @Id
    @Column(nullable = false, length = 3)
    private String code;

    @Column(length = 500)
    private String message;

    @Column(length = 20)
    private String category;

    @Column(name = "first_seen_at", nullable = false)
    private LocalDateTime firstSeenAt;

    public SefazStatus(String code, String message, String category) {
        this.code = code;
        this.message = message;
        this.category = category;
        this.firstSeenAt = LocalDateTime.now();
    }
}
