package br.com.costumerental.nfe.repository;

import br.com.costumerental.nfe.domain.NfeStatusEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface NfeStatusRepository extends JpaRepository<NfeStatusEntity, Short> {
    Optional<NfeStatusEntity> findByCode(String code);
}
