package ee.bcskoolitus.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import ee.bcskoolitus.persistance.course.participant.feedback.CourseParticipantFeedback;
import org.springframework.stereotype.Service;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HexFormat;
import java.util.List;

@Service
public class FeedbackAnswersVersionService {
    private final ObjectMapper objectMapper = new ObjectMapper();

    public String getAnswersVersion(Integer feedbackId, List<CourseParticipantFeedback> answers) {
        List<List<Object>> values = answers.stream()
                .sorted(Comparator.comparing(CourseParticipantFeedback::getId))
                .map(answer -> {
                    List<Object> row = new ArrayList<>();
                    row.add(answer.getId());
                    row.add(answer.getFeedbackCriteria().getId());
                    row.add(answer.getScore());
                    row.add(answer.getFeedbackText());
                    row.add(answer.getUpdatedAt().getEpochSecond());
                    row.add(answer.getUpdatedAt().getNano());
                    return row;
                }).toList();
        try {
            byte[] canonical = objectMapper.writeValueAsString(List.of(feedbackId, values)).getBytes(StandardCharsets.UTF_8);
            return HexFormat.of().formatHex(MessageDigest.getInstance("SHA-256").digest(canonical));
        } catch (JsonProcessingException | NoSuchAlgorithmException exception) {
            throw new IllegalStateException("Tagasiside versiooni arvutamine ebaõnnestus", exception);
        }
    }
}
