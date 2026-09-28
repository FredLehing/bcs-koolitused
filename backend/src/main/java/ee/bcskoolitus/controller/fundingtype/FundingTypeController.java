package ee.bcskoolitus.controller.fundingtype;

import ee.bcskoolitus.service.FundingTypeService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class FundingTypeController {
    private final FundingTypeService fundingTypeService;

    @GetMapping("/funding-types")
    public void findFundingTypes(@RequestParam String contentLang) {
        fundingTypeService.findFundingTypes(contentLang);
    }
}
