package br.com.costumerental.nfe.repository;

import br.com.costumerental.nfe.domain.NfeDocumentTypeEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface NfeDocumentTypeRepository extends JpaRepository<NfeDocumentTypeEntity, Short> {
    Optional<NfeDocumentTypeEntity> findByCode(String code);
}
