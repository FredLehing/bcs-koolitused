package ee.bcskoolitus.service;

import ee.bcskoolitus.CourseStatus;
import ee.bcskoolitus.TrainingStatus;
import ee.bcskoolitus.controller.course.dto.AdminCourseDto;
import ee.bcskoolitus.controller.course.dto.AdminCourseFilterDto;
import ee.bcskoolitus.controller.course.dto.AdminCourseSummaryDto;
import ee.bcskoolitus.controller.course.dto.AdminCourseSummaryItemDto;
import ee.bcskoolitus.controller.course.dto.CourseCreateRequestDto;
import ee.bcskoolitus.controller.course.dto.CoursePageDto;
import ee.bcskoolitus.controller.course.dto.CourseSummaryPageDto;
import ee.bcskoolitus.controller.course.dto.PublicCourseFilterDto;
import ee.bcskoolitus.controller.course.dto.PublicCourseSummaryItemDto;
import ee.bcskoolitus.controller.course.dto.NextCourseFilterDto;
import ee.bcskoolitus.controller.course.dto.CourseDto;
import ee.bcskoolitus.controller.course.dto.CourseSummaryDto;
import ee.bcskoolitus.controller.course.dto.CourseUpdateRequestDto;
import ee.bcskoolitus.infrastructure.exception.ForbiddenException;
import ee.bcskoolitus.infrastructure.exception.PrimaryKeyNotFoundException;
import ee.bcskoolitus.persistance.category.translation.CategoryTranslation;
import ee.bcskoolitus.persistance.category.translation.CategoryTranslationRepository;
import ee.bcskoolitus.persistance.course.Course;
import ee.bcskoolitus.persistance.course.CourseMapper;
import ee.bcskoolitus.persistance.course.CourseRepository;
import ee.bcskoolitus.persistance.course.lecturer.CourseLecturer;
import ee.bcskoolitus.persistance.course.lecturer.CourseLecturerMapper;
import ee.bcskoolitus.persistance.course.lecturer.CourseLecturerRepository;
import ee.bcskoolitus.persistance.fundingtype.translation.FundingTypeTranslationMapper;
import ee.bcskoolitus.persistance.fundingtype.translation.FundingTypeTranslationRepository;
import ee.bcskoolitus.persistance.room.Room;
import ee.bcskoolitus.persistance.training.Training;
import ee.bcskoolitus.persistance.training.translation.TrainingTranslation;
import ee.bcskoolitus.persistance.training.translation.TrainingTranslationRepository;
import ee.bcskoolitus.persistance.view.admincoursesummary.AdminCourseSummary;
import ee.bcskoolitus.persistance.view.admincoursesummary.AdminCourseSummaryMapper;
import ee.bcskoolitus.persistance.view.admincoursesummary.AdminCourseSummaryRepository;
import ee.bcskoolitus.persistance.view.admincoursesummary.AdminCourseSummarySpecifications;
import ee.bcskoolitus.persistance.view.coursesummary.CourseSummary;
import ee.bcskoolitus.persistance.view.publiccoursesummary.PublicCourseSummary;
import ee.bcskoolitus.persistance.view.publiccoursesummary.PublicCourseSummaryMapper;
import ee.bcskoolitus.persistance.view.publiccoursesummary.PublicCourseSummaryRepository;
import ee.bcskoolitus.persistance.view.publiccoursesummary.PublicCourseSummarySpecifications;
import ee.bcskoolitus.persistance.view.coursesummary.CourseSummaryMapper;
import ee.bcskoolitus.persistance.view.coursesummary.CourseSummaryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static ee.bcskoolitus.Error.COURSE_END_BEFORE_START;

@Service
@RequiredArgsConstructor
public class CourseService {

    // GET /api/admin-courses sortBy väärtus → AdminCourseSummary väli (staatus töövoo järjekorras U → O → F → X)
    private static final Map<String, String> ADMIN_COURSE_SORT_PROPERTIES = Map.of(
            "startDate", "startDate",
            "trainingTitle", "trainingTitle",
            "price", "price",
            "status", "statusOrder",
            "participantCount", "participantCount",
            "enquiryCount", "enquiryCount");

