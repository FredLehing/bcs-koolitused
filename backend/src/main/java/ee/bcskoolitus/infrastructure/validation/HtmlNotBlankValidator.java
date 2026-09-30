package ee.bcskoolitus.infrastructure.validation;

import ee.bcskoolitus.infrastructure.util.HtmlSanitizer;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

public class HtmlNotBlankValidator implements ConstraintValidator<HtmlNotBlank, String> {

    @Override
    public boolean isValid(String html, ConstraintValidatorContext context) {
        return html == null || HtmlSanitizer.hasText(html);
    }

}
