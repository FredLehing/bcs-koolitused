package ee.bcskoolitus.persistance.course.participant;

import ee.bcskoolitus.controller.courseparticipant.dto.AdminRegistrationUpdateRequestDto;
import ee.bcskoolitus.controller.courseparticipant.dto.CourseParticipantDto;
import ee.bcskoolitus.controller.courseparticipant.dto.MyRegistrationDto;
import ee.bcskoolitus.controller.user.dto.AdminUserRegistrationDto;
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

    // trainingTitle (tõlge) ja canCancel määrab CourseParticipantService
    @Mapping(source = "id", target = "courseParticipantId")
    @Mapping(source = "course.id", target = "courseId")
    @Mapping(source = "course.startDate", target = "startDate")
    @Mapping(source = "course.endDate", target = "endDate")
    @Mapping(expression = "java(courseParticipant.getCourse().getRoom() != null)", target = "isOnSite")
    @Mapping(expression = "java(courseParticipant.getCourse().getMeetingLink() != null && !courseParticipant.getCourse().getMeetingLink().isBlank())", target = "isOnline")
    @Mapping(source = "course.status", target = "courseStatus")
    @Mapping(source = "status", target = "status")
    @Mapping(source = "hasPaid", target = "hasPaid")
    @Mapping(expression = "java(courseParticipant.getCourse().getEndDate().isBefore(java.time.LocalDate.now()))", target = "isPast")
    @Mapping(target = "trainingTitle", ignore = true)
    @Mapping(target = "canCancel", ignore = true)
    MyRegistrationDto toMyRegistrationDto(CourseParticipant courseParticipant);

    // trainingTitle (tõlge) määrab AdminUserService
    @Mapping(source = "id", target = "courseParticipantId")
    @Mapping(source = "course.id", target = "courseId")
    @Mapping(source = "course.startDate", target = "startDate")
    @Mapping(source = "course.endDate", target = "endDate")
    @Mapping(expression = "java(courseParticipant.getCourse().getEndDate().isBefore(java.time.LocalDate.now()))", target = "isPast")
    @Mapping(source = "status", target = "status")
    @Mapping(source = "hasPaid", target = "hasPaid")
    @Mapping(target = "trainingTitle", ignore = true)
    AdminUserRegistrationDto toAdminUserRegistrationDto(CourseParticipant courseParticipant);

    // Admin muudab ainult registreerumise enda välju; adminNotes (tühi → null) määrab CourseParticipantService
    @Mapping(source = "status", target = "status")
    @Mapping(source = "hasPaid", target = "hasPaid")
    @Mapping(source = "requiresLaptop", target = "requiresLaptop")
    @Mapping(target = "adminNotes", ignore = true)
    void updateCourseParticipant(AdminRegistrationUpdateRequestDto adminRegistrationUpdateRequestDto, @MappingTarget CourseParticipant courseParticipant);
}
