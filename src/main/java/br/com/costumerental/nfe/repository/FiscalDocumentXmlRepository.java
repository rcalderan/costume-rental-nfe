package br.com.costumerental.nfe.repository;

import br.com.costumerental.nfe.domain.FiscalDocumentXml;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface FiscalDocumentXmlRepository extends JpaRepository<FiscalDocumentXml, Long> {
}
