package ee.bcskoolitus.controller.aitraining;

import ee.bcskoolitus.controller.common.dto.AiTrainingContentDto;
import ee.bcskoolitus.service.AiTrainingService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpHeaders;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.web.servlet.MockMvc;

import java.nio.charset.StandardCharsets;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.setup.MockMvcBuilders.standaloneSetup;

// HTTP-leping töötab ilma rakenduse konteksti, andmebaasi ja Gemini API-ta.
class AiTrainingControllerTest {

    private MockMvc mockMvc;
    private AiTrainingService aiTrainingService;

    @BeforeEach
    void setUp() {
        aiTrainingService = mock(AiTrainingService.class);
        mockMvc = standaloneSetup(new AiTrainingController(aiTrainingService)).build();
    }

    @Test
    void selectedPdfReturnsTrainingContentWithoutSavedTraining() throws Exception {
        when(aiTrainingService.createContentFromPdf(any())).thenReturn(new AiTrainingContentDto(
                "PDF-ist genereeritud pealkiri (TO BE IMPLEMENTED)",
                "PDF-ist genereeritud lühikirjeldus (TO BE IMPLEMENTED)",
                "PDF-ist genereeritud kirjeldus (TO BE IMPLEMENTED)"));

        mockMvc.perform(multipart("/api/ai-training/pdf").file(getCurriculumFile()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.title").value("PDF-ist genereeritud pealkiri (TO BE IMPLEMENTED)"))
                .andExpect(jsonPath("$.shortDescription").isString())
                .andExpect(jsonPath("$.description").isString());
    }

    @Test
    void selectedPdfEndpointRequiresCurriculumPart() throws Exception {
        mockMvc.perform(multipart("/api/ai-training/pdf"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void updateAcceptsReplacementPdf() throws Exception {
        mockMvc.perform(multipart("/api/ai-training/pdf/123").file(getCurriculumFile()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.description").value("PDF-ist genereeritud kirjeldus (TO BE IMPLEMENTED)"));
    }

    @Test
    void updateAcceptsMultipartWithoutReplacementFile() throws Exception {
        mockMvc.perform(multipart("/api/ai-training/pdf/123"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.title").isString());
    }

    @Test
    void translationUsesPostAndTargetLanguageQueryWithoutDatabaseAccess() throws Exception {
        mockMvc.perform(post("/api/ai-training/translation/123").param("languageId", "2"))
                .andExpect(status().isOk())
                .andExpect(header().string(HttpHeaders.CACHE_CONTROL, "no-store"))
                .andExpect(jsonPath("$.title").value("AI-ga tõlgitud pealkiri (TO BE IMPLEMENTED)"))
                .andExpect(jsonPath("$.shortDescription").isString())
                .andExpect(jsonPath("$.description").isString());
    }

    @Test
    void translationRequiresTargetLanguageQuery() throws Exception {
        mockMvc.perform(post("/api/ai-training/translation/123"))
                .andExpect(status().isBadRequest());
    }

    private MockMultipartFile getCurriculumFile() {
        return new MockMultipartFile("curriculum", "oppekava.pdf", "application/pdf",
                "%PDF-1.7\nplaceholder".getBytes(StandardCharsets.US_ASCII));
    }
}
