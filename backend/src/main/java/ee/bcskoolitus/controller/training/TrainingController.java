package ee.bcskoolitus.controller.training;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api")
public class TrainingController {
    @GetMapping("/trainings")
    public void getTrainings(@RequestParam(required = false) Integer categoryId,
                             @RequestParam(required = false) Integer fundingTypeId,
                             @RequestParam(defaultValue = "5") Integer limit,
                             @RequestParam(defaultValue = "0") Integer page,
                             @RequestParam(required = false) String trainingLang,
                             @RequestParam(required = false) String translationLang) {

    }
}
