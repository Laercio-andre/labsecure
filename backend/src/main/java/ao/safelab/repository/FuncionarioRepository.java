package ao.safelab.repository;

import ao.safelab.entity.Funcionario;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface FuncionarioRepository extends JpaRepository<Funcionario, Long> {
    Optional<Funcionario> findByTagRfid(String tagRfid);
    boolean existsByTagRfid(String tagRfid);
}
