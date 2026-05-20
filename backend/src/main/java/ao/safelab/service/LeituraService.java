package ao.safelab.service;

import ao.safelab.entity.Equipamento;
import ao.safelab.entity.Evento;
import ao.safelab.entity.Evento.ResultadoEvento;
import ao.safelab.entity.Evento.TipoEvento;
import ao.safelab.entity.Funcionario;
import ao.safelab.repository.EquipamentoRepository;
import ao.safelab.repository.EventoRepository;
import ao.safelab.repository.FuncionarioRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
public class LeituraService {

    private final EquipamentoRepository equipamentoRepo;
    private final FuncionarioRepository funcionarioRepo;
    private final EventoRepository eventoRepo;
    private final AlertaService alertaService;

    @Value("${safelab.auth-window-seconds:10}")
    private int authWindowSeconds;

    /**
     * Ponto de entrada principal.
     * Chamado sempre que o leitor detecta qualquer tag RFID.
     *
     * @param tagRfid  ID da tag lida
     * @param portaId  Identificador do leitor (ex: "PORTA_LAB1")
     * @return Evento registado com o resultado
     */
    @Transactional
    public Evento processarLeitura(String tagRfid, String portaId) {
        log.info("Tag detectada: {} na porta: {}", tagRfid, portaId);

        // 1. Verificar se é cartão de funcionário
        Optional<Funcionario> funcionario = funcionarioRepo.findByTagRfid(tagRfid);
        if (funcionario.isPresent()) {
            return registarLeituraFuncionario(tagRfid, portaId, funcionario.get());
        }

        // 2. Verificar se é tag de equipamento
        Optional<Equipamento> equipamento = equipamentoRepo.findByTagRfid(tagRfid);
        if (equipamento.isPresent()) {
            return processarSaidaEquipamento(tagRfid, portaId, equipamento.get());
        }

        // 3. Tag completamente desconhecida
        return registarTagDesconhecida(tagRfid, portaId);
    }

    // ── Leitura de cartão de funcionário ──────────────────────────────────────
    private Evento registarLeituraFuncionario(String tagRfid, String portaId, Funcionario funcionario) {
        Evento evento = new Evento();
        evento.setTagLida(tagRfid);
        evento.setPortaId(portaId);
        evento.setTipo(TipoEvento.LEITURA_FUNCIONARIO);
        evento.setResultado(ResultadoEvento.AUTORIZADO);
        evento.setFuncionario(funcionario);
        evento.setObservacao("Cartão de " + funcionario.getNome() + " registado. Janela de autorização aberta.");

        Evento guardado = eventoRepo.save(evento);
        log.info("Funcionário {} identificado na porta {}", funcionario.getNome(), portaId);
        return guardado;
    }

    // ── Saída de equipamento ──────────────────────────────────────────────────
    private Evento processarSaidaEquipamento(String tagRfid, String portaId, Equipamento equipamento) {
        Evento evento = new Evento();
        evento.setTagLida(tagRfid);
        evento.setPortaId(portaId);
        evento.setTipo(TipoEvento.SAIDA_EQUIPAMENTO);
        evento.setEquipamento(equipamento);

        // Verificar se há leitura de funcionário recente nesta mesma porta
        LocalDateTime janelaInicio = LocalDateTime.now().minusSeconds(authWindowSeconds);
        Optional<Evento> leituraFuncionario = eventoRepo.findUltimoEventoPorPortaETipo(
            portaId, TipoEvento.LEITURA_FUNCIONARIO, janelaInicio
        );

        if (leituraFuncionario.isPresent()) {
            // SAÍDA AUTORIZADA — funcionário apresentou o cartão nos últimos N segundos
            Funcionario funcionario = leituraFuncionario.get().getFuncionario();
            evento.setResultado(ResultadoEvento.AUTORIZADO);
            evento.setFuncionario(funcionario);
            evento.setObservacao("Saída autorizada por " + funcionario.getNome());

            equipamento.setEstado(Equipamento.EstadoEquipamento.FORA_DO_LABORATORIO);
            equipamentoRepo.save(equipamento);

            log.info("Saída AUTORIZADA: {} por {}", equipamento.getNome(), funcionario.getNome());
        } else {
            // ALARME — nenhum funcionário identificado na janela de tempo
            evento.setResultado(ResultadoEvento.ALARME);
            evento.setObservacao("Saída sem identificação de funcionário nos últimos " + authWindowSeconds + "s");

            alertaService.enviarAlarme(evento, equipamento);
            log.warn("ALARME: Saída não autorizada de {} na porta {}", equipamento.getNome(), portaId);
        }

        return eventoRepo.save(evento);
    }

    // ── Tag desconhecida ──────────────────────────────────────────────────────
    private Evento registarTagDesconhecida(String tagRfid, String portaId) {
        Evento evento = new Evento();
        evento.setTagLida(tagRfid);
        evento.setPortaId(portaId);
        evento.setTipo(TipoEvento.SAIDA_EQUIPAMENTO);
        evento.setResultado(ResultadoEvento.TAG_DESCONHECIDA);
        evento.setObservacao("Tag não encontrada na base de dados");

        Evento guardado = eventoRepo.save(evento);
        alertaService.enviarAlarmePorTagDesconhecida(guardado);

        log.warn("Tag desconhecida {} na porta {}", tagRfid, portaId);
        return guardado;
    }
}