    private final CourseRepository courseRepository;
    private final CourseMapper courseMapper;
    private final CourseLecturerRepository courseLecturerRepository;
    private final CourseLecturerMapper courseLecturerMapper;
    private final CourseSummaryRepository courseSummaryRepository;
    private final CourseSummaryMapper courseSummaryMapper;
    private final AdminCourseSummaryRepository adminCourseSummaryRepository;
    private final AdminCourseSummaryMapper adminCourseSummaryMapper;
    private final PublicCourseSummaryRepository publicCourseSummaryRepository;
    private final PublicCourseSummaryMapper publicCourseSummaryMapper;
    private final TrainingTranslationRepository trainingTranslationRepository;
    private final CategoryTranslationRepository categoryTranslationRepository;
    private final FundingTypeTranslationRepository fundingTypeTranslationRepository;
    private final FundingTypeTranslationMapper fundingTypeTranslationMapper;
    private final LanguageService languageService;
    private final TrainingService trainingService;
    private final LecturerService lecturerService;
    private final RoomService roomService;
    private final UserService userService;

    // Koolituse kalender: kustutatud (D) välja jäetud; vaikimisi ainult tulevased (end_date >= täna)
    public List<CourseSummaryDto> findTrainingCourses(Integer trainingId, Boolean includePast) {
        trainingService.getValidActiveTrainingBy(trainingId);
        String deletedStatus = CourseStatus.DELETED.getCode();
        List<CourseSummary> courseSummaries = Boolean.TRUE.equals(includePast)
                ? courseSummaryRepository.findAllByTrainingIdAndStatusNotOrderByIsPastAscDaysFromTodayAscCourseIdAsc(trainingId, deletedStatus)
                : courseSummaryRepository.findAllByTrainingIdAndStatusNotAndIsPastFalseOrderByDaysFromTodayAscCourseIdAsc(trainingId, deletedStatus);
        return courseSummaryMapper.toCourseSummaryDtos(courseSummaries);
    }

    // Kõigi koolituste toimumiskorrad (admin): kustutatud toimumiskorrad ja kustutatud koolitused alati välja jäetud
    public AdminCourseSummaryDto findAdminCourses(AdminCourseFilterDto adminCourseFilterDto) {
        Pageable pageable = PageRequest.of(adminCourseFilterDto.getPage(), adminCourseFilterDto.getLimit(),
                createAdminCourseSort(adminCourseFilterDto.getSortBy(), adminCourseFilterDto.getSortDirection()));
        Specification<AdminCourseSummary> adminCourseSummarySpecification = createAdminCourseSummarySpecification(adminCourseFilterDto);
        Page<AdminCourseSummary> adminCourseSummaryPage = adminCourseSummaryRepository.findAll(adminCourseSummarySpecification, pageable);
        List<AdminCourseSummaryItemDto> adminCourseSummaryItemDtos = adminCourseSummaryMapper.toAdminCourseSummaryItemDtos(adminCourseSummaryPage.getContent());
        return new AdminCourseSummaryDto(adminCourseSummaryPage.getTotalPages(), adminCourseSummaryPage.getTotalElements(), adminCourseSummaryItemDtos);
    }

    // Puuduv või tundmatu sortBy → tulevased lähimast, siis möödunud hiliseimast; sortDirection "asc" → kasvav, muu → kahanev.
    // Lisaks alati courseId järgi, et võrdsete väärtuste järjekord oleks stabiilne.
    static Sort createAdminCourseSort(String sortBy, String sortDirection) {
        String sortProperty = sortBy == null ? null : ADMIN_COURSE_SORT_PROPERTIES.get(sortBy);
        if (sortProperty == null) {
            return Sort.by(Sort.Order.asc("isPast"), Sort.Order.asc("daysFromToday"), Sort.Order.asc("courseId"));
        }
        Sort.Direction direction = "asc".equalsIgnoreCase(sortDirection) ? Sort.Direction.ASC : Sort.Direction.DESC;
        return Sort.by(new Sort.Order(direction, sortProperty), Sort.Order.asc("courseId"));
    }

