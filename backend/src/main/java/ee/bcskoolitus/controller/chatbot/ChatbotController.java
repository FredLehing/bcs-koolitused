package ee.bcskoolitus.controller.chatbot;

import ee.bcskoolitus.controller.chatbot.dto.ChatbotRequest;
import ee.bcskoolitus.controller.chatbot.dto.ChatbotResponse;
import ee.bcskoolitus.service.ChatbotService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/chatbot")
@RequiredArgsConstructor
public class ChatbotController {

    private final ChatbotService chatbotService;

    @PostMapping("/ask")
    public ChatbotResponse ask(@Valid @RequestBody ChatbotRequest chatbotRequest) {
        return chatbotService.ask(chatbotRequest);
    }
}
