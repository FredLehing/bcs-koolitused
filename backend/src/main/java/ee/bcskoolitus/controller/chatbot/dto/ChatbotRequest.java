package ee.bcskoolitus.controller.chatbot.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record ChatbotRequest(

        @NotBlank(message = "Küsimus ei tohi olla tühi")
        @Size(max = 500, message = "Küsimus võib olla kuni 500 tähemärki pikk")
        String question,

        @NotBlank(message = "Keel on kohustuslik")
        @Pattern(regexp = "[a-z]{2}", message = "Keel peab olema kahetäheline keelekood")
        String language
) {
}
