package br.com.costumerental.nfe.repository;

import br.com.costumerental.nfe.domain.NfeIssuer;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface NfeIssuerRepository extends JpaRepository<NfeIssuer, String> {
    Optional<NfeIssuer> findFirstByActiveTrue();
}
