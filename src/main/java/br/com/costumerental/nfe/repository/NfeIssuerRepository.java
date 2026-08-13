package br.com.costumerental.nfe.repository;

import br.com.costumerental.nfe.domain.NfeIssuer;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface NfeIssuerRepository extends JpaRepository<NfeIssuer, Long> {
    Optional<NfeIssuer> findByEmpresaRootCnpjAndBranchOrder(String empresaRootCnpj, String branchOrder);
    List<NfeIssuer> findByEmpresaRootCnpj(String empresaRootCnpj);
    Optional<NfeIssuer> findFirstByEmpresaRootCnpjAndBranchOrderAndActiveTrue(String empresaRootCnpj, String branchOrder);
    Optional<NfeIssuer> findFirstByActiveTrueOrderByBranchOrderAsc();
    List<NfeIssuer> findByActiveTrueOrderByBranchOrderAsc();
}
