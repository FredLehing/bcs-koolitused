package ee.bcskoolitus;

import lombok.Getter;

// Tagasiside staatus (feedback.status): N = uus, U = osaleja muutis pärast admini ülevaatust, H = admin on üle vaadanud
@Getter
public enum FeedbackStatus {
    NEW("N"),
    UPDATED("U"),
    HISTORICAL("H");

    private final String code;

    FeedbackStatus(String code) {
        this.code = code;
    }
}
