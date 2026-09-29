package ee.bcskoolitus.infrastructure.util;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.safety.Safelist;

// Richtext kirjelduse (training_translation.description) HTML puhastamine.
// Lubatud märgendid peavad ühtima frontendi editori (RichTextEditor.vue) toetatud vormingutega.
public class HtmlSanitizer {

    private static final Safelist DESCRIPTION_SAFELIST = new Safelist()
            .addTags("p", "br", "strong", "em", "u", "h3", "h4", "ul", "ol", "li", "a")
            .addAttributes("a", "href")
            .addProtocols("a", "href", "http", "https", "mailto")
            .addEnforcedAttribute("a", "target", "_blank")
            .addEnforcedAttribute("a", "rel", "noopener noreferrer nofollow");

    // prettyPrint(false) — muidu lisab jsoup reavahetusi ja taandeid ning iga salvestus muudaks sisu
    private static final Document.OutputSettings OUTPUT_SETTINGS = new Document.OutputSettings().prettyPrint(false);

    public static String sanitizeDescription(String html) {
        return Jsoup.clean(html, "", DESCRIPTION_SAFELIST, OUTPUT_SETTINGS);
    }

    // Tühjuse kontroll pärast puhastust — nt "<p></p>" või "<p><script>x</script></p>" on tühi
    public static boolean hasText(String html) {
        return !Jsoup.parse(sanitizeDescription(html)).text().isBlank();
    }

}
