package ee.bcskoolitus.persistance.feedback;

import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;

// Tagasiside DML-i timestamp ilma ajavööndita väljendab Europe/Tallinn kohalikku aega.
@Converter
public class FeedbackTimestampConverter implements AttributeConverter<Instant, LocalDateTime> {
    private static final ZoneId DATABASE_ZONE = ZoneId.of("Europe/Tallinn");
    @Override
    public LocalDateTime convertToDatabaseColumn(Instant instant) {
        return instant == null ? null : LocalDateTime.ofInstant(instant, DATABASE_ZONE);
    }
    @Override
    public Instant convertToEntityAttribute(LocalDateTime timestamp) {
        return timestamp == null ? null : timestamp.atZone(DATABASE_ZONE).toInstant();
    }
}
