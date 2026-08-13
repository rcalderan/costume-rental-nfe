package br.com.costumerental.nfe.repository;

import br.com.costumerental.nfe.domain.Empresa;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface EmpresaRepository extends JpaRepository<Empresa, String> {
    boolean existsByRootCnpj(String rootCnpj);
}
