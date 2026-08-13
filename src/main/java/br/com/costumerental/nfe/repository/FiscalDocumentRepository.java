package br.com.costumerental.nfe.repository;

import br.com.costumerental.nfe.domain.FiscalDocument;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface FiscalDocumentRepository extends JpaRepository<FiscalDocument, Long> {

    Optional<FiscalDocument> findByAccessKey(String accessKey);

    List<FiscalDocument> findByIssuerIdAndStatusId(UUID issuerId, Short statusId);
}
