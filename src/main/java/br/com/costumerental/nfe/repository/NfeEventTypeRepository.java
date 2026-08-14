package br.com.costumerental.nfe.repository;

import br.com.costumerental.nfe.domain.NfeEventTypeEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface NfeEventTypeRepository extends JpaRepository<NfeEventTypeEntity, Short> {
    Optional<NfeEventTypeEntity> findByCode(String code);
}
