package ee.bcskoolitus.service;

import ee.bcskoolitus.controller.common.dto.AiTrainingContentDto;
import ee.bcskoolitus.infrastructure.ai.AiMetricsAdvisor;
import ee.bcskoolitus.infrastructure.exception.ChatbotException;
import ee.bcskoolitus.infrastructure.exception.ForbiddenException;
import ee.bcskoolitus.infrastructure.util.HtmlSanitizer;
import ee.bcskoolitus.persistance.training.translation.curriculum.TrainingTranslationCurriculum;
import lombok.RequiredArgsConstructor;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.google.genai.GoogleGenAiChatOptions;
import org.springframework.ai.google.genai.common.GoogleGenAiThinkingLevel;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;

import static ee.bcskoolitus.Error.AI_SERVICE_UNAVAILABLE;
import static ee.bcskoolitus.Error.PDF_NOT_CURRICULUM;

@Service
@RequiredArgsConstructor
public class AiTrainingService {
    private static final int PROMPT_VERSION = 3;
    private static final int MAX_TEXT_LENGTH = 255;

    private final ChatClient.Builder chatClientBuilder;
    private final AiMetricsAdvisor aiMetricsAdvisor;
    private final TrainingTranslationCurriculumService trainingTranslationCurriculumService;

    @Value("classpath:prompts/training-pdf-system.md")
    private Resource trainingPdfSystemPrompt;


    public AiTrainingContentDto createContentFromPdf(byte[] pdf) {
        TrainingTranslationCurriculumService.validateCurriculum(pdf);
        AiTrainingPdfResult aiTrainingPdfResult = chatClientBuilder.build()
                .prompt()
                .system(trainingPdfSystemPrompt)
                .user(promptUserSpec -> promptUserSpec.text("Create the training form fields from this PDF.")
                        .media(MediaType.APPLICATION_PDF, new ByteArrayResource(pdf)))
                .advisors(advisorSpec -> advisorSpec.advisors(aiMetricsAdvisor).param("promptVersion", PROMPT_VERSION))
                .options(GoogleGenAiChatOptions.builder()
                        .temperature(0.2)
                        .maxOutputTokens(8192)
                        .thinkingLevel(GoogleGenAiThinkingLevel.LOW)
                        .build())
                .call()
                .entity(AiTrainingPdfResult.class);

        Boolean isCurriculum = aiTrainingPdfResult.isCurriculum();
        if (isCurriculum == null) {
            throw new ChatbotException(AI_SERVICE_UNAVAILABLE.getMessage(), AI_SERVICE_UNAVAILABLE.name(),
                    HttpStatus.SERVICE_UNAVAILABLE);
        }
        if (!isCurriculum) {
            throw new ForbiddenException(PDF_NOT_CURRICULUM.getMessage(), PDF_NOT_CURRICULUM.name());
        }
        return new AiTrainingContentDto(
                truncate(aiTrainingPdfResult.title()),
                truncate(aiTrainingPdfResult.shortDescription()),
                HtmlSanitizer.sanitizeDescription(aiTrainingPdfResult.description()));
    }

    public AiTrainingContentDto createContentFromSavedCurriculum(Integer trainingTranslationId) {
        TrainingTranslationCurriculum trainingTranslationCurriculum = trainingTranslationCurriculumService
                .getValidTrainingTranslationCurriculumBy(trainingTranslationId);
        return createContentFromPdf(trainingTranslationCurriculum.getFile());
    }

    private static String truncate(String text) {
        if (text == null || text.length() <= MAX_TEXT_LENGTH) {
            return text;
        }
        return text.substring(0, MAX_TEXT_LENGTH);
    }

    private record AiTrainingPdfResult(Boolean isCurriculum, String title, String shortDescription, String description) {
    }
}
