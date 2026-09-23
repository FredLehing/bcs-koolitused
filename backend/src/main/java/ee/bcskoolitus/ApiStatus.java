package ee.bcskoolitus;

import lombok.Getter;

@Getter
public enum ApiStatus {
    STATUS_ACTIVE("A"),
    STATUS_DELETED("D");

    private final String code;

    ApiStatus(String code) {
        this.code = code;
    }
}
