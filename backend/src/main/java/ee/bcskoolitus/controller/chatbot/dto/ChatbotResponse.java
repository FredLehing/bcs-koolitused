package ee.bcskoolitus.controller.chatbot.dto;

public record ChatbotResponse(
        String answer,
        boolean sessionEnded
) {
}