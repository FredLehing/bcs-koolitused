package ee.bcskoolitus.controller.adminfeedback.dto;

import ee.bcskoolitus.infrastructure.exception.IncorrectInputException;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import java.time.LocalDate;
import java.util.Set;

@Data
public class AdminFeedbackFilterDto {
    @Schema(description = "Kuvamiskeel; puudumisel või tundmatu koodi korral põhikeel", example = "et")
    private String contentLang;
    @Schema(description = "Iga sõna koolituse ja osaleja nime liittekstis", example = "SQL Anna")
    private String searchText;
    @Schema(description = "Olemasolev toimumiskorra ID", example = "14")
    private String courseId;
    @Schema(allowableValues = {"N", "U", "H", "pending"})
    private String status;
    @Schema(allowableValues = {"yes", "no"})
    private String comments;
    @Schema(description = "Kaasav vahemiku algus yyyy-MM-dd", example = "2026-09-01")
    private String from;
    @Schema(description = "Kaasav vahemiku lõpp yyyy-MM-dd", example = "2026-09-30")
    private String until;
    @Schema(allowableValues = {"3", "5", "7"})
    private String low;
    @Schema(defaultValue = "default", allowableValues = {"default", "createdAt", "answersUpdatedAt", "trainingTitle", "participantName", "averageScore", "minimumScore", "status"})
    private String sortBy = "default";
    @Schema(defaultValue = "desc", allowableValues = {"asc", "desc"})
    private String sortDirection = "desc";
    @Schema(description = "Nullist algav leht", defaultValue = "0")
    private String page = "0";
    @Schema(description = "Lehe suurus 1–100", defaultValue = "5")
    private String limit = "5";

    public void validate() {
        Integer selectedCourseId = courseIdValue();
        if (selectedCourseId != null && selectedCourseId <= 0) throw new IncorrectInputException("courseId");
        validateChoice("status", status, Set.of("N", "U", "H", "pending"));
        validateChoice("comments", comments, Set.of("yes", "no"));
        Integer lowValue = lowValue();
        if (lowValue != null && !Set.of(3, 5, 7).contains(lowValue)) throw new IncorrectInputException("low");
        validateChoice("sortBy", sortBy, Set.of("default", "createdAt", "answersUpdatedAt", "trainingTitle", "participantName", "averageScore", "minimumScore", "status"));
        validateChoice("sortDirection", sortDirection, Set.of("asc", "desc"));
        if (pageValue() < 0) throw new IncorrectInputException("page");
        if (limitValue() < 1 || limitValue() > 100) throw new IncorrectInputException("limit");
        LocalDate fromDate = fromValue();
        LocalDate untilDate = untilValue();
        if (fromDate != null && untilDate != null && fromDate.isAfter(untilDate)) throw new IncorrectInputException("from");
    }

    public Integer courseIdValue() { return integerValue("courseId", courseId, null); }
    public Integer lowValue() { return integerValue("low", low, null); }
    public int pageValue() { return integerValue("page", page, 0); }
    public int limitValue() { return integerValue("limit", limit, 5); }
    public LocalDate fromValue() { return dateValue("from", from); }
    public LocalDate untilValue() { return dateValue("until", until); }
    public String sortByValue() { return isEmpty(sortBy) ? "default" : sortBy; }
    public String sortDirectionValue() { return isEmpty(sortDirection) ? "desc" : sortDirection; }

    public static Integer integerValue(String fieldName, String value, Integer defaultValue) {
        if (isEmpty(value)) return defaultValue;
        try { return Integer.valueOf(value); }
        catch (NumberFormatException exception) { throw new IncorrectInputException(fieldName); }
    }
    private static LocalDate dateValue(String fieldName, String value) {
        if (isEmpty(value)) return null;
        try {
            if (!value.matches("\\d{4}-\\d{2}-\\d{2}")) throw new IllegalArgumentException();
            return LocalDate.parse(value);
        } catch (RuntimeException exception) { throw new IncorrectInputException(fieldName); }
    }
    private static void validateChoice(String fieldName, String value, Set<String> allowed) {
        if (!isEmpty(value) && !allowed.contains(value)) throw new IncorrectInputException(fieldName);
    }
    public static boolean isEmpty(String value) { return value == null || value.isBlank(); }
}
