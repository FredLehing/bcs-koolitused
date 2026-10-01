package ee.bcskoolitus.service;

import ee.bcskoolitus.infrastructure.exception.ChatbotException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.util.HashSet;
import java.util.Locale;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service
public class SqlGuardService {

    private static final Pattern FORBIDDEN_TOKENS = Pattern.compile(
            "(?i)\\b(INSERT|UPDATE|DELETE|MERGE|DROP|ALTER|CREATE|TRUNCATE|GRANT|REVOKE|COPY|CALL|DO|VACUUM|ANALYZE|REINDEX|COMMENT|SET|RESET|EXECUTE|PREPARE|DEALLOCATE|LISTEN|NOTIFY|UNLISTEN|LOCK|CLUSTER|REFRESH|SECURITY|PG_[A-Z_]+|NEXTVAL|SETVAL|LO_[A-Z_]+|DBLINK|PG_SLEEP|CURRENT_SETTING|SET_CONFIG)\\b"
    );

    private static final Pattern TABLE_REFERENCE = Pattern.compile(
            "(?i)\\b(?:FROM|JOIN)\\s+([a-z_][a-z0-9_$]*(?:\\.[a-z_][a-z0-9_$]*)?)"
    );

    private static final Pattern CTE_NAME = Pattern.compile(
            "(?i)(?:WITH|,)\\s*([a-z_][a-z0-9_$]*)\\s+AS\\s*\\("
    );

    private static final Pattern LOCKING_CLAUSE = Pattern.compile(
            "(?i)\\bFOR\\s+(UPDATE|NO\\s+KEY\\s+UPDATE|SHARE|KEY\\s+SHARE)\\b"
    );

    private static final Set<String> EXCLUDED_TABLES = Set.of(
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

    @Value("${chatbot.database-schema}")
    private String databaseSchema;

    @Value("${chatbot.max-rows}")
    private int maxRows;

    public String validateAndLimit(String candidateSql) {
        String normalizedSql = normalize(candidateSql);
        String upperCaseSql = normalizedSql.toUpperCase(Locale.ROOT);

        if (!(upperCaseSql.startsWith("SELECT ")
                || upperCaseSql.startsWith("SELECT\n")
                || upperCaseSql.startsWith("WITH ")
                || upperCaseSql.startsWith("WITH\n"))) {
            throw rejected("Lubatud on ainult üks SELECT-päring");
        }

        if (normalizedSql.contains(";")
                || normalizedSql.contains("--")
                || normalizedSql.contains("/*")
                || normalizedSql.contains("*/")
                || upperCaseSql.contains("INFORMATION_SCHEMA")
                || upperCaseSql.contains("PG_CATALOG")) {
            throw rejected("SQL sisaldab keelatud süntaksit või süsteemiobjekti");
        }

        if (FORBIDDEN_TOKENS.matcher(normalizedSql).find()) {
            throw rejected("SQL sisaldab keelatud käsku või funktsiooni");
        }

        if (LOCKING_CLAUSE.matcher(normalizedSql).find()) {
            throw rejected("Ridu lukustav SELECT ei ole lubatud");
        }

        validateTableReferences(normalizedSql);

        return applyRowLimit(normalizedSql);
    }

    private String normalize(String candidateSql) {
        if (candidateSql == null) {
            throw rejected("Mudel ei tagastanud SQL-i");
        }

        String normalizedSql = candidateSql.trim();

        if (normalizedSql.startsWith("```")) {
            int firstLineEnd = normalizedSql.indexOf('\n');

            normalizedSql = firstLineEnd < 0
                    ? ""
                    : normalizedSql.substring(firstLineEnd + 1);

            if (normalizedSql.endsWith("```")) {
                normalizedSql = normalizedSql.substring(
                        0,
                        normalizedSql.length() - 3
                );
            }

            normalizedSql = normalizedSql.trim();
        }

        if (normalizedSql.isBlank()) {
            throw rejected("Mudel ei tagastanud SQL-i");
        }

        return normalizedSql;
    }

    private void validateTableReferences(String normalizedSql) {
        Set<String> cteNames = new HashSet<>();

        Matcher cteMatcher = CTE_NAME.matcher(normalizedSql);

        while (cteMatcher.find()) {
            cteNames.add(
                    cteMatcher.group(1).toLowerCase(Locale.ROOT)
            );
        }

        Matcher tableReferenceMatcher = TABLE_REFERENCE.matcher(normalizedSql);

        while (tableReferenceMatcher.find()) {
            String tableReference = tableReferenceMatcher
                    .group(1)
                    .toLowerCase(Locale.ROOT);

            if (cteNames.contains(tableReference)) {
                continue;
            }

            String allowedSchemaPrefix =
                    databaseSchema.toLowerCase(Locale.ROOT) + ".";

            if (!tableReference.startsWith(allowedSchemaPrefix)) {
                throw rejected(
                        "Tabel peab kuuluma lubatud rakendusskeemi"
                );
            }

            String tableName = tableReference.substring(
                    tableReference.indexOf('.') + 1
            );

            if (EXCLUDED_TABLES.contains(tableName)) {
                throw rejected(
                        "Isikuandmeid sisaldavate tabelite pärimine ei ole lubatud"
                );
            }
        }
    }

    private String applyRowLimit(String validatedSql) {
        return "SELECT * FROM ("
                + validatedSql
                + ") AS chatbot_result LIMIT "
                + maxRows;
    }

    private ChatbotException rejected(String message) {
        return new ChatbotException(
                message,
                "CHATBOT_QUERY_REJECTED",
                HttpStatus.BAD_REQUEST
        );
    }
}