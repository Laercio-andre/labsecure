package ao.safelab.repository;

import ao.safelab.entity.Evento;
import ao.safelab.entity.Evento.ResultadoEvento;
import ao.safelab.entity.Evento.TipoEvento;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface EventoRepository extends JpaRepository<Evento, Long> {

    // Busca o evento de funcionário mais recente numa porta específica dentro da janela de tempo
    @Query("""
        SELECT e FROM Evento e
        WHERE e.portaId = :portaId
          AND e.tipo = :tipo
          AND e.ocorridoEm >= :desde
        ORDER BY e.ocorridoEm DESC
        LIMIT 1
    """)
    Optional<Evento> findUltimoEventoPorPortaETipo(
        @Param("portaId") String portaId,
        @Param("tipo") TipoEvento tipo,
        @Param("desde") LocalDateTime desde
    );

    // Todos os alarmes ordenados do mais recente
    List<Evento> findByResultadoOrderByOcorridoEmDesc(ResultadoEvento resultado);

    // Eventos de uma porta específica
    List<Evento> findByPortaIdOrderByOcorridoEmDesc(String portaId);

    // Contagem de alarmes nas últimas 24h (para o dashboard)
    @Query("""
        SELECT COUNT(e) FROM Evento e
        WHERE e.resultado = 'ALARME'
          AND e.ocorridoEm >= :desde
    """)
    long contarAlarmes(@Param("desde") LocalDateTime desde);
}
