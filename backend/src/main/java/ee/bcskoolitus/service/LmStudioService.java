package ee.bcskoolitus.service;

import tools.jackson.core.JacksonException;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.json.JsonMapper;
import ee.bcskoolitus.controller.chatbot.dto.SqlGenerationResult;
import ee.bcskoolitus.infrastructure.exception.ChatbotException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ClassPathResource;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestClientResponseException;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Service
public class LmStudioService {

    private final JsonMapper objectMapper = JsonMapper.builder().build();
    private final RestClient restClient;
    private final String apiKey;
    private final String configuredModel;
    private final String runtimeInstructions;

    public LmStudioService(@Value("${lmstudio.base-url}") String baseUrl,
                           @Value("${lmstudio.api-key:}") String apiKey,
                           @Value("${lmstudio.model:}") String configuredModel) {
        this.restClient = RestClient.builder()
                .baseUrl(baseUrl)
                .build();

        this.apiKey = apiKey;
        this.configuredModel = configuredModel;
        this.runtimeInstructions = loadRuntimeInstructions();
    }

    public SqlGenerationResult generateSql(String question,
                                           String schemaContext,
                                           String language) {

        String requestContext = """
                MODE: GENERATE_SQL
                Target language: %s
                User question: %s

                %s

                Return JSON only: {"sql":"...", "reason":"..."}.
                """.formatted(language, question, schemaContext);

        String content = requestCompletion(requestContext, true);

        try {
            return objectMapper.readValue(
                    stripMarkdownFence(content),
                    SqlGenerationResult.class
            );
        } catch (JacksonException exception) {
            throw new ChatbotException(
                    "LM Studio tagastas vigase SQL-vastuse",
                    "CHATBOT_RESPONSE_INVALID",
                    HttpStatus.BAD_GATEWAY
            );
        }
    }

    public String answerFromRows(String question,
                                 String language,
                                 List<Map<String, Object>> rows) {

        String rowsJson;

        try {
            rowsJson = objectMapper.writeValueAsString(rows);
        } catch (JacksonException exception) {
            throw new ChatbotException(
                    "Andmete vastuseks ettevalmistamine ebaõnnestus",
                    "CHATBOT_RESPONSE_INVALID",
                    HttpStatus.BAD_GATEWAY
            );
        }

        String requestContext = """
                MODE: ANSWER_FROM_ROWS
                Target language: %s
                Original user question: %s
                Database result rows: %s
                """.formatted(language, question, rowsJson);

        return requestCompletion(requestContext, false).trim();
    }

    private String requestCompletion(String requestContext,
                                     boolean requestStructuredOutput) {

        Map<String, Object> request = new LinkedHashMap<>();

        request.put("model", detectModelId());
        request.put("temperature", 0);
        request.put("stream", false);

        request.put("messages", List.of(
                Map.of(
                        "role", "system",
                        "content", runtimeInstructions
                ),
                Map.of(
                        "role", "user",
                        "content", requestContext
                )
        ));

        if (requestStructuredOutput) {
            request.put(
                    "response_format",
                    Map.of("type", "json_object")
            );
        }

        try {
            return extractContent(sendCompletionRequest(request));

        } catch (RestClientResponseException exception) {

            if (!requestStructuredOutput
                    || exception.getStatusCode().value() != 400) {
                throw unavailable();
            }

            request.remove("response_format");

            try {
                return extractContent(sendCompletionRequest(request));
            } catch (RestClientException fallbackException) {
                throw unavailable();
            }

        } catch (RestClientException exception) {
            throw unavailable();
        }
    }

    private JsonNode sendCompletionRequest(Map<String, Object> request) {

        return restClient.post()
                .uri("/v1/chat/completions")
                .contentType(MediaType.APPLICATION_JSON)
                .headers(this::addAuthorization)
                .body(request)
                .retrieve()
                .body(JsonNode.class);
    }

    private String detectModelId() {

        try {
            JsonNode response = restClient.get()
                    .uri("/v1/models")
                    .headers(this::addAuthorization)
                    .retrieve()
                    .body(JsonNode.class);

            JsonNode models = response == null
                    ? null
                    : response.path("data");

            if (models == null
                    || !models.isArray()
                    || models.isEmpty()) {

                throw new ChatbotException(
                        "LM Studio mudelit ei leitud",
                        "CHATBOT_MODEL_UNAVAILABLE",
                        HttpStatus.SERVICE_UNAVAILABLE
                );
            }

            if (StringUtils.hasText(configuredModel)) {

                for (JsonNode model : models) {

                    if (configuredModel.equals(
                            model.path("id").asText())) {

                        return configuredModel;
                    }
                }

                throw new ChatbotException(
                        "Seadistatud LM Studio mudelit ei leitud",
                        "CHATBOT_MODEL_UNAVAILABLE",
                        HttpStatus.SERVICE_UNAVAILABLE
                );
            }

            return models.get(0)
                    .path("id")
                    .asText();

        } catch (RestClientException exception) {
            throw unavailable();
        }
    }

    private String extractContent(JsonNode response) {

        String content = response == null
                ? null
                : response
                .path("choices")
                .path(0)
                .path("message")
                .path("content")
                .asText(null);

        if (!StringUtils.hasText(content)) {

            throw new ChatbotException(
                    "LM Studio tagastas tühja vastuse",
                    "CHATBOT_RESPONSE_INVALID",
                    HttpStatus.BAD_GATEWAY
            );
        }

        return content;
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

    private String stripMarkdownFence(String content) {

        String normalizedContent = content.trim();

        if (normalizedContent.startsWith("```")) {

            int firstLineEnd = normalizedContent.indexOf('\n');

            normalizedContent = firstLineEnd < 0
                    ? ""
                    : normalizedContent.substring(firstLineEnd + 1);

            if (normalizedContent.endsWith("```")) {

                normalizedContent = normalizedContent.substring(
                        0,
                        normalizedContent.length() - 3
                );
            }
        }

        return normalizedContent.trim();
    }

    private void addAuthorization(
            org.springframework.http.HttpHeaders headers) {

        if (StringUtils.hasText(apiKey)) {
            headers.setBearerAuth(apiKey);
        }
    }

    private ChatbotException unavailable() {

        return new ChatbotException(
                "LM Studio ei ole praegu kättesaadav",
                "CHATBOT_UNAVAILABLE",
                HttpStatus.SERVICE_UNAVAILABLE
        );
    }
}