    private static Specification<AdminCourseSummary> createAdminCourseSummarySpecification(AdminCourseFilterDto adminCourseFilterDto) {
        return Specification.allOf(
                AdminCourseSummarySpecifications.hasContentLanguageCode(adminCourseFilterDto.getContentLang()),
                AdminCourseSummarySpecifications.hasActiveTraining(),
                AdminCourseSummarySpecifications.hasStatus(adminCourseFilterDto.getStatus()),
                AdminCourseSummarySpecifications.hasCategoryId(adminCourseFilterDto.getCategoryId()),
                AdminCourseSummarySpecifications.hasTrainingLanguageId(adminCourseFilterDto.getTrainingLanguageId()),
                AdminCourseSummarySpecifications.hasAttendance(adminCourseFilterDto.getAttendance()),
                AdminCourseSummarySpecifications.hasIsPromoted(adminCourseFilterDto.getIsPromoted()),
                AdminCourseSummarySpecifications.startsOnOrAfter(adminCourseFilterDto.getStartDateFrom()),
                AdminCourseSummarySpecifications.startsOnOrBefore(adminCourseFilterDto.getStartDateTo()),
                AdminCourseSummarySpecifications.isPastIncluded(adminCourseFilterDto.getIncludePast()),
                AdminCourseSummarySpecifications.trainingTitleContainsAllWords(adminCourseFilterDto.getSearchText()));
    }

    // Toimumiskorra ülevaade (admin): view rida + tunnid, koolitajad, ruum, veebilink ja märkmed course tabelist
    @Transactional(readOnly = true)
    public AdminCourseDto getAdminCourse(Integer courseId, String contentLang) {
        Course course = getValidActiveCourseBy(courseId);
        AdminCourseSummary adminCourseSummary = adminCourseSummaryRepository.findByCourseIdAndContentLanguageCode(courseId, contentLang)
                .filter(summary -> !TrainingStatus.DELETED.getCode().equals(summary.getTrainingStatus()))
                .orElseThrow(() -> new PrimaryKeyNotFoundException("courseId", courseId));
        AdminCourseDto adminCourseDto = adminCourseSummaryMapper.toAdminCourseDto(adminCourseSummary);
        adminCourseDto.setNumberOfAcademicHours(course.getNumberOfAcademicHours());
        adminCourseDto.setLecturerNames(getLecturerNamesOrNull(courseId));
        adminCourseDto.setRoomName(course.getRoom() == null ? null : course.getRoom().getName());
        adminCourseDto.setMeetingLink(course.getMeetingLink());
        adminCourseDto.setNotes(course.getNotes());
        return adminCourseDto;
    }

    // Koolitajad sort_order järjekorras komaga eraldatult; koolitajateta → null
    private String getLecturerNamesOrNull(Integer courseId) {
        List<CourseLecturer> courseLecturers = courseLecturerRepository.findCourseLecturersBy(courseId);
        if (courseLecturers.isEmpty()) {
            return null;
        }
        return courseLecturers.stream()
                .map(courseLecturer -> courseLecturer.getLecturer().getFullName())
                .collect(Collectors.joining(", "));
    }

    // Avalik kalender: publitseeritud koolituse avatud või täis tulevased toimumiskorrad, esile tõstetud eespool
    public CourseSummaryPageDto findPublicCourses(PublicCourseFilterDto publicCourseFilterDto) {
        Pageable pageable = PageRequest.of(publicCourseFilterDto.getPage(), publicCourseFilterDto.getLimit(), createPublicCourseSort());
        Specification<PublicCourseSummary> publicCourseSummarySpecification = createPublicCourseSummarySpecification(publicCourseFilterDto);
        Page<PublicCourseSummary> publicCourseSummaryPage = publicCourseSummaryRepository.findAll(publicCourseSummarySpecification, pageable);
        List<PublicCourseSummaryItemDto> publicCourseSummaryItemDtos = publicCourseSummaryMapper.toPublicCourseSummaryItemDtos(publicCourseSummaryPage.getContent());
        for (PublicCourseSummaryItemDto publicCourseSummaryItemDto : publicCourseSummaryItemDtos) {
            handleAddFundingTypes(publicCourseSummaryItemDto, publicCourseFilterDto.getContentLang());
        }
        return new CourseSummaryPageDto(publicCourseSummaryPage.getTotalPages(), publicCourseSummaryPage.getTotalElements(), publicCourseSummaryItemDtos);
    }

