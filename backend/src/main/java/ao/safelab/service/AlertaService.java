package ao.safelab.service;

import ao.safelab.entity.Equipamento;
import ao.safelab.entity.Evento;
import com.twilio.Twilio;
import com.twilio.rest.api.v2010.account.Message;
import com.twilio.type.PhoneNumber;
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

import java.time.format.DateTimeFormatter;

@Slf4j
@Service
public class AlertaService {

    private final JavaMailSender mailSender;

    @Value("${twilio.account-sid}")
    private String twilioSid;

    @Value("${twilio.auth-token}")
    private String twilioToken;

    @Value("${twilio.from-number}")
    private String twilioFrom;

    @Value("${safelab.alert-email}")
    private String alertEmail;

    @Value("${safelab.alert-phone}")
    private String alertPhone;

    public AlertaService(JavaMailSender mailSender) {
        this.mailSender = mailSender;
    }

    @PostConstruct
    public void init() {
        Twilio.init(twilioSid, twilioToken);
    }

    public void enviarAlarme(Evento evento, Equipamento equipamento) {
        String hora = evento.getOcorridoEm()
            .format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss"));

        String mensagem = String.format(
            "⚠️ ALARME SafeLab\n" +
            "Equipamento: %s\n" +
            "Nº Série: %s\n" +
            "Porta: %s\n" +
            "Hora: %s\n" +
            "Tag RFID: %s",
            equipamento.getNome(),
            equipamento.getNumeroSerie(),
            evento.getPortaId(),
            hora,
            evento.getTagLida()
        );

        enviarSms(mensagem);
        enviarEmail(
            "⚠️ ALARME SafeLab — Saída não autorizada",
            mensagem
        );
    }

    public void enviarAlarmePorTagDesconhecida(Evento evento) {
        String hora = evento.getOcorridoEm()
            .format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss"));

        String mensagem = String.format(
            "⚠️ ALARME SafeLab\n" +
            "Tag RFID desconhecida detectada\n" +
            "Porta: %s\n" +
            "Hora: %s\n" +
            "Tag: %s",
            evento.getPortaId(),
            hora,
            evento.getTagLida()
        );

        enviarSms(mensagem);
        enviarEmail("⚠️ ALARME SafeLab — Tag desconhecida", mensagem);
    }

    private void enviarSms(String corpo) {
        try {
            Message.creator(
                new PhoneNumber(alertPhone),
                new PhoneNumber(twilioFrom),
                corpo
            ).create();
            log.info("SMS de alerta enviado para {}", alertPhone);
        } catch (Exception e) {
            log.error("Falha ao enviar SMS: {}", e.getMessage());
        }
    }

    private void enviarEmail(String assunto, String corpo) {
        try {
            SimpleMailMessage msg = new SimpleMailMessage();
            msg.setTo(alertEmail);
            msg.setSubject(assunto);
            msg.setText(corpo);
            mailSender.send(msg);
            log.info("Email de alerta enviado para {}", alertEmail);
        } catch (Exception e) {
            log.error("Falha ao enviar email: {}", e.getMessage());
        }
    }
}
