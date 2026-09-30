package ee.bcskoolitus.persistance.view.adminlecturersummary;

import ee.bcskoolitus.controller.lecturer.dto.AdminLecturerSummaryDto;
import org.mapstruct.*;

import java.util.List;

@Mapper(unmappedTargetPolicy = ReportingPolicy.IGNORE, componentModel = MappingConstants.ComponentModel.SPRING)
public interface AdminLecturerSummaryMapper {

    @Mapping(source = "lecturerId", target = "lecturerId")
    @Mapping(source = "lecturerTranslationId", target = "lecturerTranslationId")
    @Mapping(source = "fullName", target = "fullName")
    @Mapping(source = "title", target = "title")
    @Mapping(source = "status", target = "status")
    @Mapping(source = "hasAllTranslations", target = "hasAllTranslations")
    @Mapping(source = "missingTranslationLanguageCodes", target = "missingTranslationLanguageCodes")
    @Mapping(source = "trainingCount", target = "trainingCount")
    @Mapping(source = "upcomingCourseCount", target = "upcomingCourseCount")
    @Mapping(source = "updatedAt", target = "updatedAt")
    AdminLecturerSummaryDto toAdminLecturerSummaryDto(AdminLecturerSummary adminLecturerSummary);

    List<AdminLecturerSummaryDto> toAdminLecturerSummaryDtos(List<AdminLecturerSummary> adminLecturerSummaries);

    // View veerg on komaga eraldatud keelekoodid (nt "en"); null = kõik tõlked olemas → tühi list
    default List<String> toLanguageCodes(String languageCodes) {
        return languageCodes == null ? List.of() : List.of(languageCodes.split(","));
    }
}