    // Avaleht: järgmised avatud toimumiskorrad (täis välja jäetud), järjestus nagu avalikus kalendris
    public List<PublicCourseSummaryItemDto> findNextCourses(NextCourseFilterDto nextCourseFilterDto) {
        Pageable pageable = PageRequest.of(0, nextCourseFilterDto.getLimit(), createPublicCourseSort());
        Specification<PublicCourseSummary> publicCourseSummarySpecification = Specification.allOf(
                PublicCourseSummarySpecifications.hasContentLanguageCode(nextCourseFilterDto.getContentLang()),
                PublicCourseSummarySpecifications.isFullIncluded(true));
        List<PublicCourseSummary> publicCourseSummaries = publicCourseSummaryRepository.findAll(publicCourseSummarySpecification, pageable).getContent();
        List<PublicCourseSummaryItemDto> publicCourseSummaryItemDtos = publicCourseSummaryMapper.toPublicCourseSummaryItemDtos(publicCourseSummaries);
        for (PublicCourseSummaryItemDto publicCourseSummaryItemDto : publicCourseSummaryItemDtos) {
            handleAddFundingTypes(publicCourseSummaryItemDto, nextCourseFilterDto.getContentLang());
        }
        return publicCourseSummaryItemDtos;
    }

    // Esile tõstetud eespool, edasi alguse järgi
    private static Sort createPublicCourseSort() {
        return Sort.by(Sort.Order.desc("isPromoted"), Sort.Order.asc("startDate"), Sort.Order.asc("courseId"));
    }

    private static Specification<PublicCourseSummary> createPublicCourseSummarySpecification(PublicCourseFilterDto publicCourseFilterDto) {
        return Specification.allOf(
                PublicCourseSummarySpecifications.hasContentLanguageCode(publicCourseFilterDto.getContentLang()),
                PublicCourseSummarySpecifications.hasCategoryId(publicCourseFilterDto.getCategoryId()),
                PublicCourseSummarySpecifications.hasTrainingLanguageId(publicCourseFilterDto.getTrainingLanguageId()),
                PublicCourseSummarySpecifications.hasFundingTypeId(publicCourseFilterDto.getFundingTypeId()),
                PublicCourseSummarySpecifications.hasAttendance(publicCourseFilterDto.getAttendance()),
                PublicCourseSummarySpecifications.isFullIncluded(publicCourseFilterDto.getHideFull()),
                PublicCourseSummarySpecifications.startsOnOrAfter(publicCourseFilterDto.getStartDateFrom()),
                PublicCourseSummarySpecifications.startsOnOrBefore(publicCourseFilterDto.getStartDateTo()),
                PublicCourseSummarySpecifications.containsAllWords(publicCourseFilterDto.getSearchText()));
    }

    private void handleAddFundingTypes(PublicCourseSummaryItemDto publicCourseSummaryItemDto, String contentLang) {
        publicCourseSummaryItemDto.setFundingTypes(fundingTypeTranslationMapper.toFundingTypeDtos(
                fundingTypeTranslationRepository.findTrainingFundingTypeTranslationsBy(publicCourseSummaryItemDto.getTrainingId(), contentLang)));
    }

