package ee.bcskoolitus.infrastructure.util;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class HtmlSanitizerTest {

    // TipTap editori realistlik väljund (ChatGPT-st kleebitud koolituse kirjeldus)
    private static final Path SAMPLE_PATH = Path.of("../docs/JSON/training-description-sample.html");

    private static final String MALICIOUS_HTML =
            "<p>Tavaline tekst</p><img src=x onerror=\"alert('XSS')\"><script>alert(1)</script>"
                    + "<a href=\"javascript:alert(1)\">pahatahtlik link</a>"
                    + "<p style=\"color:red\" onclick=\"alert(1)\">stiiliga lõik</p>";

    @Test
    void sanitizeDescription_keepsTextAndTagsOfEditorOutput() throws IOException {
        String sampleHtml = Files.readString(SAMPLE_PATH, StandardCharsets.UTF_8).strip();

        String sanitizedHtml = HtmlSanitizer.sanitizeDescription(sampleHtml);

        Document sampleDocument = Jsoup.parse(sampleHtml);
        Document sanitizedDocument = Jsoup.parse(sanitizedHtml);
        assertEquals(sampleDocument.text(), sanitizedDocument.text());
        assertEquals(sampleDocument.body().getAllElements().size(), sanitizedDocument.body().getAllElements().size());
        assertEquals(2, sanitizedDocument.select("a[href^=https][target=_blank][rel=noopener noreferrer nofollow]").size());
    }

    @Test
    void sanitizeDescription_doesNotAddLineBreaks() throws IOException {
        String sampleHtml = Files.readString(SAMPLE_PATH, StandardCharsets.UTF_8).strip();

        String sanitizedHtml = HtmlSanitizer.sanitizeDescription(sampleHtml);

        assertFalse(sanitizedHtml.contains("\n"));
    }

    @Test
    void sanitizeDescription_isIdempotent() throws IOException {
        String sampleHtml = Files.readString(SAMPLE_PATH, StandardCharsets.UTF_8).strip();

        String sanitizedHtml = HtmlSanitizer.sanitizeDescription(sampleHtml);

        assertEquals(sanitizedHtml, HtmlSanitizer.sanitizeDescription(sanitizedHtml));
    }

    @Test
    void sanitizeDescription_removesMaliciousContent() {
        String sanitizedHtml = HtmlSanitizer.sanitizeDescription(MALICIOUS_HTML);

        assertFalse(sanitizedHtml.contains("<img"));
        assertFalse(sanitizedHtml.contains("<script"));
        assertFalse(sanitizedHtml.contains("alert"));
        assertFalse(sanitizedHtml.contains("javascript:"));
        assertFalse(sanitizedHtml.contains("style="));
        assertFalse(sanitizedHtml.contains("onclick"));
        assertTrue(sanitizedHtml.contains("<p>Tavaline tekst</p>"));
        assertTrue(sanitizedHtml.contains("pahatahtlik link"));
        assertTrue(sanitizedHtml.contains("<p>stiiliga lõik</p>"));
    }

    @Test
    void sanitizeDescription_removesUnsupportedTagsButKeepsText() {
        String sanitizedHtml = HtmlSanitizer.sanitizeDescription(
                "<h1>Pealkiri</h1><p><span class=\"x\">tekst</span></p><table><tr><td>lahter</td></tr></table>");

        assertEquals("Pealkiri<p>tekst</p>lahter", sanitizedHtml);
    }

    @Test
    void sanitizeDescription_keepsPlainText() {
        String plainText = "Kursusel õpitakse Java süntaksit, objektorienteeritud programmeerimist ja põhilisi andmestruktuure.";

        assertEquals(plainText, HtmlSanitizer.sanitizeDescription(plainText));
    }

    @Test
    void sanitizeDescription_removesRelativeLinkHref() {
        String sanitizedHtml = HtmlSanitizer.sanitizeDescription("<p><a href=\"/koolitused\">link</a></p>");

        assertFalse(sanitizedHtml.contains("href"));
    }

    @Test
    void hasText_returnsFalseForEmptyHtml() {
        assertFalse(HtmlSanitizer.hasText(""));
        assertFalse(HtmlSanitizer.hasText("<p></p>"));
        assertFalse(HtmlSanitizer.hasText("<p> </p>"));
        assertFalse(HtmlSanitizer.hasText("<p><br></p>"));
        assertFalse(HtmlSanitizer.hasText("<script>x</script>"));
    }

    @Test
    void hasText_returnsTrueForHtmlWithText() {
        assertTrue(HtmlSanitizer.hasText("<p>a</p>"));
        assertTrue(HtmlSanitizer.hasText("tekst"));
    }

}
