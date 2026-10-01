package ee.bcskoolitus.service;

import ee.bcskoolitus.controller.chatbot.dto.SqlGenerationResult;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class NlToSqlService {

    private final LmStudioService lmStudioService;

    public SqlGenerationResult generateSql(String question, String schemaContext, String language) {
        return lmStudioService.generateSql(question, schemaContext, language);
    }
}
