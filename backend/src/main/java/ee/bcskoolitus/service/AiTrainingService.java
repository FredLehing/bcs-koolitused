package ee.bcskoolitus.service;

import ee.bcskoolitus.controller.common.dto.AiTrainingContentDto;
import ee.bcskoolitus.infrastructure.ai.AiMetricsAdvisor;
import lombok.RequiredArgsConstructor;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.google.genai.GoogleGenAiChatOptions;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.Resource;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AiTrainingService {
    private static final int PROMPT_VERSION = 1;

    private final ChatClient.Builder chatClientBuilder;
    private final AiMetricsAdvisor aiMetricsAdvisor;

    @Value("classpath:prompts/training-pdf-system.st")
    private Resource trainingPdfSystemPrompt;


    public AiTrainingContentDto createContentFromPdf(byte[] pdf) {
        AiTrainingPdfResult aiTrainingPdfResult = chatClientBuilder.build()
                .prompt()
                .system(trainingPdfSystemPrompt)
                .user(promptUserSpec -> promptUserSpec.text("Create the training form fields from this PDF.")
                        .media(MediaType.APPLICATION_PDF, new ByteArrayResource(pdf)))
                .advisors(advisorSpec -> advisorSpec.advisors(aiMetricsAdvisor))
                .options(GoogleGenAiChatOptions.builder()
                        .temperature(0.2)
                        .maxOutputTokens(8192)
                        .build())
                .call()
                .entity(AiTrainingPdfResult.class);
        // TODO Samm 4: isCurriculum kontroll, kärpimine, HTML puhastus.
        return new AiTrainingContentDto(
                aiTrainingPdfResult.title(),
                aiTrainingPdfResult.shortDescription(),
                aiTrainingPdfResult.description());
    }

    private record AiTrainingPdfResult(Boolean isCurriculum, String title, String shortDescription, String description) {

    }
}
