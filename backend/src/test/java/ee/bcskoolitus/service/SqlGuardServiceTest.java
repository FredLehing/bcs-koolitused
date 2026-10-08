package ee.bcskoolitus.service;

import ee.bcskoolitus.infrastructure.exception.ChatbotException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class SqlGuardServiceTest {

    private SqlGuardService sqlGuardService;

    private final Set<String> allowedTables = Set.of(
            "location",
            "training"
    );

    @BeforeEach
    void setUp() {
        sqlGuardService = new SqlGuardService();
        ReflectionTestUtils.setField(
                sqlGuardService,
                "databaseSchema",
                "bcs_koolitused"
        );
        ReflectionTestUtils.setField(
                sqlGuardService,
                "maxRows",
                100
        );
    }

    @Test
    void addsLimitToAllowedSchemaQualifiedSelect() {
        String query = sqlGuardService.validateAndLimit(
                "SELECT id, name FROM bcs_koolitused.location",
                allowedTables
        );

        assertEquals(
                "SELECT * FROM (SELECT id, name FROM bcs_koolitused.location) AS chatbot_result LIMIT 100",
                query
        );
    }

    @Test
    void rejectsWriteCommandAndTableOutsideAllowedScope() {
        assertThrows(
                ChatbotException.class,
                () -> sqlGuardService.validateAndLimit(
                        "DELETE FROM bcs_koolitused.training",
                        allowedTables
                )
        );

        assertThrows(
                ChatbotException.class,
                () -> sqlGuardService.validateAndLimit(
                        "SELECT email FROM bcs_koolitused.profile",
                        allowedTables
                )
        );
    }
}