package ee.bcskoolitus.service;

import ee.bcskoolitus.CourseStatus;
import ee.bcskoolitus.LecturerStatus;
import ee.bcskoolitus.TrainingStatus;
import ee.bcskoolitus.controller.common.dto.LecturerDto;
import ee.bcskoolitus.controller.lecturer.dto.AdminLecturerSummaryDto;
import ee.bcskoolitus.controller.lecturer.dto.LecturerCreateRequestDto;
import ee.bcskoolitus.controller.lecturer.dto.LecturerCreateResponseDto;
import ee.bcskoolitus.controller.lecturer.dto.LecturerDetailDto;
import ee.bcskoolitus.controller.lecturer.dto.LecturerProfileDto;
import ee.bcskoolitus.controller.common.dto.LecturerSummaryDto;
import ee.bcskoolitus.controller.lecturer.dto.LecturerTrainingDto;
import ee.bcskoolitus.controller.lecturer.dto.LecturerTranslationCreateRequestDto;
import ee.bcskoolitus.controller.lecturer.dto.LecturerTranslationCreateResponseDto;
import ee.bcskoolitus.controller.lecturer.dto.LecturerTranslationItemDto;
import ee.bcskoolitus.controller.lecturer.dto.LecturerUpdateRequestDto;
import ee.bcskoolitus.infrastructure.exception.ForbiddenException;
import ee.bcskoolitus.infrastructure.exception.PrimaryKeyNotFoundException;
import ee.bcskoolitus.infrastructure.util.HtmlSanitizer;
import ee.bcskoolitus.persistance.course.lecturer.CourseLecturerRepository;
import ee.bcskoolitus.persistance.language.Language;
import ee.bcskoolitus.persistance.lecturer.Lecturer;
import ee.bcskoolitus.persistance.lecturer.LecturerMapper;
import ee.bcskoolitus.persistance.lecturer.LecturerRepository;
import ee.bcskoolitus.persistance.lecturer.translation.LecturerTranslation;
import ee.bcskoolitus.persistance.lecturer.translation.LecturerTranslationMapper;
import ee.bcskoolitus.persistance.lecturer.translation.LecturerTranslationRepository;
import ee.bcskoolitus.persistance.training.lecturer.TrainingLecturerRepository;
import ee.bcskoolitus.persistance.view.adminlecturersummary.AdminLecturerSummary;
import ee.bcskoolitus.persistance.view.adminlecturersummary.AdminLecturerSummaryMapper;
import ee.bcskoolitus.persistance.view.adminlecturersummary.AdminLecturerSummaryRepository;
import ee.bcskoolitus.persistance.view.admintrainingsummary.AdminTrainingSummary;
import ee.bcskoolitus.persistance.view.admintrainingsummary.AdminTrainingSummaryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import static ee.bcskoolitus.Error.LECTURER_HAS_UPCOMING_COURSES;
import static ee.bcskoolitus.Error.TRANSLATION_EXISTS;

@Service
@RequiredArgsConstructor
public class LecturerService {

    // Tühistatud ja kustutatud toimumiskorrad ei ole "tulevased" (kustutamist ei takista)
    private static final List<String> NOT_UPCOMING_COURSE_STATUSES =
            List.of(CourseStatus.CANCELLED.getCode(), CourseStatus.DELETED.getCode());

    private final LecturerRepository lecturerRepository;
    private final LecturerMapper lecturerMapper;
    private final LecturerTranslationRepository lecturerTranslationRepository;
    private final LecturerTranslationMapper lecturerTranslationMapper;
    private final LecturerTranslationService lecturerTranslationService;
    private final LecturerPhotoService lecturerPhotoService;
    private final CourseLecturerRepository courseLecturerRepository;
    private final TrainingLecturerRepository trainingLecturerRepository;
    private final AdminLecturerSummaryRepository adminLecturerSummaryRepository;
    private final AdminLecturerSummaryMapper adminLecturerSummaryMapper;
    private final AdminTrainingSummaryRepository adminTrainingSummaryRepository;
    private final UserService userService;
    private final LanguageService languageService;

