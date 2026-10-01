package ee.bcskoolitus.controller.enquiry;

import ee.bcskoolitus.controller.enquiry.dto.AdminEnquiryDto;
import ee.bcskoolitus.controller.enquiry.dto.CourseEnquiryDto;
import ee.bcskoolitus.controller.enquiry.dto.EnquiryCreateRequestDto;
import ee.bcskoolitus.controller.enquiry.dto.AdminEnquirySummaryDto;
import ee.bcskoolitus.infrastructure.error.ApiError;
import ee.bcskoolitus.service.EnquiryService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
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
public class EnquiryController {
    private final EnquiryService enquiryService;

    @GetMapping("/admin-enquiries")
    @Operation(summary = "Admini huviliste päringute nimekiri",
            description = "Uusimad eespool (created_at kahanevalt). Vaikimisi ainult uued (status U); includeHandled=true → ka käsitletud (H). trainingTitle contentLang keeles, puudumisel põhikeeles. Sõnumit ja telefoni ei tagastata. Tundmatu contentLang → tühi list.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "OK")
    })
    public List<AdminEnquirySummaryDto> findAdminEnquiries(@RequestParam String contentLang,
                                                           @RequestParam(required = false, defaultValue = "false") Boolean includeHandled) {
        return enquiryService.findAdminEnquiries(contentLang, includeHandled);
    }

    @PostMapping("/enquiry")
    @Operation(summary = "Külastaja päring \"Küsi lisainfot\"",
            description = "Sisselogimiseta. Loob uue profile rea (e-posti järgi ei otsita) ja enquiry rea (status U). Koolitus peab olema publitseeritud; "
                    + "courseId korral selle koolituse avatud või täis toimumiskord. Tühi companyName → null.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "OK"),
            @ApiResponse(
                    responseCode = "404",
                    description = "Olematu või mitteavalik trainingId / courseId -> 'errorCode:' PRIMARY_KEY_NOT_FOUND",
                    content = @Content(schema = @Schema(implementation = ApiError.class))
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Kohustuslik väli puudub, vigane e-post või liiga pikk väli -> 'errorCode:' INCORRECT_INPUT",
                    content = @Content(schema = @Schema(implementation = ApiError.class))
            )
    })
    public void addEnquiry(@Valid @RequestBody EnquiryCreateRequestDto enquiryCreateRequestDto) {
        enquiryService.addEnquiry(enquiryCreateRequestDto);
    }

    @GetMapping("/course/{courseId}/enquiries")
    @Operation(summary = "Toimumiskorraga seotud päringud (admin)",
            description = "Kõik staatused (U, H), uusimad eespool. fullName ja email päringu profiilist.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "OK"),
            @ApiResponse(
                    responseCode = "404",
                    description = "Olematu courseId -> 'errorCode:' PRIMARY_KEY_NOT_FOUND",
                    content = @Content(schema = @Schema(implementation = ApiError.class))
            )
    })
    public List<CourseEnquiryDto> findCourseEnquiries(@PathVariable Integer courseId) {
        return enquiryService.findCourseEnquiries(courseId);
    }

    @GetMapping("/admin-enquiry/{enquiryId}")
    @Operation(summary = "Ühe päringu admini vaade",
            description = "Päringu andmed, sõnum ja kontakt. trainingTitle contentLang keeles, puudumisel põhikeeles; trainingTranslationId = kuvatud tõlke ID.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "OK"),
            @ApiResponse(
                    responseCode = "404",
                    description = "Olematu enquiryId -> 'errorCode:' PRIMARY_KEY_NOT_FOUND",
                    content = @Content(schema = @Schema(implementation = ApiError.class))
            )
    })
    public AdminEnquiryDto getAdminEnquiry(@PathVariable Integer enquiryId, @RequestParam String contentLang) {
        return enquiryService.getAdminEnquiry(enquiryId, contentLang);
    }

    @PutMapping("/enquiry/{enquiryId}/handle")
    @Operation(summary = "Märgib päringu käsitletuks",
            description = "Tegevusteenus: status = H. Juba käsitletud päringu korral midagi ei muutu.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "OK"),
            @ApiResponse(
                    responseCode = "404",
                    description = "Olematu enquiryId -> 'errorCode:' PRIMARY_KEY_NOT_FOUND",
                    content = @Content(schema = @Schema(implementation = ApiError.class))
            )
    })
    public void handleEnquiry(@PathVariable Integer enquiryId) {
        enquiryService.handleEnquiry(enquiryId);
    }

    @PutMapping("/enquiry/{enquiryId}/reopen")
    @Operation(summary = "Märgib päringu uueks",
            description = "Tegevusteenus: status = U. Uue päringu korral midagi ei muutu.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "OK"),
            @ApiResponse(
                    responseCode = "404",
                    description = "Olematu enquiryId -> 'errorCode:' PRIMARY_KEY_NOT_FOUND",
                    content = @Content(schema = @Schema(implementation = ApiError.class))
            )
    })
    public void reopenEnquiry(@PathVariable Integer enquiryId) {
        enquiryService.reopenEnquiry(enquiryId);
    }
}
