package ee.bcskoolitus.service;

import ee.bcskoolitus.infrastructure.exception.ChatbotException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.util.HashSet;
import java.util.Locale;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

@Service
@Slf4j
public class SqlGuardService {

    private static final Pattern READ_QUERY_START = Pattern.compile(
            "(?i)^(SELECT|WITH)\\b"
    );

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

    @Value("${chatbot.database-schema}")
    private String databaseSchema;

    @Value("${chatbot.max-rows}")
    private int maxRows;

    public String validateAndLimit(
            String candidateSql,
            Set<String> allowedTables
    ) {
        Set<String> normalizedAllowedTables =
                normalizeAllowedTables(allowedTables);

        if (normalizedAllowedTables.isEmpty()) {
            throw rejected("Lubatud tabelite loend on tühi");
        }

        String normalizedSql = normalize(candidateSql);
        String upperCaseSql = normalizedSql.toUpperCase(Locale.ROOT);

        if (!READ_QUERY_START.matcher(normalizedSql).find()) {
            throw rejected("Päring ei alga SELECT või WITH võtmesõnaga");
        }

        if (normalizedSql.contains(";")
                || normalizedSql.contains("--")
                || normalizedSql.contains("/*")
                || normalizedSql.contains("*/")
                || upperCaseSql.contains("INFORMATION_SCHEMA")
                || upperCaseSql.contains("PG_CATALOG")) {

            throw rejected(
                    "Päring sisaldab keelatud süntaksit või süsteemiobjekti"
            );
        }

        if (FORBIDDEN_TOKENS.matcher(normalizedSql).find()) {
            throw rejected(
                    "Päring sisaldab keelatud käsku või funktsiooni"
            );
        }

        if (LOCKING_CLAUSE.matcher(normalizedSql).find()) {
            throw rejected(
                    "Päring sisaldab ridu lukustavat SELECT-lauset"
            );
        }

        validateTableReferences(
                normalizedSql,
                normalizedAllowedTables
        );

        return applyRowLimit(normalizedSql);
    }

    private String normalize(String candidateSql) {

        if (candidateSql == null) {
            throw rejected("Mudel ei tagastanud SQL-päringut");
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
            throw rejected("Mudel tagastas tühja SQL-päringu");
        }

        return normalizedSql;
    }

    private void validateTableReferences(
            String normalizedSql,
            Set<String> allowedTables
    ) {
        Set<String> cteNames = findCteNames(normalizedSql);

        Matcher tableReferenceMatcher =
                TABLE_REFERENCE.matcher(normalizedSql);

        boolean hasAllowedTableReference = false;

        String allowedSchemaPrefix =
                databaseSchema.toLowerCase(Locale.ROOT) + ".";

        while (tableReferenceMatcher.find()) {

            String tableReference =
                    tableReferenceMatcher
                            .group(1)
                            .toLowerCase(Locale.ROOT);

            if (cteNames.contains(tableReference)) {
                continue;
            }

            if (!tableReference.startsWith(allowedSchemaPrefix)) {
                throw rejected(
                        "Tabel ei kuulu lubatud rakendusskeemi"
                );
            }

            String tableName = tableReference.substring(
                    allowedSchemaPrefix.length()
            );

            if (!allowedTables.contains(tableName)) {
                throw rejected(
                        "Tabel ei kuulu chatbotile lubatud andmeulatusse"
                );
            }

            hasAllowedTableReference = true;
        }

        if (!hasAllowedTableReference) {
            throw rejected(
                    "Päring ei kasuta ühtegi chatbotile lubatud tabelit"
            );
        }
    }

    private Set<String> findCteNames(String normalizedSql) {

        Set<String> cteNames = new HashSet<>();

        Matcher cteMatcher =
                CTE_NAME.matcher(normalizedSql);

        while (cteMatcher.find()) {
            cteNames.add(
                    cteMatcher
                            .group(1)
                            .toLowerCase(Locale.ROOT)
            );
        }

        return cteNames;
    }

    private Set<String> normalizeAllowedTables(
            Set<String> allowedTables
    ) {
        if (allowedTables == null || allowedTables.isEmpty()) {
            return Set.of();
        }

        String schemaPrefix =
                databaseSchema.toLowerCase(Locale.ROOT) + ".";

        return allowedTables.stream()
                .filter(tableName ->
                        tableName != null && !tableName.isBlank()
                )
                .map(tableName ->
                        tableName.trim().toLowerCase(Locale.ROOT)
                )
                .map(tableName ->
                        tableName.startsWith(schemaPrefix)
                                ? tableName.substring(schemaPrefix.length())
                                : tableName
                )
                .filter(tableName ->
                        !tableName.contains(".")
                )
                .collect(Collectors.toUnmodifiableSet());
    }

    private String applyRowLimit(String validatedSql) {

        int safeMaxRows = Math.max(1, maxRows);

        return "SELECT * FROM ("
                + validatedSql
                + ") AS chatbot_result LIMIT "
                + safeMaxRows;
    }

    private ChatbotException rejected(String reason) {

        log.warn("Chatboti SQL-päring lükati tagasi: {}", reason);

        return new ChatbotException(
                "AI-chatboti teenus ei ole hetkel saadaval",
                "CHATBOT_QUERY_REJECTED",
                HttpStatus.SERVICE_UNAVAILABLE
        );
    }
}