package br.com.costumerental.nfe.repository;

import br.com.costumerental.nfe.domain.NfeIssuer;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface NfeIssuerRepository extends JpaRepository<NfeIssuer, UUID> {
    Optional<NfeIssuer> findByFirmRootCnpjAndFirmBranchOrder(String rootCnpj, String branchOrder);
    List<NfeIssuer> findByFirmRootCnpj(String rootCnpj);
    Optional<NfeIssuer> findFirstByFirmRootCnpjAndFirmBranchOrderAndActiveTrue(String rootCnpj, String branchOrder);
    Optional<NfeIssuer> findFirstByActiveTrueOrderByFirmBranchOrderAsc();
    List<NfeIssuer> findByActiveTrueOrderByFirmBranchOrderAsc();
}
