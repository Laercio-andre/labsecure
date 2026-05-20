package ao.safelab.controller;

import ao.safelab.dto.EquipamentoDTO;
import ao.safelab.entity.Equipamento;
import ao.safelab.repository.EquipamentoRepository;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.time.format.DateTimeFormatter;
import java.util.List;

@RestController
@RequestMapping("/api/equipamentos")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class EquipamentoController {

    private final EquipamentoRepository repo;

    // GET /api/equipamentos — listar todos
    @GetMapping
    public List<EquipamentoDTO.Response> listar() {
        return repo.findAll().stream().map(this::toResponse).toList();
    }

    // GET /api/equipamentos/{id}
    @GetMapping("/{id}")
    public EquipamentoDTO.Response buscar(@PathVariable Long id) {
        return repo.findById(id)
            .map(this::toResponse)
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Equipamento não encontrado"));
    }

    // POST /api/equipamentos — registar novo equipamento
    @PostMapping
    public ResponseEntity<EquipamentoDTO.Response> criar(@Valid @RequestBody EquipamentoDTO.Request dto) {
        if (repo.existsByTagRfid(dto.getTagRfid())) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Tag RFID já registada");
        }

        Equipamento eq = new Equipamento();
        eq.setTagRfid(dto.getTagRfid());
        eq.setNome(dto.getNome());
        eq.setNumeroSerie(dto.getNumeroSerie());
        eq.setDescricao(dto.getDescricao());
        eq.setLaboratorio(dto.getLaboratorio());

        return ResponseEntity.status(HttpStatus.CREATED).body(toResponse(repo.save(eq)));
    }

    // PUT /api/equipamentos/{id}/estado — actualizar estado manualmente
    @PutMapping("/{id}/estado")
    public EquipamentoDTO.Response actualizarEstado(
            @PathVariable Long id,
            @RequestParam String estado) {
        Equipamento eq = repo.findById(id)
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Equipamento não encontrado"));
        eq.setEstado(Equipamento.EstadoEquipamento.valueOf(estado));
        return toResponse(repo.save(eq));
    }

    // DELETE /api/equipamentos/{id}
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> remover(@PathVariable Long id) {
        if (!repo.existsById(id)) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND);
        }
        repo.deleteById(id);
        return ResponseEntity.noContent().build();
    }

    private EquipamentoDTO.Response toResponse(Equipamento eq) {
        EquipamentoDTO.Response r = new EquipamentoDTO.Response();
        r.setId(eq.getId());
        r.setTagRfid(eq.getTagRfid());
        r.setNome(eq.getNome());
        r.setNumeroSerie(eq.getNumeroSerie());
        r.setDescricao(eq.getDescricao());
        r.setLaboratorio(eq.getLaboratorio());
        r.setEstado(eq.getEstado().name());
        r.setCriadoEm(eq.getCriadoEm().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME));
        return r;
    }
}
