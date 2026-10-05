package ee.bcskoolitus.service;

import ee.bcskoolitus.TrainingStatus;
import ee.bcskoolitus.controller.common.dto.LecturerDto;
import ee.bcskoolitus.controller.training.dto.TrainingCreateRequestDto;
import ee.bcskoolitus.controller.training.dto.TrainingDto;
import ee.bcskoolitus.infrastructure.exception.PrimaryKeyNotFoundException;
import ee.bcskoolitus.persistance.lecturer.Lecturer;
import ee.bcskoolitus.persistance.training.Training;
import ee.bcskoolitus.persistance.training.TrainingMapper;
import ee.bcskoolitus.persistance.training.TrainingMapperImpl;
import ee.bcskoolitus.persistance.training.TrainingRepository;
import ee.bcskoolitus.persistance.training.fundingtype.TrainingFundingTypeRepository;
import ee.bcskoolitus.persistance.training.lecturer.TrainingLecturer;
import ee.bcskoolitus.persistance.training.lecturer.TrainingLecturerMapper;
import ee.bcskoolitus.persistance.training.lecturer.TrainingLecturerMapperImpl;
import ee.bcskoolitus.persistance.training.lecturer.TrainingLecturerRepository;
import ee.bcskoolitus.persistance.training.translation.TrainingTranslationMapper;
import ee.bcskoolitus.persistance.training.translation.TrainingTranslationMapperImpl;
import ee.bcskoolitus.persistance.training.translation.TrainingTranslationRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

// Koolituse koolitajad (training_lecturer): GET tagastab sort_order järjekorras, POST salvestab lecturerIds järjekorras
// (uus koolitus — seotud koolitajaid pole, seega peavad kõik olema aktiivsed)
@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class TrainingServiceLecturersTest {

    private static final Integer TRAINING_ID = 1;

    @Mock
    private TrainingRepository trainingRepository;
    @Mock
    private TrainingFundingTypeRepository trainingFundingTypeRepository;
    @Mock
    private TrainingTranslationRepository trainingTranslationRepository;
    @Mock
    private TrainingLecturerRepository trainingLecturerRepository;
    @Mock
    private UserService userService;
    @Mock
    private CategoryService categoryService;
    @Mock
    private LanguageService languageService;
    @Mock
    private LocationService locationService;
    @Mock
    private LecturerService lecturerService;
    @Mock
    private TrainingTranslationCurriculumService trainingTranslationCurriculumService;
    @Spy
    private TrainingMapper trainingMapper = new TrainingMapperImpl();
    @Spy
    private TrainingTranslationMapper trainingTranslationMapper = new TrainingTranslationMapperImpl();
    @Spy
    private TrainingLecturerMapper trainingLecturerMapper = new TrainingLecturerMapperImpl();

    @InjectMocks
    private TrainingService trainingService;

    @Test
    void getTraining_returnsLecturersInSortOrder() {
        Training training = new Training();
        training.setId(TRAINING_ID);
        training.setStatus(TrainingStatus.PUBLISHED.getCode());
        when(trainingRepository.findById(TRAINING_ID)).thenReturn(Optional.of(training));
        when(trainingLecturerRepository.findTrainingLecturersBy(TRAINING_ID)).thenReturn(List.of(
                createTrainingLecturer(training, createLecturer(1, "Rain Tüür"), 1),
                createTrainingLecturer(training, createLecturer(8, "Meelis Teern"), 2)));

        TrainingDto trainingDto = trainingService.getTraining(TRAINING_ID);

        assertEquals(List.of(new LecturerDto(1, "Rain Tüür"), new LecturerDto(8, "Meelis Teern")), trainingDto.getLecturers());
    }

    @Test
    void getTraining_withoutLecturersReturnsEmptyList() {
        Training training = new Training();
        training.setId(TRAINING_ID);
        training.setStatus(TrainingStatus.UNPUBLISHED.getCode());
        when(trainingRepository.findById(TRAINING_ID)).thenReturn(Optional.of(training));
        when(trainingLecturerRepository.findTrainingLecturersBy(TRAINING_ID)).thenReturn(List.of());

        TrainingDto trainingDto = trainingService.getTraining(TRAINING_ID);

        assertEquals(List.of(), trainingDto.getLecturers());
    }

    @Test
    void addTraining_savesActiveLecturersInGivenOrder() {
        when(lecturerService.getValidAssignableLecturerBy(8, List.of())).thenReturn(createLecturer(8, "Meelis Teern"));
        when(lecturerService.getValidAssignableLecturerBy(1, List.of())).thenReturn(createLecturer(1, "Rain Tüür"));

        trainingService.addTraining(createTrainingCreateRequestDto(List.of(8, 1)));

        ArgumentCaptor<TrainingLecturer> trainingLecturerCaptor = ArgumentCaptor.forClass(TrainingLecturer.class);
        verify(trainingLecturerRepository, times(2)).save(trainingLecturerCaptor.capture());
        List<TrainingLecturer> savedTrainingLecturers = trainingLecturerCaptor.getAllValues();
        assertEquals(List.of(8, 1), savedTrainingLecturers.stream()
                .map(trainingLecturer -> trainingLecturer.getLecturer().getId())
                .toList());
        assertEquals(List.of(1, 2), savedTrainingLecturers.stream()
                .map(TrainingLecturer::getSortOrder)
                .toList());
    }

    @Test
    void addTraining_deletedLecturerThrows() {
        when(lecturerService.getValidAssignableLecturerBy(4, List.of()))
                .thenThrow(new PrimaryKeyNotFoundException("lecturerId", 4));

        PrimaryKeyNotFoundException exception = assertThrows(PrimaryKeyNotFoundException.class,
                () -> trainingService.addTraining(createTrainingCreateRequestDto(List.of(4))));

        assertEquals("Ei leidnud primary keyd 'lecturerId' väärtusega: 4", exception.getMessage());
        verify(trainingLecturerRepository, never()).save(any());
    }

    private static TrainingCreateRequestDto createTrainingCreateRequestDto(List<Integer> lecturerIds) {
        return new TrainingCreateRequestDto(1, 1, 1, 1, lecturerIds, true, false,
                "Java algkursus", "Java alused.", "<p>Java alused.</p>", List.of(), null, "Õppekava");
    }

    private static Lecturer createLecturer(Integer lecturerId, String fullName) {
        Lecturer lecturer = new Lecturer();
        lecturer.setId(lecturerId);
        lecturer.setFullName(fullName);
        return lecturer;
    }

    private static TrainingLecturer createTrainingLecturer(Training training, Lecturer lecturer, Integer sortOrder) {
        TrainingLecturer trainingLecturer = new TrainingLecturer();
        trainingLecturer.setTraining(training);
        trainingLecturer.setLecturer(lecturer);
        trainingLecturer.setSortOrder(sortOrder);
        return trainingLecturer;
    }
}
