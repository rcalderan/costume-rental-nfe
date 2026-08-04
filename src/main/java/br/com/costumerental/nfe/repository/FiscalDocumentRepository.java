package br.com.costumerental.nfe.repository;

import br.com.costumerental.nfe.domain.FiscalDocument;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface FiscalDocumentRepository extends JpaRepository<FiscalDocument, Long> {

    Optional<FiscalDocument> findByAccessKey(String accessKey);
}
