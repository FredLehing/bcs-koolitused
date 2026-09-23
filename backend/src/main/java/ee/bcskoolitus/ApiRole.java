package ee.bcskoolitus;

import lombok.Getter;

@Getter
public enum ApiRole {
    ROLE_ADMIN ("admin");

    private final String name;

    ApiRole(String name) {
        this.name = name;
    }
}
