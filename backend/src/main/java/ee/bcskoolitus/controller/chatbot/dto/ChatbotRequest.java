package ee.bcskoolitus.controller.chatbot.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

import java.util.List;

public record ChatbotRequest(

        @NotBlank(message = "Küsimus ei tohi olla tühi")
        @Size(max = 500, message = "Küsimus võib olla kuni 500 tähemärki pikk")
        String question,

        @NotBlank(message = "Keel on kohustuslik")
        @Pattern(regexp = "et|en", message = "Vastuse keel peab olema et või en")
        String language,

        List<@Valid @NotNull ChatbotHistoryMessage> previousMessages
) {
}