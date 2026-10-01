package ee.bcskoolitus.persistance.chatbot;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.sql.ResultSetMetaData;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Repository
@RequiredArgsConstructor
public class ChatbotQueryRepository {

    private final JdbcTemplate jdbcTemplate;

    @Value("${chatbot.query-timeout-ms}")
    private int queryTimeoutMs;

    @Transactional(readOnly = true)
    public List<Map<String, Object>> executeReadOnlyQuery(String validatedSql) {
        return jdbcTemplate.query(validatedSql,
                statement -> statement.setQueryTimeout(Math.max(1, (int) Math.ceil(queryTimeoutMs / 1000.0))),
                (resultSet, rowNumber) -> toRow(resultSet));
    }

    private Map<String, Object> toRow(java.sql.ResultSet resultSet) throws java.sql.SQLException {
        ResultSetMetaData resultSetMetaData = resultSet.getMetaData();
        Map<String, Object> row = new LinkedHashMap<>();
        for (int columnIndex = 1; columnIndex <= resultSetMetaData.getColumnCount(); columnIndex++) {
            row.put(resultSetMetaData.getColumnLabel(columnIndex), resultSet.getObject(columnIndex));
        }
        return row;
    }
}
