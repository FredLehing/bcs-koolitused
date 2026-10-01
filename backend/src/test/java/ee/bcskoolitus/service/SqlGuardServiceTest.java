package ee.bcskoolitus.service;

import ee.bcskoolitus.infrastructure.exception.ChatbotException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class SqlGuardServiceTest {

    private SqlGuardService sqlGuardService;

    @BeforeEach
    void setUp() {
        sqlGuardService = new SqlGuardService();
        ReflectionTestUtils.setField(sqlGuardService, "databaseSchema", "bcs_koolitused");
        ReflectionTestUtils.setField(sqlGuardService, "maxRows", 100);
    }

    @Test
    void addsLimitToAllowedSchemaQualifiedSelect() {
        String query = sqlGuardService.validateAndLimit("SELECT id, name FROM bcs_koolitused.location");

        assertEquals("SELECT id, name FROM bcs_koolitused.location LIMIT 100", query);
    }

    @Test
    void rejectsWriteCommandAndSensitiveTable() {
        assertThrows(ChatbotException.class,
                () -> sqlGuardService.validateAndLimit("DELETE FROM bcs_koolitused.training"));
        assertThrows(ChatbotException.class,
                () -> sqlGuardService.validateAndLimit("SELECT email FROM bcs_koolitused.profile"));
    }
}
