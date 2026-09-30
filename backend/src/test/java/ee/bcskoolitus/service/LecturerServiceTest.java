package ee.bcskoolitus.service;

import ee.bcskoolitus.LecturerStatus;
import ee.bcskoolitus.infrastructure.exception.PrimaryKeyNotFoundException;
import ee.bcskoolitus.persistance.lecturer.Lecturer;
import ee.bcskoolitus.persistance.lecturer.LecturerRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.when;

// Kustutatud koolitaja (status D) on getValidActiveLecturerBy jaoks nagu olematu; getValidLecturerBy leiab ta üles
@ExtendWith(MockitoExtension.class)
class LecturerServiceTest {

    private static final Integer LECTURER_ID = 4;

    @Mock
    private LecturerRepository lecturerRepository;

    @InjectMocks
    private LecturerService lecturerService;

    private Lecturer lecturer;

    @BeforeEach
    void setUp() {
        lecturer = new Lecturer();
        lecturer.setId(LECTURER_ID);
        when(lecturerRepository.findById(LECTURER_ID)).thenReturn(Optional.of(lecturer));
    }

    @Test
    void getValidActiveLecturerBy_activeLecturer_returnsLecturer() {
        lecturer.setStatus(LecturerStatus.ACTIVE.getCode());

        assertSame(lecturer, lecturerService.getValidActiveLecturerBy(LECTURER_ID, "lecturerId"));
    }

    @Test
    void getValidActiveLecturerBy_deletedLecturer_throwsPrimaryKeyNotFound() {
        lecturer.setStatus(LecturerStatus.DELETED.getCode());

        PrimaryKeyNotFoundException exception = assertThrows(PrimaryKeyNotFoundException.class,
                () -> lecturerService.getValidActiveLecturerBy(LECTURER_ID, "lecturerId"));

        assertEquals("Ei leidnud primary keyd 'lecturerId' väärtusega: 4", exception.getMessage());
    }

    @Test
    void getValidLecturerBy_deletedLecturer_returnsLecturer() {
        lecturer.setStatus(LecturerStatus.DELETED.getCode());

        assertSame(lecturer, lecturerService.getValidLecturerBy(LECTURER_ID, "lecturerId"));
    }
}
