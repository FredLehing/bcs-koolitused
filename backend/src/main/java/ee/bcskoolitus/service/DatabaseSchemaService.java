package ee.bcskoolitus.service;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class DatabaseSchemaService {

    private final JdbcTemplate jdbcTemplate;

    @Value("${chatbot.database-schema}")
    private String databaseSchema;

    /**
     * Koostab chatbotile ainult lubatud tabelitest andmebaasiskeemi kirjelduse.
     * allowedTables peab tulema backendis kontrollitud ChatbotAccessScopeService'ist.
     */
    public String getSchemaContext(Set<String> allowedTables) {

        Set<String> normalizedAllowedTables =
                normalizeAllowedTables(allowedTables);

        Map<String, List<ColumnMetadata>> columnsByTable =
                new LinkedHashMap<>();

        for (ColumnMetadata column : loadColumns()) {

            String normalizedTableName =
                    column.tableName().toLowerCase(Locale.ROOT);

            if (normalizedAllowedTables.contains(normalizedTableName)) {
                columnsByTable
                        .computeIfAbsent(
                                column.tableName(),
                                ignored -> new ArrayList<>()
                        )
                        .add(column);
            }
        }

        StringBuilder schemaContext =
                buildSchemaContext(columnsByTable);

        appendForeignKeys(
                schemaContext,
                normalizedAllowedTables
        );

        return schemaContext.toString();
    }

    private List<ColumnMetadata> loadColumns() {

        return jdbcTemplate.query("""
                        SELECT table_name,
                               column_name,
                               data_type,
                               is_nullable,
                               ordinal_position
                        FROM information_schema.columns
                        WHERE table_schema = ?
                        ORDER BY table_name, ordinal_position
                        """,
                (resultSet, rowNumber) ->
                        new ColumnMetadata(
                                resultSet.getString("table_name"),
                                resultSet.getString("column_name"),
                                resultSet.getString("data_type"),
                                resultSet.getString("is_nullable")
                        ),
                databaseSchema
        );
    }

    private StringBuilder buildSchemaContext(
            Map<String, List<ColumnMetadata>> columnsByTable
    ) {

        StringBuilder schemaContext =
                new StringBuilder("Allowed schema: ")
                        .append(databaseSchema)
                        .append('\n');

        columnsByTable.forEach((tableName, tableColumns) -> {

            schemaContext
                    .append(databaseSchema)
                    .append('.')
                    .append(tableName)
                    .append(":\n");

            tableColumns.forEach(column ->
                    schemaContext
                            .append("- ")
                            .append(column.columnName())
                            .append(' ')
                            .append(column.dataType())
                            .append(' ')
                            .append(
                                    "NO".equals(column.isNullable())
                                            ? "not null"
                                            : "nullable"
                            )
                            .append('\n')
            );
        });

        return schemaContext;
    }

    private void appendForeignKeys(
            StringBuilder schemaContext,
            Set<String> allowedTables
    ) {

        List<ForeignKeyMetadata> allowedForeignKeys =
                loadForeignKeys().stream()
                        .filter(foreignKey ->
                                allowedTables.contains(
                                        foreignKey.tableName()
                                                .toLowerCase(Locale.ROOT)
                                )
                        )
                        .filter(foreignKey ->
                                allowedTables.contains(
                                        foreignKey.referencedTableName()
                                                .toLowerCase(Locale.ROOT)
                                )
                        )
                        .toList();

        if (allowedForeignKeys.isEmpty()) {
            return;
        }

        schemaContext.append("Relations:\n");

        allowedForeignKeys.forEach(foreignKey ->
                schemaContext
                        .append("- ")
                        .append(databaseSchema)
                        .append('.')
                        .append(foreignKey.tableName())
                        .append('.')
                        .append(foreignKey.columnName())
                        .append(" -> ")
                        .append(databaseSchema)
                        .append('.')
                        .append(foreignKey.referencedTableName())
                        .append('.')
                        .append(foreignKey.referencedColumnName())
                        .append('\n')
        );
    }

    private List<ForeignKeyMetadata> loadForeignKeys() {

        return jdbcTemplate.query("""
                        SELECT tc.table_name,
                               kcu.column_name,
                               ccu.table_name AS referenced_table_name,
                               ccu.column_name AS referenced_column_name
                        FROM information_schema.table_constraints tc
                        JOIN information_schema.key_column_usage kcu
                          ON tc.constraint_name = kcu.constraint_name
                         AND tc.table_schema = kcu.table_schema
                        JOIN information_schema.constraint_column_usage ccu
                          ON ccu.constraint_name = tc.constraint_name
                         AND ccu.table_schema = tc.table_schema
                        WHERE tc.constraint_type = 'FOREIGN KEY'
                          AND tc.table_schema = ?
                        ORDER BY tc.table_name, kcu.column_name
                        """,
                (resultSet, rowNumber) ->
                        new ForeignKeyMetadata(
                                resultSet.getString("table_name"),
                                resultSet.getString("column_name"),
                                resultSet.getString("referenced_table_name"),
                                resultSet.getString("referenced_column_name")
                        ),
                databaseSchema
        );
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

    private record ColumnMetadata(
            String tableName,
            String columnName,
            String dataType,
            String isNullable
    ) {
    }

    private record ForeignKeyMetadata(
            String tableName,
            String columnName,
            String referencedTableName,
            String referencedColumnName
    ) {
    }
}