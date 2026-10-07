package ee.bcskoolitus.service;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class ChatbotAccessScopeService {

    private static final Set<String> DEFAULT_EXCLUDED_TABLES = Set.of(
            "user",
            "profile",
            "participant",
            "course_participant",
            "participant_certificate",
            "enquiry",
            "newsletter",
            "feedback",
            "lecturer_photo"
    );

    private final JdbcTemplate jdbcTemplate;

    @Value("${chatbot.database-schema}")
    private String databaseSchema;

    /**
     * Tagastab backendis kontrollitud rollile lubatud tabelid.
     *
     * Praegu kasutavad kõik rollid sama piiratud read-only andmeulatust.
     * Rollipõhist laiendamist saab hiljem teha ainult selles service'is.
     *
     * verifiedRole peab tulema backendist, mitte frontendist ega mudelilt.
     * Puuduva või tundmatu rolli korral kasutatakse GUEST scope'i.
     */
    public Set<String> getAllowedTables(String verifiedRole) {
        String role = normalizeRole(verifiedRole);

        return switch (role) {
            case "ADMIN" -> getAdminTables();
            case "USER" -> getUserTables();
            case "PARTICIPANT" -> getParticipantTables();
            default -> getGuestTables();
        };
    }

    private Set<String> getGuestTables() {
        return getDefaultAllowedTables();
    }

    private Set<String> getUserTables() {
        return getDefaultAllowedTables();
    }

    private Set<String> getParticipantTables() {
        return getDefaultAllowedTables();
    }

    private Set<String> getAdminTables() {
        return getDefaultAllowedTables();
    }

    private Set<String> getDefaultAllowedTables() {
        List<String> schemaTables = jdbcTemplate.queryForList("""
                        SELECT DISTINCT table_name
                        FROM information_schema.columns
                        WHERE table_schema = ?
                        ORDER BY table_name
                        """,
                String.class,
                databaseSchema
        );

        Set<String> allowedTables = new LinkedHashSet<>();

        for (String tableName : schemaTables) {
            String normalizedTableName =
                    tableName.toLowerCase(Locale.ROOT);

            if (!DEFAULT_EXCLUDED_TABLES.contains(normalizedTableName)) {
                allowedTables.add(normalizedTableName);
            }
        }

        return Set.copyOf(allowedTables);
    }

    private String normalizeRole(String verifiedRole) {
        if (verifiedRole == null || verifiedRole.isBlank()) {
            return "GUEST";
        }

        return verifiedRole
                .trim()
                .toUpperCase(Locale.ROOT);
    }
}