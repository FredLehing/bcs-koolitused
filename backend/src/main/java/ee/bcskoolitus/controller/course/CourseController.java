package ee.bcskoolitus.controller.course;

import ee.bcskoolitus.controller.course.dto.AdminCourseDto;
import ee.bcskoolitus.controller.course.dto.AdminCourseFilterDto;
import ee.bcskoolitus.controller.course.dto.AdminCourseSummaryDto;
import ee.bcskoolitus.controller.course.dto.CourseCreateRequestDto;
import ee.bcskoolitus.controller.course.dto.CoursePageDto;
import ee.bcskoolitus.controller.course.dto.CourseSummaryPageDto;
import ee.bcskoolitus.controller.course.dto.NextCourseFilterDto;
import ee.bcskoolitus.controller.course.dto.PublicCourseSummaryItemDto;
import ee.bcskoolitus.controller.course.dto.PublicCourseFilterDto;
import ee.bcskoolitus.controller.course.dto.CourseDto;
import ee.bcskoolitus.controller.course.dto.CourseSummaryDto;
import ee.bcskoolitus.controller.course.dto.CourseUpdateRequestDto;
import ee.bcskoolitus.infrastructure.error.ApiError;
import ee.bcskoolitus.service.CourseService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api")
public class CourseController {
    private final CourseService courseService;

