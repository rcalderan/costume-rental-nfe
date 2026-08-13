package br.com.costumerental.nfe.repository;

import br.com.costumerental.nfe.domain.SefazStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface SefazStatusRepository extends JpaRepository<SefazStatus, String> {
}
