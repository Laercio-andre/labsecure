package ao.safelab.repository;

import ao.safelab.entity.Equipamento;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface EquipamentoRepository extends JpaRepository<Equipamento, Long> {
    Optional<Equipamento> findByTagRfid(String tagRfid);
    boolean existsByTagRfid(String tagRfid);
}
