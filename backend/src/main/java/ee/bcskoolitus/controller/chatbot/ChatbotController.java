package ee.bcskoolitus.controller.chatbot;

import ee.bcskoolitus.controller.chatbot.dto.ChatbotRequest;
import ee.bcskoolitus.controller.chatbot.dto.ChatbotResponse;
import ee.bcskoolitus.infrastructure.error.ApiError;
import ee.bcskoolitus.service.ChatbotService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
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
    @Operation(
            summary = "Vastab kasutaja küsimusele BCS Koolituste kohta",
            description = "Chatbot vastab loomulikus keeles. Vajaduse korral hangitakse vastuseks "
                    + "BCS Koolituste andmed kontrollitud read-only SQL-päringuga. "
                    + "Vastuse keel määratakse päringu language väärtusega."
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Chatboti vastus",
                    content = @Content(schema = @Schema(implementation = ChatbotResponse.class))
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Vigane sisend",
                    content = @Content(schema = @Schema(implementation = ApiError.class))
            ),
            @ApiResponse(
                    responseCode = "503",
                    description = "AI-chatboti teenus ei ole hetkel saadaval",
                    content = @Content(schema = @Schema(implementation = ApiError.class))
            )
    })
    public ChatbotResponse ask(@Valid @RequestBody ChatbotRequest chatbotRequest) {
        return chatbotService.ask(chatbotRequest);
    }
}