package ee.bcskoolitus;

import lombok.Getter;

// Tagasiside kriteeriumi staatus (feedback_criteria.status). Kustutatud kriteeriumit uues vormis ei kuvata.
@Getter
public enum FeedbackCriteriaStatus {
    ACTIVE("A"),
    DELETED("D");

    private final String code;

    FeedbackCriteriaStatus(String code) {
        this.code = code;
    }
}
