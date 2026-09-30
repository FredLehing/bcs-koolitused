package ee.bcskoolitus.infrastructure.util;

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

class LikePatternsTest {

    @Test
    void toLowerCaseWords_splitsOnWhitespaceAndLowercases() {
        assertEquals(List.of("java", "algkursus"), LikePatterns.toLowerCaseWords("  Java   Algkursus "));
    }

    @Test
    void toLowerCaseWords_blankOrNull_returnsEmptyList() {
        assertEquals(List.of(), LikePatterns.toLowerCaseWords(""));
        assertEquals(List.of(), LikePatterns.toLowerCaseWords("   "));
        assertEquals(List.of(), LikePatterns.toLowerCaseWords(null));
    }

    @Test
    void toContainsPattern_escapesLikeWildcards() {
        assertEquals("%java%", LikePatterns.toContainsPattern("java"));
        assertEquals("%100\\%%", LikePatterns.toContainsPattern("100%"));
        assertEquals("%a\\_b%", LikePatterns.toContainsPattern("a_b"));
        assertEquals("%c:\\\\x%", LikePatterns.toContainsPattern("c:\\x"));
    }
}