    // Ainult aktiivsed — kustutatud koolitajat ei saa "Vali koolitaja" modalis valida
    public List<LecturerDto> findLecturers(String search) {
        List<Lecturer> lecturers = lecturerRepository.findLecturersBy(search.trim(), LecturerStatus.ACTIVE.getCode());
        return lecturerMapper.toLecturerDtos(lecturers);
    }

    // Leiab ka kustutatud koolitaja — kasutavad delete ja restore
    public Lecturer getValidLecturerBy(Integer lecturerId, String fieldName) {
        return lecturerRepository.findById(lecturerId)
                .orElseThrow(() -> new PrimaryKeyNotFoundException(fieldName, lecturerId));
    }

    // Kustutatud koolitaja (status D) on nagu olematu → 404
    public Lecturer getValidActiveLecturerBy(Integer lecturerId, String fieldName) {
        Lecturer lecturer = getValidLecturerBy(lecturerId, fieldName);
        if (LecturerStatus.DELETED.getCode().equals(lecturer.getStatus())) {
            throw new PrimaryKeyNotFoundException(fieldName, lecturerId);
        }
        return lecturer;
    }

    // Koolitusele / toimumiskorrale määratav koolitaja: uus peab olema aktiivne, juba seotud
    // (linkedLecturerIds) võib olla ka kustutatud, et muid välju saaks edasi salvestada
    public Lecturer getValidAssignableLecturerBy(Integer lecturerId, List<Integer> linkedLecturerIds) {
        if (linkedLecturerIds.contains(lecturerId)) {
            return getValidLecturerBy(lecturerId, "lecturerId");
        }
        return getValidActiveLecturerBy(lecturerId, "lecturerId");
    }

    // Admini nimekiri: title ja lecturerTranslationId contentLang keeles (view annab puudumisel põhikeele omad)
    public List<AdminLecturerSummaryDto> findAdminLecturers(String contentLang, Boolean includeDeleted) {
        List<AdminLecturerSummary> adminLecturerSummaries = Boolean.TRUE.equals(includeDeleted)
                ? adminLecturerSummaryRepository.findAllByContentLanguageCodeOrderByFullNameAscLecturerIdAsc(contentLang)
                : adminLecturerSummaryRepository.findAllByContentLanguageCodeAndStatusOrderByFullNameAscLecturerIdAsc(contentLang, LecturerStatus.ACTIVE.getCode());
        return adminLecturerSummaryMapper.toAdminLecturerSummaryDtos(adminLecturerSummaries);
    }

    public LecturerDetailDto getLecturer(Integer lecturerId) {
        Lecturer lecturer = getValidActiveLecturerBy(lecturerId, "lecturerId");
        return new LecturerDetailDto(lecturer.getId(), lecturer.getFullName(), lecturerPhotoService.findPhotoVersion(lecturerId));
    }

    @Transactional(readOnly = true)
    public List<LecturerTranslationItemDto> getLecturerTranslations(Integer lecturerId) {
        getValidActiveLecturerBy(lecturerId, "lecturerId");
        List<LecturerTranslation> lecturerTranslations = lecturerTranslationRepository.findAllByLecturer_IdOrderByLanguage_IdAsc(lecturerId);
        return lecturerTranslationMapper.toLecturerTranslationItemDtos(lecturerTranslations);
    }

