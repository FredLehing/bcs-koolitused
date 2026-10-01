package ee.bcskoolitus.persistance.feedback;

import ee.bcskoolitus.controller.adminfeedback.dto.AdminFeedbackDto;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper
public interface AdminFeedbackMapper {
    @Mapping(source = "id", target = "feedbackId")
    @Mapping(source = "courseParticipant.id", target = "courseParticipantId")
    @Mapping(source = "courseParticipant.course.id", target = "courseId")
    @Mapping(source = "courseParticipant.course.training.id", target = "trainingId")
    @Mapping(source = "courseParticipant.participant.name", target = "participantName")
    @Mapping(source = "courseParticipant.course.startDate", target = "startDate")
    @Mapping(source = "courseParticipant.course.endDate", target = "endDate")
    AdminFeedbackDto toAdminFeedbackDto(Feedback feedback);
}
