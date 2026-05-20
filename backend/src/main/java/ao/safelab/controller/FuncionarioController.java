package ao.safelab.controller;

import ao.safelab.entity.Funcionario;
import ao.safelab.repository.FuncionarioRepository;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@RestController
@RequestMapping("/api/funcionarios")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class FuncionarioController {

    private final FuncionarioRepository repo;

    @GetMapping
    public List<Funcionario> listar() {
        return repo.findAll();
    }

    @PostMapping
    public ResponseEntity<Funcionario> criar(@Valid @RequestBody FuncionarioRequest dto) {
        if (repo.existsByTagRfid(dto.getTagRfid())) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Tag RFID já registada");
        }
        Funcionario f = new Funcionario();
        f.setTagRfid(dto.getTagRfid());
        f.setNome(dto.getNome());
        f.setEmail(dto.getEmail());
        f.setTelefone(dto.getTelefone());
        f.setCargo(dto.getCargo());
        return ResponseEntity.status(HttpStatus.CREATED).body(repo.save(f));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> remover(@PathVariable Long id) {
        if (!repo.existsById(id)) throw new ResponseStatusException(HttpStatus.NOT_FOUND);
        repo.deleteById(id);
        return ResponseEntity.noContent().build();
    }

    @Data
    public static class FuncionarioRequest {
        @NotBlank private String tagRfid;
        @NotBlank private String nome;
        @NotBlank private String email;
        private String telefone;
        @NotBlank private String cargo;
    }
}
