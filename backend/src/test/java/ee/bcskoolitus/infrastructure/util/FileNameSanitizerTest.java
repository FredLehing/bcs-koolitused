package ee.bcskoolitus.infrastructure.util;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class FileNameSanitizerTest {

    // Näited: docs/tasks/backend/training-curriculum-db-changes.md, "Failinime reegel"
    @ParameterizedTest
    @CsvSource(delimiter = '|', textBlock = """
            Tehisaru töövahendid arendajale | Õppekava   | tehisaru-toovahendid-arendajale-oppekava.pdf
            AI tools for developers         | Curriculum | ai-tools-for-developers-curriculum.pdf
            Java & Spring: algkursus (2026) | Õppekava   | java-spring-algkursus-2026-oppekava.pdf
            Java algkursus                  | Õppekava   | java-algkursus-oppekava.pdf
            Курс Java                       | Õppekava   | java-oppekava.pdf
            Курс                            | Õppekava   | koolitus-12-oppekava.pdf
            Java algkursus                  | !!!        | java-algkursus.pdf
            """)
    void createCurriculumFileName_returnsSanitizedFileName(String title, String label, String expectedFileName) {
        assertEquals(expectedFileName, FileNameSanitizer.createCurriculumFileName(title, label, 12));
    }

    @Test
    void createCurriculumFileName_removesAllEstonianDiacritics() {
        assertEquals("oaouszoaousz-oppekava.pdf", FileNameSanitizer.createCurriculumFileName("õäöüšžÕÄÖÜŠŽ", "Õppekava", 12));
    }

    @Test
    void createCurriculumFileName_truncatesLongTitleAtDash() {
        String longTitle = "sõna ".repeat(30); // puhastatult "sona-sona-…", 149 märki

        String fileName = FileNameSanitizer.createCurriculumFileName(longTitle, "Õppekava", 12);

        String titlePart = fileName.substring(0, fileName.length() - "-oppekava.pdf".length());
        assertTrue(titlePart.length() <= 100);
        assertEquals("sona-".repeat(19) + "sona", titlePart);
    }

    @Test
    void createCurriculumFileName_cutsLongTitleWithoutDash() {
        String fileName = FileNameSanitizer.createCurriculumFileName("a".repeat(150), "Õppekava", 12);

        assertEquals("a".repeat(100) + "-oppekava.pdf", fileName);
    }
}
