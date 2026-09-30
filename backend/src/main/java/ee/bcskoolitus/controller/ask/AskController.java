package ee.bcskoolitus.controller.ask;

import ee.bcskoolitus.controller.ask.dto.AskRequest;
import ee.bcskoolitus.controller.ask.dto.AskResponse;
import ee.bcskoolitus.service.NlToSqlService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/ask")
@RequiredArgsConstructor
public class AskController {

    private final NlToSqlService nlToSqlService;

    @PostMapping
    public AskResponse ask(@Valid @RequestBody AskRequest request) {
        return nlToSqlService.ask(request.question());
    }
}
