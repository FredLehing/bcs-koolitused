package ee.bcskoolitus.infrastructure.util;

import java.util.Arrays;
import java.util.List;

// Vabateksti otsingu abimeetodid JPA Specificationite LIKE tingimuste jaoks
public final class LikePatterns {

    public static final char ESCAPE_CHAR = '\\';

    private LikePatterns() {
    }

    // Tühikutega eraldatud sõnad väiketähtedes; tühi või null tekst → tühi list
    public static List<String> toLowerCaseWords(String searchText) {
        if (searchText == null || searchText.isBlank()) {
            return List.of();
        }
        return Arrays.asList(searchText.trim().toLowerCase().split("\\s+"));
    }

    // "%sõna%" — kasutaja sisestatud % ja _ otsitakse sõna-sõnalt, mitte LIKE metamärkidena
    public static String toContainsPattern(String word) {
        String escapedWord = word.replace("\\", "\\\\")
                .replace("%", "\\%")
                .replace("_", "\\_");
        return "%" + escapedWord + "%";
    }
}
