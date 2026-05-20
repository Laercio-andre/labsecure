package ao.safelab.controller;

import ao.safelab.entity.Evento;
import ao.safelab.entity.Evento.ResultadoEvento;
import ao.safelab.repository.EventoRepository;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/eventos")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class EventoController {

    private final EventoRepository repo;

    // Todos os eventos
    @GetMapping
    public List<Evento> listar() {
        return repo.findAll();
    }

    // Apenas alarmes — para a lista de incidentes no dashboard
    @GetMapping("/alarmes")
    public List<Evento> alarmes() {
        return repo.findByResultadoOrderByOcorridoEmDesc(ResultadoEvento.ALARME);
    }

    // Resumo para o dashboard
    @GetMapping("/resumo")
    public Map<String, Object> resumo() {
        LocalDateTime inicio24h = LocalDateTime.now().minusHours(24);
        long alarmes24h = repo.contarAlarmes(inicio24h);
        long totalEventos = repo.count();

        return Map.of(
            "alarmes24h", alarmes24h,
            "totalEventos", totalEventos,
            "horaServidor", LocalDateTime.now().toString()
        );
    }
}
