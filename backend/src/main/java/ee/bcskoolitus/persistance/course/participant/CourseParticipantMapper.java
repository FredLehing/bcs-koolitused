package ee.bcskoolitus.persistance.course.participant;

import ee.bcskoolitus.controller.courseparticipant.dto.AdminRegistrationUpdateRequestDto;
import ee.bcskoolitus.controller.courseparticipant.dto.CourseParticipantDto;
import org.mapstruct.*;

import java.util.List;

@Mapper(unmappedTargetPolicy = ReportingPolicy.IGNORE, componentModel = MappingConstants.ComponentModel.SPRING)
public interface CourseParticipantMapper {

    @Mapping(source = "id", target = "courseParticipantId")
    @Mapping(source = "participant.name", target = "participantName")
    @Mapping(source = "participant.profile.email", target = "email")
    @Mapping(source = "participant.profile.phone", target = "phone")
    @Mapping(source = "createdAt", target = "registeredAt")
    @Mapping(source = "hasPaid", target = "hasPaid")
    @Mapping(source = "requiresLaptop", target = "requiresLaptop")
    @Mapping(source = "status", target = "status")
    @Mapping(source = "notes", target = "notes")
    CourseParticipantDto toCourseParticipantDto(CourseParticipant courseParticipant);

    List<CourseParticipantDto> toCourseParticipantDtos(List<CourseParticipant> courseParticipants);

    // Admin muudab ainult registreerumise enda välju; adminNotes (tühi → null) määrab CourseParticipantService
    @Mapping(source = "status", target = "status")
    @Mapping(source = "hasPaid", target = "hasPaid")
    @Mapping(source = "requiresLaptop", target = "requiresLaptop")
    @Mapping(target = "adminNotes", ignore = true)
    void updateCourseParticipant(AdminRegistrationUpdateRequestDto adminRegistrationUpdateRequestDto, @MappingTarget CourseParticipant courseParticipant);
}