    // Avalik toimumiskorra leht: ka möödunud avalik toimumiskord; tekstid contentLang keeles, puudumisel põhikeeles
    @Transactional(readOnly = true)
    public CoursePageDto getCoursePage(Integer courseId, String contentLang) {
        Course course = getValidPublicCourseBy(courseId);
        Training training = course.getTraining();
        TrainingTranslation trainingTranslation = getTrainingTranslationOrMainLanguageTranslation(training.getId(), contentLang, courseId);
        String displayedLanguageCode = trainingTranslation.getLanguage().getCode();
        CoursePageDto coursePageDto = courseMapper.toCoursePageDto(course);
        coursePageDto.setTrainingTranslationId(trainingTranslation.getId());
        coursePageDto.setIsMainLanguageFallback(!displayedLanguageCode.equals(contentLang));
        coursePageDto.setTitle(trainingTranslation.getTitle());
        coursePageDto.setShortDescription(trainingTranslation.getShortDescription());
        coursePageDto.setDescription(trainingTranslation.getDescription());
        coursePageDto.setCategoryName(getCategoryNameOrNull(training.getCategory().getId(), trainingTranslation.getLanguage().getId()));
        coursePageDto.setFundingTypes(fundingTypeTranslationMapper.toFundingTypeDtos(
                fundingTypeTranslationRepository.findTrainingFundingTypeTranslationsBy(training.getId(), displayedLanguageCode)));
        coursePageDto.setLecturers(lecturerService.findLecturerSummariesBy(
                courseLecturerRepository.findCourseLecturersBy(courseId).stream()
                        .map(CourseLecturer::getLecturer).toList(), contentLang));
        coursePageDto.setUpcomingCourses(publicCourseSummaryMapper.toUpcomingCourseDtos(
                publicCourseSummaryRepository.findAllByTrainingIdAndContentLanguageCodeOrderByStartDateAscCourseIdAsc(training.getId(), displayedLanguageCode)));
        return coursePageDto;
    }

    // Avalik = toimumiskord avatud või täis (O, F) ja koolitus publitseeritud (P); muu on nagu olematu → 404
    public Course getValidPublicCourseBy(Integer courseId) {
        Course course = getValidCourseBy(courseId);
        boolean isPublicStatus = CourseStatus.OPEN.getCode().equals(course.getStatus()) || CourseStatus.FULL.getCode().equals(course.getStatus());
        if (!isPublicStatus || !TrainingStatus.PUBLISHED.getCode().equals(course.getTraining().getStatus())) {
            throw new PrimaryKeyNotFoundException("courseId", courseId);
        }
        return course;
    }

    private TrainingTranslation getTrainingTranslationOrMainLanguageTranslation(Integer trainingId, String contentLang, Integer courseId) {
        return trainingTranslationRepository.findByTraining_IdAndLanguage_Code(trainingId, contentLang)
                .or(() -> trainingTranslationRepository.findByTraining_IdAndLanguage_Code(trainingId, languageService.getMainLanguage().getCode()))
                .orElseThrow(() -> new PrimaryKeyNotFoundException("courseId", courseId));
    }

    private String getCategoryNameOrNull(Integer categoryId, Integer languageId) {
        return categoryTranslationRepository.findByCategory_IdAndLanguage_Id(categoryId, languageId)
                .map(CategoryTranslation::getName)
                .orElse(null);
    }

    // Leiab ka kustutatud toimumiskorra — kasutab delete
    public Course getValidCourseBy(Integer courseId) {
        return courseRepository.findById(courseId)
                .orElseThrow(() -> new PrimaryKeyNotFoundException("courseId", courseId));
    }

    // Kustutatud toimumiskord (status D) on nagu olematu → 404
    public Course getValidActiveCourseBy(Integer courseId) {
        Course course = getValidCourseBy(courseId);
        if (CourseStatus.DELETED.getCode().equals(course.getStatus())) {
            throw new PrimaryKeyNotFoundException("courseId", courseId);
        }
        return course;
    }

    @Transactional(readOnly = true)
    public CourseDto getCourse(Integer courseId) {
        Course course = getValidActiveCourseBy(courseId);
        CourseDto courseDto = courseMapper.toCourseDto(course);
        courseDto.setLecturers(courseLecturerMapper.toLecturerDtos(courseLecturerRepository.findCourseLecturersBy(courseId)));
        return courseDto;
    }

