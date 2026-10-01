package ee.bcskoolitus.persistance.view.adminregistrationsummary;

import ee.bcskoolitus.controller.courseparticipant.dto.AdminRegistrationDto;
import ee.bcskoolitus.controller.courseparticipant.dto.AdminRegistrationSummaryDto;
import org.mapstruct.*;

import java.util.List;

@Mapper(unmappedTargetPolicy = ReportingPolicy.IGNORE, componentModel = MappingConstants.ComponentModel.SPRING)
public interface AdminRegistrationSummaryMapper {

    @Mapping(source = "courseParticipantId", target = "courseParticipantId")
    @Mapping(source = "createdAt", target = "registeredAt")
    @Mapping(source = "participantName", target = "participantName")
    @Mapping(source = "email", target = "email")
    @Mapping(source = "courseId", target = "courseId")
    @Mapping(source = "trainingTitle", target = "trainingTitle")
    @Mapping(source = "courseStartDate", target = "courseStartDate")
    @Mapping(source = "courseEndDate", target = "courseEndDate")
    @Mapping(source = "isPast", target = "isPast")
    @Mapping(source = "hasPaid", target = "hasPaid")
    @Mapping(source = "requiresLaptop", target = "requiresLaptop")
    @Mapping(source = "status", target = "status")
    AdminRegistrationSummaryDto toAdminRegistrationSummaryDto(AdminRegistrationSummary adminRegistrationSummary);

    List<AdminRegistrationSummaryDto> toAdminRegistrationSummaryDtos(List<AdminRegistrationSummary> adminRegistrationSummaries);

    @Mapping(source = "courseParticipantId", target = "courseParticipantId")
    @Mapping(source = "status", target = "status")
    @Mapping(source = "hasPaid", target = "hasPaid")
    @Mapping(source = "requiresLaptop", target = "requiresLaptop")
    @Mapping(source = "notes", target = "notes")
    @Mapping(source = "adminNotes", target = "adminNotes")
    @Mapping(source = "createdAt", target = "createdAt")
    @Mapping(source = "updatedAt", target = "updatedAt")
    @Mapping(source = "participantName", target = "participantName")
    @Mapping(source = "email", target = "email")
    @Mapping(source = "phone", target = "phone")
    @Mapping(source = "accountEmail", target = "accountEmail")
    @Mapping(source = "courseId", target = "courseId")
    @Mapping(source = "trainingTitle", target = "trainingTitle")
    @Mapping(source = "courseStartDate", target = "courseStartDate")
    @Mapping(source = "courseEndDate", target = "courseEndDate")
    @Mapping(source = "courseStatus", target = "courseStatus")
    @Mapping(source = "isPast", target = "isPast")
    AdminRegistrationDto toAdminRegistrationDto(AdminRegistrationSummary adminRegistrationSummary);
}
