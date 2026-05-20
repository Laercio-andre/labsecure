package ao.safelab.entity;

import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@Entity
@Table(name = "equipamentos")
public class Equipamento {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String tagRfid;           // ID único gravado na tag RFID colada no equipamento

    @Column(nullable = false)
    private String nome;              // Ex: "Laptop Dell Inspiron 15"

    @Column(nullable = false)
    private String numeroSerie;

    private String descricao;

    @Column(nullable = false)
    private String laboratorio;       // Ex: "Lab Informática 1"

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private EstadoEquipamento estado = EstadoEquipamento.NO_LABORATORIO;

    @Column(nullable = false, updatable = false)
    private LocalDateTime criadoEm = LocalDateTime.now();

    private LocalDateTime atualizadoEm;

    @PreUpdate
    public void preUpdate() {
        this.atualizadoEm = LocalDateTime.now();
    }

    public enum EstadoEquipamento {
        NO_LABORATORIO,
        FORA_DO_LABORATORIO,
        EM_MANUTENCAO
    }
}