    // Loob toimumiskorra ja koolitajate seosed ühes transaktsioonis (ka mustandis koolitusele)
    @Transactional
    public void addCourse(Integer trainingId, CourseCreateRequestDto courseCreateRequestDto) {
        validateEndDateNotBeforeStartDate(courseCreateRequestDto.getStartDate(), courseCreateRequestDto.getEndDate());
        Course course = courseMapper.toCourse(courseCreateRequestDto);
        course.setTraining(trainingService.getValidActiveTrainingBy(trainingId));
        course.setCreatedBy(userService.getValidUserBy(courseCreateRequestDto.getUserId()));
        course.setRoom(getActiveRoomOrNull(courseCreateRequestDto.getRoomId()));
        course.setNotes(blankToNull(courseCreateRequestDto.getNotes()));
        course.setMeetingLink(blankToNull(courseCreateRequestDto.getMeetingLink()));
        courseRepository.save(course);
        addCourseLecturers(course, courseCreateRequestDto.getLecturerIds(), List.of());
    }

    // Muudab kõik väljad (ka staatuse) ja kirjutab koolitajad üle ühes transaktsioonis
    @Transactional
    public void updateCourse(Integer courseId, CourseUpdateRequestDto courseUpdateRequestDto) {
        Course course = getValidActiveCourseBy(courseId);
        validateEndDateNotBeforeStartDate(courseUpdateRequestDto.getStartDate(), courseUpdateRequestDto.getEndDate());
        Room room = getAssignableRoomOrNull(courseUpdateRequestDto.getRoomId(), course.getRoom());
        courseMapper.updateCourse(courseUpdateRequestDto, course);
        course.setRoom(room);
        course.setNotes(blankToNull(courseUpdateRequestDto.getNotes()));
        course.setMeetingLink(blankToNull(courseUpdateRequestDto.getMeetingLink()));
        courseRepository.save(course);
        replaceCourseLecturers(course, courseUpdateRequestDto.getLecturerIds());
    }

    // Soft delete: status = D; osalejad ja koolitajad jäävad alles. Korduv kustutamine ei muuda midagi.
    @Transactional
    public void deleteCourse(Integer courseId) {
        Course course = getValidCourseBy(courseId);
        if (CourseStatus.DELETED.getCode().equals(course.getStatus())) {
            return;
        }
        course.setStatus(CourseStatus.DELETED.getCode());
        courseRepository.save(course);
    }

    private static void validateEndDateNotBeforeStartDate(LocalDate startDate, LocalDate endDate) {
        if (endDate.isBefore(startDate)) {
            throw new ForbiddenException(COURSE_END_BEFORE_START.getMessage(), COURSE_END_BEFORE_START.name());
        }
    }

    private Room getActiveRoomOrNull(Integer roomId) {
        return roomId == null ? null : roomService.getValidActiveRoomBy(roomId);
    }

    // Praegune ruum võib olla ka kustutatud — nii saab toimumiskorra muid välju edasi salvestada
    private Room getAssignableRoomOrNull(Integer roomId, Room linkedRoom) {
        if (roomId == null) {
            return null;
        }
        return roomService.getValidAssignableRoomBy(roomId, linkedRoom == null ? null : linkedRoom.getId());
    }

    private static String blankToNull(String value) {
        return value == null || value.isBlank() ? null : value;
    }

    // Praegused seosed loetakse enne kustutamist — nende koolitajad võivad jääda ka kustutatuna
    private void replaceCourseLecturers(Course course, List<Integer> lecturerIds) {
        List<Integer> linkedLecturerIds = courseLecturerRepository.findCourseLecturersBy(course.getId()).stream()
                .map(courseLecturer -> courseLecturer.getLecturer().getId())
                .toList();
        courseLecturerRepository.deleteCourseLecturersBy(course.getId());
        addCourseLecturers(course, lecturerIds, linkedLecturerIds);
    }

    // Järjekord listis = sort_order (1 = esimene)
    private void addCourseLecturers(Course course, List<Integer> lecturerIds, List<Integer> linkedLecturerIds) {
        int sortOrder = 1;
        for (Integer lecturerId : lecturerIds) {
            CourseLecturer courseLecturer = new CourseLecturer();
            courseLecturer.setCourse(course);
            courseLecturer.setLecturer(lecturerService.getValidAssignableLecturerBy(lecturerId, linkedLecturerIds));
            courseLecturer.setSortOrder(sortOrder++);
            courseLecturerRepository.save(courseLecturer);
        }
    }
}
