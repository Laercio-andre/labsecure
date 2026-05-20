package ao.safelab.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

// ── Request: leitura enviada pelo leitor RFID ──────────────────────────────
public class LeituraDTO {

    @Data
    public static class Request {
        @NotBlank(message = "tagRfid é obrigatório")
        private String tagRfid;

        @NotBlank(message = "portaId é obrigatório")
        private String portaId;
    }

    @Data
    public static class Response {
        private Long eventoId;
        private String resultado;   // AUTORIZADO / ALARME / TAG_DESCONHECIDA
        private String tipo;
        private String mensagem;
        private String portaId;
        private String ocorridoEm;
    }
}