    // Loob koolitaja (status A), pildi (kui on) ja põhikeele tõlke ühes transaktsioonis
    @Transactional
    public LecturerCreateResponseDto addLecturer(LecturerCreateRequestDto lecturerCreateRequestDto) {
        Lecturer lecturer = createLecturer(lecturerCreateRequestDto);
        lecturerRepository.save(lecturer);
        if (lecturerCreateRequestDto.getPhoto() != null) {
            lecturerPhotoService.saveLecturerPhoto(lecturer, lecturerCreateRequestDto.getPhoto(), lecturerCreateRequestDto.getPhotoContentType());
        }
        LecturerTranslation lecturerTranslation = lecturerTranslationMapper.toLecturerTranslation(lecturerCreateRequestDto);
        lecturerTranslation.setDescription(HtmlSanitizer.sanitizeDescription(lecturerCreateRequestDto.getDescription()));
        lecturerTranslation.setLecturer(lecturer);
        lecturerTranslation.setLanguage(languageService.getMainLanguage());
        lecturerTranslationRepository.save(lecturerTranslation);
        return new LecturerCreateResponseDto(lecturer.getId(), lecturerTranslation.getId());
    }

    private Lecturer createLecturer(LecturerCreateRequestDto lecturerCreateRequestDto) {
        Lecturer lecturer = new Lecturer();
        lecturer.setFullName(lecturerCreateRequestDto.getFullName());
        lecturer.setStatus(LecturerStatus.ACTIVE.getCode());
        lecturer.setCreatedBy(userService.getValidUserBy(lecturerCreateRequestDto.getUserId()));
        return lecturer;
    }

    // Muudab nime, pilti ja avatud tõlget ühes transaktsioonis.
    // Pilt: photo ≠ null → lisatakse/asendatakse; isPhotoRemoved → eemaldatakse; muidu ei muutu.
    @Transactional
    public void updateLecturer(Integer lecturerId, LecturerUpdateRequestDto lecturerUpdateRequestDto) {
        Lecturer lecturer = getValidActiveLecturerBy(lecturerId, "lecturerId");
        LecturerTranslation lecturerTranslation = lecturerTranslationService.getValidLecturerTranslationBy(
                lecturerUpdateRequestDto.getLecturerTranslation().getLecturerTranslationId(), lecturerId);
        lecturer.setFullName(lecturerUpdateRequestDto.getFullName());
        lecturerRepository.save(lecturer);
        handleUpdatePhoto(lecturer, lecturerUpdateRequestDto);
        lecturerTranslationMapper.updateLecturerTranslation(lecturerUpdateRequestDto.getLecturerTranslation(), lecturerTranslation);
        lecturerTranslation.setDescription(HtmlSanitizer.sanitizeDescription(lecturerUpdateRequestDto.getLecturerTranslation().getDescription()));
        lecturerTranslationRepository.save(lecturerTranslation);
    }

    private void handleUpdatePhoto(Lecturer lecturer, LecturerUpdateRequestDto lecturerUpdateRequestDto) {
        if (Boolean.TRUE.equals(lecturerUpdateRequestDto.getIsPhotoRemoved())) {
            lecturerPhotoService.deleteLecturerPhoto(lecturer.getId());
        } else if (lecturerUpdateRequestDto.getPhoto() != null) {
            lecturerPhotoService.saveLecturerPhoto(lecturer, lecturerUpdateRequestDto.getPhoto(), lecturerUpdateRequestDto.getPhotoContentType());
        }
    }

    @Transactional
    public LecturerTranslationCreateResponseDto addLecturerTranslation(Integer lecturerId, LecturerTranslationCreateRequestDto lecturerTranslationCreateRequestDto) {
        Lecturer lecturer = getValidActiveLecturerBy(lecturerId, "lecturerId");
        Language language = languageService.getValidLanguageBy(lecturerTranslationCreateRequestDto.getLanguageId(), "languageId");
        validateTranslationDoesNotExist(lecturerId, language.getId());
        LecturerTranslation lecturerTranslation = lecturerTranslationMapper.toLecturerTranslation(lecturerTranslationCreateRequestDto);
        lecturerTranslation.setDescription(HtmlSanitizer.sanitizeDescription(lecturerTranslationCreateRequestDto.getDescription()));
        lecturerTranslation.setLecturer(lecturer);
        lecturerTranslation.setLanguage(language);
        lecturerTranslationRepository.save(lecturerTranslation);
        return new LecturerTranslationCreateResponseDto(lecturerTranslation.getId());
    }

