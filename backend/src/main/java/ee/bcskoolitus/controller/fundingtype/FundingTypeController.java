package ee.bcskoolitus.controller.fundingtype;

import ee.bcskoolitus.controller.common.dto.FundingTypeDto;
import ee.bcskoolitus.service.FundingTypeService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class FundingTypeController {

    private final FundingTypeService fundingTypeService;

    @GetMapping("/funding-types")
    public List<FundingTypeDto> getFundingTypes(@RequestParam String contentLang) {
        return fundingTypeService.getFundingTypes(contentLang);
    }
}