    @GetMapping("/training/{trainingId}/courses")
    @Operation(summary = "Koolituse toimumiskorrad (kalender)",
            description = "View course_summary. Kustutatud (D) toimumiskordi ei tagastata. includePast=false (vaikimisi) → ainult end_date >= täna. Järjestus: tulevased lähimast, siis möödunud hiliseimast. notes / meetingLink sisu ei tagastata.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "OK"),
            @ApiResponse(
                    responseCode = "404",
                    description = "Olematu või kustutatud trainingId -> 'errorCode:' PRIMARY_KEY_NOT_FOUND",
                    content = @Content(schema = @Schema(implementation = ApiError.class))
            )
    })
    public List<CourseSummaryDto> findTrainingCourses(@PathVariable Integer trainingId,
                                                      @RequestParam(required = false, defaultValue = "false") Boolean includePast) {
        return courseService.findTrainingCourses(trainingId, includePast);
    }

    @GetMapping("/admin-courses")
    @Operation(summary = "Kõigi koolituste toimumiskorrad (admin): filtrid, sorteerimine, leheküljestus",
            description = "View admin_course_summary. Kustutatud toimumiskordi ja kustutatud koolituste toimumiskordi ei tagastata. "
                    + "status puudub = kõik peale D. includePast=false (vaikimisi) → ainult end_date >= täna. attendance: ONSITE / ONLINE. "
                    + "searchText otsib koolituse nimest (iga sõna peab esinema). sortBy: startDate / trainingTitle / price / status / participantCount / enquiryCount, "
                    + "puudub → tulevased lähimast, siis möödunud hiliseimast; sortDirection: ASC / DESC. "
                    + "participantCount ja paidCount loevad ainult registreerunud (R) osalejaid.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "OK"),
            @ApiResponse(
                    responseCode = "400",
                    description = "Kohustuslik parameeter puudub või on vigane -> 'errorCode:' INCORRECT_INPUT",
                    content = @Content(schema = @Schema(implementation = ApiError.class))
            )
    })
    public AdminCourseSummaryDto findAdminCourses(@Valid @ParameterObject AdminCourseFilterDto adminCourseFilterDto) {
        return courseService.findAdminCourses(adminCourseFilterDto);
    }

    @GetMapping("/admin-course/{courseId}")
    @Operation(summary = "Toimumiskorra ülevaade (admin, ainult lugemiseks)",
            description = "Koolituse nimi contentLang keeles, puudumisel põhikeeles. lecturerNames (sort_order järjekorras), roomName, meetingLink ja notes võivad olla null.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "OK"),
            @ApiResponse(
                    responseCode = "404",
                    description = "Olematu või kustutatud courseId või kustutatud koolitus -> 'errorCode:' PRIMARY_KEY_NOT_FOUND",
                    content = @Content(schema = @Schema(implementation = ApiError.class))
            )
    })
    public AdminCourseDto getAdminCourse(@PathVariable Integer courseId, @RequestParam String contentLang) {
        return courseService.getAdminCourse(courseId, contentLang);
    }

    @GetMapping("/courses")
    @Operation(summary = "Avalik koolituste kalender: filtrid ja leheküljestus",
            description = "View public_course_summary: publitseeritud koolituse avatud või täis (O, F) toimumiskorrad alates tänasest, ainult olemasoleva contentLang tõlkega. "
                    + "Järjestus: esile tõstetud eespool, siis alguse järgi. searchText otsib pealkirjast ja lühikirjeldusest (iga sõna peab esinema). "
                    + "categoryId / trainingLanguageId / fundingTypeId 0 = kõik. attendance: ONSITE / ONLINE. hideFull=true → ilma täis toimumiskordadeta. lecturers sisaldab aktiivsete koolitajate kaardiandmeid (fullName, title, shortDescription, photoVersion), kasutajaliidese keeles ja seoste järjekorras. Kaardid ei vaja eraldi JSON-päringuid. Veebilinki ei tagastata.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "OK"),
            @ApiResponse(
                    responseCode = "400",
                    description = "Kohustuslik parameeter puudub või on vigane -> 'errorCode:' INCORRECT_INPUT",
                    content = @Content(schema = @Schema(implementation = ApiError.class))
            )
    })
    public CourseSummaryPageDto findPublicCourses(@Valid @ParameterObject PublicCourseFilterDto publicCourseFilterDto) {
        return courseService.findPublicCourses(publicCourseFilterDto);
    }

    @GetMapping("/next-courses")
    @Operation(summary = "Avalehe järgmised toimumiskorrad",
            description = "View public_course_summary: publitseeritud koolituse avatud (O) toimumiskorrad alates tänasest, ainult olemasoleva contentLang tõlkega; täis (F) jäetakse välja. "
                    + "Järjestus: esile tõstetud eespool, siis alguse järgi. limit 1–20 (vaikimisi 5). Tühi list, kui toimumiskordi pole.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "OK"),
            @ApiResponse(
                    responseCode = "400",
                    description = "contentLang puudub või limit väljaspool 1–20 -> 'errorCode:' INCORRECT_INPUT",
                    content = @Content(schema = @Schema(implementation = ApiError.class))
            )
    })
    public List<PublicCourseSummaryItemDto> findNextCourses(@Valid @ParameterObject NextCourseFilterDto nextCourseFilterDto) {
        return courseService.findNextCourses(nextCourseFilterDto);
    }

    @GetMapping("/course-summary/{courseId}")
    @Operation(summary = "Avalik toimumiskorra leht",
            description = "Toimumiskord peab olema avatud või täis (O, F) ja koolitus publitseeritud; möödunud avalik toimumiskord leitakse (isPast = true). "
                    + "Tekstid contentLang keeles, puudumisel põhikeeles (isMainLanguageFallback = true). upcomingCourses = sama koolituse avalikud tulevased toimumiskorrad. lecturers sisaldab aktiivsete koolitajate kaardiandmeid (fullName, title, shortDescription, photoVersion), kasutajaliidese keeles ja seoste järjekorras. Kaardid ei vaja eraldi JSON-päringuid. Veebilinki ei tagastata.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "OK"),
            @ApiResponse(
                    responseCode = "404",
                    description = "Olematu või mitteavalik courseId -> 'errorCode:' PRIMARY_KEY_NOT_FOUND",
                    content = @Content(schema = @Schema(implementation = ApiError.class))
            )
    })
    public CoursePageDto getCoursePage(@PathVariable Integer courseId, @RequestParam String contentLang) {
        return courseService.getCoursePage(courseId, contentLang);
    }

    @GetMapping("/course/{courseId}")
    @Operation(summary = "Toimumiskorra andmed muutmise vormi jaoks",
            description = "lecturers = course_lecturer sort_order järjekorras (võib olla tühi); roomId, notes ja meetingLink võivad olla null.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "OK"),
            @ApiResponse(
                    responseCode = "404",
                    description = "Olematu või kustutatud courseId -> 'errorCode:' PRIMARY_KEY_NOT_FOUND",
                    content = @Content(schema = @Schema(implementation = ApiError.class))
            )
    })
    public CourseDto getCourse(@PathVariable Integer courseId) {
        return courseService.getCourse(courseId);
    }

    @PostMapping("/training/{trainingId}/course")
    @Operation(summary = "Lisab koolitusele toimumiskorra",
            description = "Loob course ja course_lecturer read (lecturerIds järjekorras) ühes transaktsioonis. Uus koolitaja peab olema aktiivne. Tühi notes / meetingLink → null.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "OK"),
            @ApiResponse(
                    responseCode = "404",
                    description = "Olematu või kustutatud trainingId / olematu roomId / userId / olematu või kustutatud lecturerId -> 'errorCode:' PRIMARY_KEY_NOT_FOUND",
                    content = @Content(schema = @Schema(implementation = ApiError.class))
            ),
            @ApiResponse(
                    responseCode = "403",
                    description = "endDate on varasem kui startDate -> 'errorCode:' COURSE_END_BEFORE_START",
                    content = @Content(schema = @Schema(implementation = ApiError.class))
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Kohustuslik väli puudub, numberOfDays / numberOfAcademicHours < 1, price < 0, status pole U/O/F/X, meetingLink liiga pikk või lecturerIds sisaldab korduvat ID-d -> 'errorCode:' INCORRECT_INPUT",
                    content = @Content(schema = @Schema(implementation = ApiError.class))
            )
    })
    public void addCourse(@PathVariable Integer trainingId, @Valid @RequestBody CourseCreateRequestDto courseCreateRequestDto) {
        courseService.addCourse(trainingId, courseCreateRequestDto);
    }

    @PutMapping("/course/{courseId}")
    @Operation(summary = "Muudab toimumiskorda",
            description = "Muudab kõik väljad (ka staatuse). course_lecturer read kirjutatakse lecturerIds järgi üle; juba seotud kustutatud koolitaja võib jääda.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "OK"),
            @ApiResponse(
                    responseCode = "404",
                    description = "Olematu või kustutatud courseId / olematu roomId / olematu lecturerId või uus kustutatud lecturerId -> 'errorCode:' PRIMARY_KEY_NOT_FOUND",
                    content = @Content(schema = @Schema(implementation = ApiError.class))
            ),
            @ApiResponse(
                    responseCode = "403",
                    description = "endDate on varasem kui startDate -> 'errorCode:' COURSE_END_BEFORE_START",
                    content = @Content(schema = @Schema(implementation = ApiError.class))
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Kohustuslik väli puudub, numberOfDays / numberOfAcademicHours < 1, price < 0, status pole U/O/F/X, meetingLink liiga pikk või lecturerIds sisaldab korduvat ID-d -> 'errorCode:' INCORRECT_INPUT",
                    content = @Content(schema = @Schema(implementation = ApiError.class))
            )
    })
    public void updateCourse(@PathVariable Integer courseId, @Valid @RequestBody CourseUpdateRequestDto courseUpdateRequestDto) {
        courseService.updateCourse(courseId, courseUpdateRequestDto);
    }

    @DeleteMapping("/course/{courseId}")
    @Operation(summary = "Kustutab toimumiskorra (soft delete)",
            description = "Määrab course.status = D. Osalejaid ja koolitajaid ei kustutata. Juba kustutatud toimumiskorra korral midagi ei muutu.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "OK"),
            @ApiResponse(
                    responseCode = "404",
                    description = "Olematu courseId -> 'errorCode:' PRIMARY_KEY_NOT_FOUND",
                    content = @Content(schema = @Schema(implementation = ApiError.class))
            )
    })
    public void deleteCourse(@PathVariable Integer courseId) {
        courseService.deleteCourse(courseId);
    }
}
