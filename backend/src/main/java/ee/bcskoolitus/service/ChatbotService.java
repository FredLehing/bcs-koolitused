package ee.bcskoolitus.service;

import ee.bcskoolitus.controller.chatbot.dto.ChatbotRequest;
import ee.bcskoolitus.controller.chatbot.dto.ChatbotResponse;
import ee.bcskoolitus.controller.chatbot.dto.SqlGenerationResult;
import ee.bcskoolitus.persistance.chatbot.ChatbotQueryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class ChatbotService {

    private static final int MAX_USER_QUESTIONS_PER_SESSION = 10;

    private final DatabaseSchemaService databaseSchemaService;
    private final SqlGuardService sqlGuardService;
    private final ChatbotQueryRepository chatbotQueryRepository;
    private final ChatbotModelService chatbotModelService;
    private final ChatbotAccessScopeService chatbotAccessScopeService;
    private final ChatbotHandoffService chatbotHandoffService;

    public ChatbotResponse ask(ChatbotRequest chatbotRequest) {

        if (sessionLimitReached(chatbotRequest)) {
            chatbotHandoffService.handoff(chatbotRequest);

            return new ChatbotResponse(
                    getHandoffMessage(chatbotRequest.language()),
                    true
            );
        }

        Set<String> allowedTables =
                chatbotAccessScopeService.getAllowedTables(null);

        String schemaContext =
                databaseSchemaService.getSchemaContext(allowedTables);

        SqlGenerationResult sqlGenerationResult =
                chatbotModelService.generateSql(
                        chatbotRequest.question(),
                        schemaContext,
                        chatbotRequest.language(),
                        chatbotRequest.previousMessages()
                );

        if (sqlGenerationResult.sql() == null
                || sqlGenerationResult.sql().isBlank()) {

            return new ChatbotResponse(
                    sqlGenerationResult.reason(),
                    false
            );
        }

        String validatedSql =
                sqlGuardService.validateAndLimit(
                        sqlGenerationResult.sql(),
                        allowedTables
                );

        List<Map<String, Object>> rows =
                chatbotQueryRepository.executeReadOnlyQuery(validatedSql);

        String answer =
                chatbotModelService.answerFromRows(
                        chatbotRequest.question(),
                        chatbotRequest.language(),
                        rows
                );

        return new ChatbotResponse(answer, false);
    }

    private boolean sessionLimitReached(ChatbotRequest chatbotRequest) {

        if (chatbotRequest.previousMessages() == null) {
            return false;
        }

        long previousUserQuestions =
                chatbotRequest.previousMessages().stream()
                        .filter(message -> "user".equals(message.role()))
                        .count();

        return previousUserQuestions + 1 >= MAX_USER_QUESTIONS_PER_SESSION;
    }

    private String getHandoffMessage(String language) {

        if ("en".equals(language)) {
            return "I have forwarded the conversation to customer service. They will contact you.";
        }

        return "Edastasin vestluse teenindajale. Teiega võetakse ühendust.";
    }
}