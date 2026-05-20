package ao.safelab.controller;

import ao.safelab.dto.LeituraDTO;
import ao.safelab.entity.Evento;
import ao.safelab.service.LeituraService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.format.DateTimeFormatter;

/**
 * Endpoint chamado pelo leitor RFID sempre que detecta uma tag.
 * Este é o endpoint mais importante do sistema.
 */
@RestController
@RequestMapping("/api/leituras")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class LeituraController {

    private final LeituraService leituraService;

    /**
     * POST /api/leituras
     * Corpo: { "tagRfid": "E2003411B802011806D0C3C0", "portaId": "PORTA_LAB1" }
     */
    @PostMapping
    public ResponseEntity<LeituraDTO.Response> processarLeitura(
            @Valid @RequestBody LeituraDTO.Request request) {

        Evento evento = leituraService.processarLeitura(
            request.getTagRfid(),
            request.getPortaId()
        );

        LeituraDTO.Response response = new LeituraDTO.Response();
        response.setEventoId(evento.getId());
        response.setResultado(evento.getResultado().name());
        response.setTipo(evento.getTipo().name());
        response.setPortaId(evento.getPortaId());
        response.setOcorridoEm(
            evento.getOcorridoEm().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME)
        );

        // Mensagem legível para o frontend
        response.setMensagem(switch (evento.getResultado()) {
            case AUTORIZADO -> "✅ " + (evento.getObservacao() != null ? evento.getObservacao() : "Autorizado");
            case ALARME     -> "🚨 " + evento.getObservacao();
            case TAG_DESCONHECIDA -> "⚠️ Tag não reconhecida: " + request.getTagRfid();
        });

        return ResponseEntity.ok(response);
    }
}
