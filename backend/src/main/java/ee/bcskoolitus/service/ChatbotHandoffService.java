package ee.bcskoolitus.service;

import ee.bcskoolitus.controller.chatbot.dto.ChatbotHistoryMessage;
import ee.bcskoolitus.controller.chatbot.dto.ChatbotRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class ChatbotHandoffService {

    private final ObjectProvider<JavaMailSender> mailSenderProvider;

    @Value("${chatbot.handoff.recipient:}")
    private String handoffRecipient;

    @Async
    public void handoff(ChatbotRequest chatbotRequest) {

        JavaMailSender mailSender = mailSenderProvider.getIfAvailable();

        if (mailSender == null || handoffRecipient.isBlank()) {
            log.warn(
                    "Chatboti üleandmise e-post ei ole seadistatud. "
                            + "SMTP või chatbot.handoff.recipient puudub."
            );
            return;
        }

        SimpleMailMessage message = new SimpleMailMessage();
        message.setTo(handoffRecipient);
        message.setSubject("Chatboti vestlus teenindajale");
        message.setText(createTranscript(chatbotRequest));

        try {
            mailSender.send(message);
        } catch (Exception exception) {
            log.error(
                    "Chatboti vestluse e-postiga edastamine ebaõnnestus",
                    exception
            );
        }
    }

    private String createTranscript(ChatbotRequest chatbotRequest) {

        StringBuilder transcript = new StringBuilder();

        List<ChatbotHistoryMessage> previousMessages =
                chatbotRequest.previousMessages();

        if (previousMessages != null) {
            previousMessages.forEach(message ->
                    transcript
                            .append(getRoleName(message.role()))
                            .append(": ")
                            .append(message.text())
                            .append(System.lineSeparator())
                            .append(System.lineSeparator())
            );
        }

        transcript
                .append("Kasutaja: ")
                .append(chatbotRequest.question())
                .append(System.lineSeparator());

        return transcript.toString();
    }

    private String getRoleName(String role) {
        return "assistant".equals(role)
                ? "Chatbot"
                : "Kasutaja";
    }
}