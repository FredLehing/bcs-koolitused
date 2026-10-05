package ee.bcskoolitus.infrastructure.util;

import java.text.Normalizer;
import java.util.Locale;

// Allalaaditava faili nimi koolituse pealkirjast: ainult a-z, 0-9 ja "-" (ka Content-Disposition päises turvaline).
// Reegel: docs/mock-wireframe/loo-mock-vaade/form-view/training-curriculum-plaan.md, jaotis "Failinimi".
public class FileNameSanitizer {

    private static final int TITLE_MAX_LENGTH = 100;

    // label = sõna "õppekava" tõlke keeles (frontendist, curriculumLabel)
    public static String createCurriculumFileName(String title, String label, Integer trainingId) {
        String titlePart = truncateAtDash(sanitize(title), TITLE_MAX_LENGTH);
        if (titlePart.isEmpty()) {
            titlePart = "koolitus-" + trainingId;
        }
        String labelPart = sanitize(label);
        return labelPart.isEmpty() ? titlePart + ".pdf" : titlePart + "-" + labelPart + ".pdf";
    }

    // "Java & Spring: algkursus (2026)" -> "java-spring-algkursus-2026", "Õppekava" -> "oppekava"
    static String sanitize(String text) {
        if (text == null) {
            return "";
        }
        String withoutDiacritics = Normalizer.normalize(text, Normalizer.Form.NFD).replaceAll("\\p{M}", "");
        return withoutDiacritics.toLowerCase(Locale.ROOT)
                .replaceAll("[^a-z0-9]+", "-")
                .replaceAll("^-|-$", "");
    }

    // Lõigatakse sõna piirilt (viimase "-" kohalt), et failinimes ei oleks poolikut sõna
    private static String truncateAtDash(String text, int maxLength) {
        if (text.length() <= maxLength) {
            return text;
        }
        String truncated = text.substring(0, maxLength + 1);
        int lastDashIndex = truncated.lastIndexOf('-');
        return lastDashIndex > 0 ? truncated.substring(0, lastDashIndex) : text.substring(0, maxLength);
    }

}
