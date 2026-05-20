package ao.safelab.entity;

import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@Entity
@Table(name = "eventos")
public class Evento {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String tagLida;           // Tag que o leitor detectou

    @Column(nullable = false)
    private String portaId;           // Identificador do leitor/porta (ex: "PORTA_LAB1")

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private TipoEvento tipo;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ResultadoEvento resultado;

    // Referências opcionais — preenchidas quando a tag é reconhecida
    @ManyToOne
    @JoinColumn(name = "equipamento_id")
    private Equipamento equipamento;

    @ManyToOne
    @JoinColumn(name = "funcionario_id")
    private Funcionario funcionario;  // Funcionário associado à saída (se autorizada)

    private String observacao;        // Detalhe adicional ou motivo do alerta

    @Column(nullable = false)
    private LocalDateTime ocorridoEm = LocalDateTime.now();

    public enum TipoEvento {
        LEITURA_FUNCIONARIO,          // Cartão de funcionário apresentado ao leitor
        SAIDA_EQUIPAMENTO             // Tag de equipamento detectada na porta
    }

    public enum ResultadoEvento {
        AUTORIZADO,
        ALARME,
        TAG_DESCONHECIDA
    }
}
