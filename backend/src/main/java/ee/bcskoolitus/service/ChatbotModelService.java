package ee.bcskoolitus.service;

import ee.bcskoolitus.controller.chatbot.dto.ChatbotHistoryMessage;
import ee.bcskoolitus.controller.chatbot.dto.SqlGenerationResult;
import ee.bcskoolitus.infrastructure.exception.ChatbotException;
import lombok.RequiredArgsConstructor;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.core.io.ClassPathResource;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Chatboti mudeliklient.
 * Kasutab Google AI pilveteenust ja Gemini mudelit.
 */
@Service
@RequiredArgsConstructor
public class ChatbotModelService {

    private final ChatClient.Builder chatClientBuilder;

    public SqlGenerationResult generateSql(String question,
                                           String schemaContext,
                                           String language,
                                           List<ChatbotHistoryMessage> previousMessages) {

        String requestContext = """
                MODE: GENERATE_SQL
                Target response language: %s

                Earlier conversation messages:
                %s

                Latest user question:
                %s

                Database schema:
                %s

                Return JSON only:
                {"sql":"...", "reason":"..."}
                """.formatted(
                language,
                formatPreviousMessages(previousMessages),
                question,
                schemaContext
        );

        SqlGenerationResult result = requestStructuredResponse(
                requestContext,
                SqlGenerationResult.class
        );

        if (result == null) {
            throw unavailable();
        }

        return result;
    }

    public String answerFromRows(String question,
                                 String language,
                                 List<Map<String, Object>> rows) {

        String requestContext = """
                MODE: ANSWER_FROM_ROWS
                Target response language: %s

                Original user question:
                %s

                Database result rows:
                %s

                Return JSON only:
                {"answer":"..."}
                """.formatted(
                language,
                question,
                rows
        );

        ModelAnswer response = requestStructuredResponse(
                requestContext,
                ModelAnswer.class
        );

        if (response == null
                || response.answer() == null
                || response.answer().isBlank()) {
            throw unavailable();
        }

        return response.answer().trim();
    }

    private <T> T requestStructuredResponse(String requestContext,
                                            Class<T> responseType) {
        try {
            return chatClientBuilder.build()
                    .prompt()
                    .system(loadRuntimeInstructions())
                    .user(requestContext)
                    .call()
                    .responseEntity(responseType)
                    .getEntity();

        } catch (ChatbotException exception) {
            throw exception;

        } catch (Exception exception) {
            throw unavailable();
        }
    }

    private String formatPreviousMessages(
            List<ChatbotHistoryMessage> previousMessages) {

        if (previousMessages == null || previousMessages.isEmpty()) {
            return "(none)";
        }

        return previousMessages.stream()
                .map(message ->
                        "[" + message.role() + "] " + message.text())
                .collect(Collectors.joining("\n"));
    }

    private String loadRuntimeInstructions() {
        return readResource("chatbot/AGENT.md")
                + "\n\n"
                + readResource("chatbot/SKILL.md");
    }

    private String readResource(String path) {
        try {
            return new ClassPathResource(path)
                    .getContentAsString(StandardCharsets.UTF_8);

        } catch (IOException exception) {
            throw new IllegalStateException(
                    "Chatboti runtime juhise laadimine ebaõnnestus",
                    exception
            );
        }
    }

    private ChatbotException unavailable() {
        return new ChatbotException(
                "AI-chatboti teenus ei ole hetkel saadaval",
                "CHATBOT_UNAVAILABLE",
                HttpStatus.SERVICE_UNAVAILABLE
        );
    }

    private record ModelAnswer(String answer) {
    }
}