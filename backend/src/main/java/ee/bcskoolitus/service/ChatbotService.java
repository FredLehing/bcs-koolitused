package ee.bcskoolitus.service;

import ee.bcskoolitus.controller.chatbot.dto.ChatbotRequest;
import ee.bcskoolitus.controller.chatbot.dto.ChatbotResponse;
import ee.bcskoolitus.controller.chatbot.dto.SqlGenerationResult;
import ee.bcskoolitus.persistance.chatbot.ChatbotQueryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class ChatbotService {

    private final DatabaseSchemaService databaseSchemaService;
    private final NlToSqlService nlToSqlService;
    private final SqlGuardService sqlGuardService;
    private final ChatbotQueryRepository chatbotQueryRepository;
    private final LmStudioService lmStudioService;

    @Transactional(readOnly = true)
    public ChatbotResponse ask(ChatbotRequest chatbotRequest) {
        String schemaContext = databaseSchemaService.getSchemaContext();
        SqlGenerationResult sqlGenerationResult = nlToSqlService.generateSql(
                chatbotRequest.question(), schemaContext, chatbotRequest.language());

        if (sqlGenerationResult.sql() == null || sqlGenerationResult.sql().isBlank()) {
            return new ChatbotResponse(sqlGenerationResult.reason());
        }

        String validatedSql = sqlGuardService.validateAndLimit(sqlGenerationResult.sql());
        List<Map<String, Object>> rows = chatbotQueryRepository.executeReadOnlyQuery(validatedSql);
        String answer = lmStudioService.answerFromRows(chatbotRequest.question(), chatbotRequest.language(), rows);
        return new ChatbotResponse(answer);
    }
}
