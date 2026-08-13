package br.com.costumerental.nfe.repository;

import br.com.costumerental.nfe.domain.FiscalEventXml;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface FiscalEventXmlRepository extends JpaRepository<FiscalEventXml, Long> {
}
