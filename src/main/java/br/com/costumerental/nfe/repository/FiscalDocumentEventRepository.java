package br.com.costumerental.nfe.repository;

import br.com.costumerental.nfe.domain.FiscalDocumentEvent;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface FiscalDocumentEventRepository extends JpaRepository<FiscalDocumentEvent, Long> {

    List<FiscalDocumentEvent> findByAccessKeyOrderBySequenceDesc(String accessKey);
}
