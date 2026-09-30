package ee.bcskoolitus.service;

import ee.bcskoolitus.LecturerStatus;
import ee.bcskoolitus.controller.lecturertranslation.dto.LecturerTranslationDto;
import ee.bcskoolitus.infrastructure.exception.PrimaryKeyNotFoundException;
import ee.bcskoolitus.persistance.language.Language;
import ee.bcskoolitus.persistance.lecturer.Lecturer;
import ee.bcskoolitus.persistance.lecturer.translation.LecturerTranslation;
import ee.bcskoolitus.persistance.lecturer.translation.LecturerTranslationMapper;
import ee.bcskoolitus.persistance.lecturer.translation.LecturerTranslationMapperImpl;
import ee.bcskoolitus.persistance.lecturer.translation.LecturerTranslationRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class LecturerTranslationServiceTest {

    @Mock
    private LecturerTranslationRepository lecturerTranslationRepository;
    @Spy
    private LecturerTranslationMapper lecturerTranslationMapper = new LecturerTranslationMapperImpl();

    @InjectMocks
    private LecturerTranslationService lecturerTranslationService;

    private Lecturer lecturer;

    @BeforeEach
    void setUp() {
        lecturer = new Lecturer();
        lecturer.setId(1);
        Language language = new Language();
        language.setId(2);
        language.setCode("en");
        LecturerTranslation lecturerTranslation = new LecturerTranslation();
        lecturerTranslation.setId(2);
        lecturerTranslation.setLecturer(lecturer);
        lecturerTranslation.setLanguage(language);
        lecturerTranslation.setTitle("Lecturer/consultant");
        lecturerTranslation.setShortDescription("Software development");
        lecturerTranslation.setDescription("<p>Software development</p>");
        when(lecturerTranslationRepository.findById(2)).thenReturn(Optional.of(lecturerTranslation));
    }

    @Test
    void getLecturerTranslation_activeLecturer_returnsTranslation() {
        lecturer.setStatus(LecturerStatus.ACTIVE.getCode());

        LecturerTranslationDto lecturerTranslationDto = lecturerTranslationService.getLecturerTranslation(2);

        assertEquals(new LecturerTranslationDto(2, 1, 2, "en", "Lecturer/consultant", "Software development", "<p>Software development</p>"),
                lecturerTranslationDto);
    }

    @Test
    void getLecturerTranslation_deletedLecturer_throwsPrimaryKeyNotFound() {
        lecturer.setStatus(LecturerStatus.DELETED.getCode());

        PrimaryKeyNotFoundException exception = assertThrows(PrimaryKeyNotFoundException.class,
                () -> lecturerTranslationService.getLecturerTranslation(2));

        assertEquals("Ei leidnud primary keyd 'lecturerTranslationId' väärtusega: 2", exception.getMessage());
    }
}