    // Kontroll enne salvestamist — muidu annaks lecturer_translation_uq andmebaasi vea (500)
    private void validateTranslationDoesNotExist(Integer lecturerId, Integer languageId) {
        if (lecturerTranslationRepository.existsByLecturer_IdAndLanguage_Id(lecturerId, languageId)) {
            throw new ForbiddenException(TRANSLATION_EXISTS.getMessage(), TRANSLATION_EXISTS.name());
        }
    }

    // Soft delete: ainult status = D; tõlked, pilt ja seosed jäävad alles. Korduv kustutamine ei muuda midagi.
    @Transactional
    public void deleteLecturer(Integer lecturerId) {
        Lecturer lecturer = getValidLecturerBy(lecturerId, "lecturerId");
        if (LecturerStatus.DELETED.getCode().equals(lecturer.getStatus())) {
            return;
        }
        validateLecturerHasNoUpcomingCourses(lecturerId);
        lecturer.setStatus(LecturerStatus.DELETED.getCode());
        lecturerRepository.save(lecturer);
    }

    private void validateLecturerHasNoUpcomingCourses(Integer lecturerId) {
        long upcomingCourseCount = courseLecturerRepository.countUpcomingCourseLecturersBy(lecturerId, LocalDate.now(), NOT_UPCOMING_COURSE_STATUSES);
        if (upcomingCourseCount > 0) {
            throw new ForbiddenException(LECTURER_HAS_UPCOMING_COURSES.getMessage(), LECTURER_HAS_UPCOMING_COURSES.name());
        }
    }

    // Aktiivse koolitaja korral midagi ei muutu
    @Transactional
    public void restoreLecturer(Integer lecturerId) {
        Lecturer lecturer = getValidLecturerBy(lecturerId, "lecturerId");
        if (LecturerStatus.ACTIVE.getCode().equals(lecturer.getStatus())) {
            return;
        }
        lecturer.setStatus(LecturerStatus.ACTIVE.getCode());
        lecturerRepository.save(lecturer);
    }

    // Koolitaja kaart (LecturerCard): tekstid contentLang keeles, puudumisel põhikeeles
    @Transactional(readOnly = true)
    public LecturerSummaryDto getLecturerSummary(Integer lecturerId, String contentLang) {
        Lecturer lecturer = getValidActiveLecturerBy(lecturerId, "lecturerId");
        return createLecturerSummaryDtos(List.of(lecturer), contentLang).getFirst();
    }

    // "Meie koolitajad": aktiivsed koolitajad nime järgi
    @Transactional(readOnly = true)
    public List<LecturerSummaryDto> findLecturerSummaries(String contentLang) {
        List<Lecturer> lecturers = lecturerRepository.findAllByStatusOrderByFullNameAscIdAsc(LecturerStatus.ACTIVE.getCode());
        return createLecturerSummaryDtos(lecturers, contentLang);
    }

    // Seotud koolitajad on juba sortOrder järjekorras ja join fetch abil laetud.
    // Tõlked ja fotode versioonid loetakse hulgi; kustutatud koolitaja kaarti ei kuvata.
    @Transactional(readOnly = true)
    public List<LecturerSummaryDto> findLecturerSummariesBy(List<Lecturer> lecturers, String contentLang) {
        List<Lecturer> activeLecturers = lecturers.stream()
                .filter(lecturer -> LecturerStatus.ACTIVE.getCode().equals(lecturer.getStatus()))
                .toList();
        return createLecturerSummaryDtos(activeLecturers, contentLang);
    }

    private List<LecturerSummaryDto> createLecturerSummaryDtos(List<Lecturer> lecturers, String contentLang) {
        if (lecturers.isEmpty()) {
            return List.of();
        }
        List<Integer> lecturerIds = lecturers.stream().map(Lecturer::getId).toList();
        Map<Integer, LecturerTranslation> displayTranslations = findDisplayTranslations(lecturerIds, contentLang);
        Map<Integer, Long> photoVersions = lecturerPhotoService.findPhotoVersions(lecturerIds);
        return lecturers.stream()
                .map(lecturer -> createLecturerSummaryDto(lecturer, displayTranslations.get(lecturer.getId()), photoVersions.get(lecturer.getId())))
                .toList();
    }

