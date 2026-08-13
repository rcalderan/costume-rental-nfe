package br.com.costumerental.nfe.repository;

import br.com.costumerental.nfe.domain.Firm;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface FirmRepository extends JpaRepository<Firm, UUID> {
    Optional<Firm> findByRootCnpjAndBranchOrder(String rootCnpj, String branchOrder);
    List<Firm> findByRootCnpj(String rootCnpj);
    boolean existsByRootCnpj(String rootCnpj);
}
