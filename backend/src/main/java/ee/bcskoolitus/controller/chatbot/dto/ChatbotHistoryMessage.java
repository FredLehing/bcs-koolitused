package ee.bcskoolitus.controller.chatbot.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

/**
 * Üks brauseris hoitava chatboti vestluse varasem sõnum.
 * Ajaloos on ainult kasutaja ja assistendi nähtavad sõnumid.
 */
public record ChatbotHistoryMessage(

        @NotBlank(message = "Vestluse sõnumi roll on kohustuslik")
        @Pattern(
                regexp = "user|assistant",
                message = "Vestluse sõnumi roll peab olema user või assistant"
        )
        String role,

        @NotBlank(message = "Vestluse sõnum ei tohi olla tühi")
        @Size(max = 4000, message = "Vestluse sõnum võib olla kuni 4000 tähemärki pikk")
        String text
) {
}