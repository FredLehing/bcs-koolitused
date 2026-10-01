package ee.bcskoolitus.controller.adminfeedback;

import ee.bcskoolitus.controller.adminfeedback.dto.AdminFeedbackFilterDto;
import ee.bcskoolitus.controller.adminfeedback.dto.AdminFeedbackPageDto;
import ee.bcskoolitus.infrastructure.RestExceptionHandler;
import ee.bcskoolitus.infrastructure.exception.ConflictException;
import ee.bcskoolitus.infrastructure.exception.IncorrectInputException;
import ee.bcskoolitus.infrastructure.exception.PrimaryKeyNotFoundException;
import ee.bcskoolitus.service.AdminFeedbackService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;
import java.util.Map;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static org.springframework.test.web.servlet.setup.MockMvcBuilders.standaloneSetup;

class AdminFeedbackControllerTest {
    private MockMvc mockMvc;
    private AdminFeedbackService adminFeedbackService;
    @BeforeEach
    void setUp() {
        adminFeedbackService = mock(AdminFeedbackService.class);
        mockMvc = standaloneSetup(new AdminFeedbackController(adminFeedbackService)).setControllerAdvice(new RestExceptionHandler()).build();
        when(adminFeedbackService.getAdminFeedbackPage(any())).thenAnswer(invocation -> {
            AdminFeedbackFilterDto filter = invocation.getArgument(0); filter.validate();
            return new AdminFeedbackPageDto(filter.pageValue(), 0, 0L, 0L, 0L, null, null, List.of(), List.of());
        });
        doAnswer(invocation -> {
            String version = invocation.getArgument(1);
            if (version == null || !version.matches("[0-9a-f]{64}")) throw new IncorrectInputException("X-Answers-Version");
            return null;
        }).when(adminFeedbackService).reviewAdminFeedback(anyInt(), nullable(String.class));
    }
    @Test
    void defaultPaginationAndOptionalLanguageBindCorrectly() throws Exception {
        mockMvc.perform(get("/api/admin-feedbacks"))
                .andExpect(status().isOk()).andExpect(jsonPath("$.page").value(0)).andExpect(jsonPath("$.content").isEmpty());
        mockMvc.perform(get("/api/admin-feedback-courses")).andExpect(status().isOk());
        verify(adminFeedbackService).getAdminFeedbackCourses(null);
    }
    @Test
    void invalidQueriesReturnSame400ShapeIncludingTypeConversion() throws Exception {
        for (Map.Entry<String, String> invalid : List.of(
                Map.entry("page", "-1"), Map.entry("limit", "101"), Map.entry("courseId", "0"),
                Map.entry("low", "4"), Map.entry("from", "2026-02-30"), Map.entry("until", "bad"),
                Map.entry("status", "X"), Map.entry("comments", "true"), Map.entry("sortBy", "injection"),
                Map.entry("sortDirection", "down"), Map.entry("page", "not-a-number"))) {
            mockMvc.perform(get("/api/admin-feedbacks").param(invalid.getKey(), invalid.getValue()))
                    .andDo(result -> { if (result.getResponse().getStatus() == 200) throw new AssertionError("Valideerimata sisend: " + invalid); })
                    .andExpect(status().isBadRequest()).andExpect(jsonPath("$.errorCode").value("INCORRECT_INPUT"))
                    .andExpect(jsonPath("$.message").value(invalid.getKey() + ": vigane väärtus"));
        }
    }
    @Test
    void invalidPathAndMissingHeaderReturn400() throws Exception {
        mockMvc.perform(get("/api/admin-feedback/no-number")).andExpect(status().isBadRequest()).andExpect(jsonPath("$.errorCode").value("INCORRECT_INPUT"));
        mockMvc.perform(put("/api/admin-feedback/3/review")).andExpect(status().isBadRequest()).andExpect(jsonPath("$.message").value("X-Answers-Version: vigane väärtus"));
        mockMvc.perform(put("/api/admin-feedback/3/review").header("X-Answers-Version", "bad")).andExpect(status().isBadRequest());
    }
    @Test
    void reviewSuccessIsEmptyAndConflictAndMissingIdsUseApiError() throws Exception {
        String version = "a".repeat(64);
        mockMvc.perform(put("/api/admin-feedback/3/review").header("X-Answers-Version", version)).andExpect(status().isOk()).andExpect(content().string(""));
        doThrow(new ConflictException("Tagasiside vastused on vahepeal muutunud. Vaata vastused uuesti üle.", "FEEDBACK_ANSWERS_CHANGED"))
                .when(adminFeedbackService).reviewAdminFeedback(3, version);
        mockMvc.perform(put("/api/admin-feedback/3/review").header("X-Answers-Version", version)).andExpect(status().isConflict()).andExpect(jsonPath("$.errorCode").value("FEEDBACK_ANSWERS_CHANGED"));
        when(adminFeedbackService.getAdminFeedback(123, null)).thenThrow(new PrimaryKeyNotFoundException("feedbackId", 123));
        mockMvc.perform(get("/api/admin-feedback/123")).andExpect(status().isNotFound()).andExpect(jsonPath("$.errorCode").value("PRIMARY_KEY_NOT_FOUND"));
    }
}
