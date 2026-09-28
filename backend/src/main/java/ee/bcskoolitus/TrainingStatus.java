package ee.bcskoolitus;

import lombok.Getter;

// Koolituse staatus (training.status). NB! Ära kasuta koolituse juures ApiStatus'i — seal "D" tähendab kustutatud.
@Getter
public enum TrainingStatus {
    UNPUBLISHED("U"),
    PUBLISHED("P");

    private final String code;

    TrainingStatus(String code) {
        this.code = code;
    }
}
