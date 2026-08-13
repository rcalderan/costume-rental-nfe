package br.com.costumerental.nfe.repository;

import br.com.costumerental.nfe.domain.FiscalDocumentNumberControl;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface FiscalDocumentNumberControlRepository
        extends JpaRepository<FiscalDocumentNumberControl, FiscalDocumentNumberControl.PK> {

    @Query("UPDATE FiscalDocumentNumberControl c SET c.lastNumber = c.lastNumber + 1 " +
            "WHERE c.issuerId = :issuerId AND c.series = :series")
    void incrementLastNumber(@Param("issuerId") Long issuerId, @Param("series") String series);
}