    private static LecturerSummaryDto createLecturerSummaryDto(Lecturer lecturer, LecturerTranslation lecturerTranslation, Long photoVersion) {
        LecturerSummaryDto lecturerSummaryDto = new LecturerSummaryDto();
        lecturerSummaryDto.setLecturerId(lecturer.getId());
        lecturerSummaryDto.setFullName(lecturer.getFullName());
        lecturerSummaryDto.setPhotoVersion(photoVersion);
        if (lecturerTranslation != null) {
            lecturerSummaryDto.setTitle(lecturerTranslation.getTitle());
            lecturerSummaryDto.setShortDescription(lecturerTranslation.getShortDescription());
        }
        return lecturerSummaryDto;
    }

    // lecturerId → contentLang keele tõlge, puudumisel põhikeele tõlge (päring järjestab contentLang ette)
    private Map<Integer, LecturerTranslation> findDisplayTranslations(List<Integer> lecturerIds, String contentLang) {
        Map<Integer, LecturerTranslation> displayTranslations = new LinkedHashMap<>();
        if (lecturerIds.isEmpty()) {
            return displayTranslations;
        }
        for (LecturerTranslation lecturerTranslation : lecturerTranslationRepository.findDisplayLecturerTranslationsBy(lecturerIds, contentLang)) {
            displayTranslations.putIfAbsent(lecturerTranslation.getLecturer().getId(), lecturerTranslation);
        }
        return displayTranslations;
    }

    // Avalik profiil: kirjeldus puhastatud, koolitused ainult publitseeritud (training_lecturer kaudu) nime järgi
    @Transactional(readOnly = true)
    public LecturerProfileDto getLecturerProfile(Integer lecturerId, String contentLang) {
        Lecturer lecturer = getValidActiveLecturerBy(lecturerId, "lecturerId");
        LecturerTranslation lecturerTranslation = findDisplayTranslations(List.of(lecturerId), contentLang).get(lecturerId);
        LecturerProfileDto lecturerProfileDto = new LecturerProfileDto();
        lecturerProfileDto.setLecturerId(lecturer.getId());
        lecturerProfileDto.setFullName(lecturer.getFullName());
        lecturerProfileDto.setPhotoVersion(lecturerPhotoService.findPhotoVersion(lecturerId));
        if (lecturerTranslation != null) {
            lecturerProfileDto.setTitle(lecturerTranslation.getTitle());
            lecturerProfileDto.setShortDescription(lecturerTranslation.getShortDescription());
            lecturerProfileDto.setDescription(HtmlSanitizer.sanitizeDescription(lecturerTranslation.getDescription()));
        }
        lecturerProfileDto.setTrainings(findPublishedLecturerTrainings(lecturerId, contentLang));
        return lecturerProfileDto;
    }

    private List<LecturerTrainingDto> findPublishedLecturerTrainings(Integer lecturerId, String contentLang) {
        List<Integer> trainingIds = trainingLecturerRepository.findTrainingIdsBy(lecturerId);
        if (trainingIds.isEmpty()) {
            return List.of();
        }
        List<AdminTrainingSummary> adminTrainingSummaries = adminTrainingSummaryRepository
                .findAllByContentLanguageCodeAndStatusAndTraining_IdInOrderByTitleAsc(contentLang, TrainingStatus.PUBLISHED.getCode(), trainingIds);
        return adminTrainingSummaries.stream()
                .map(adminTrainingSummary -> new LecturerTrainingDto(adminTrainingSummary.getTraining().getId(),
                        adminTrainingSummary.getTrainingTranslationId(), adminTrainingSummary.getTitle()))
                .toList();
    }
}
