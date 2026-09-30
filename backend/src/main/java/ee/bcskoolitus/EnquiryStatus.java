package ee.bcskoolitus;

import lombok.Getter;

// Huvilise päringu staatus (enquiry.status)
@Getter
public enum EnquiryStatus {
    NEW("U"),
    HANDLED("H");

    private final String code;

    EnquiryStatus(String code) {
        this.code = code;
    }
}
