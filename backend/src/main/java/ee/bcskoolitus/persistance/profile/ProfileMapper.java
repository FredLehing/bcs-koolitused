package ee.bcskoolitus.persistance.profile;

import ee.bcskoolitus.controller.courseparticipant.dto.CourseRegistrationRequestDto;
import ee.bcskoolitus.controller.enquiry.dto.EnquiryCreateRequestDto;
import ee.bcskoolitus.controller.user.dto.MyParticipantDto;
import ee.bcskoolitus.controller.user.dto.ProfileUpdateRequestDto;
import ee.bcskoolitus.controller.user.dto.SignupRequestDto;
import org.mapstruct.*;

@Mapper(unmappedTargetPolicy = ReportingPolicy.IGNORE, componentModel = MappingConstants.ComponentModel.SPRING)
public interface ProfileMapper {

    @Mapping(source = "firstName", target = "firstName")
    @Mapping(source = "lastName", target = "lastName")
    @Mapping(source = "email", target = "email")
    @Mapping(source = "phone", target = "phone")
    Profile toProfile(EnquiryCreateRequestDto enquiryCreateRequestDto);

    @Mapping(source = "firstName", target = "firstName")
    @Mapping(source = "lastName", target = "lastName")
    @Mapping(source = "email", target = "email")
    @Mapping(source = "phone", target = "phone")
    Profile toProfile(SignupRequestDto signupRequestDto);

    @Mapping(source = "firstName", target = "firstName")
    @Mapping(source = "lastName", target = "lastName")
    @Mapping(source = "email", target = "email")
    @Mapping(source = "phone", target = "phone")
    Profile toProfile(CourseRegistrationRequestDto courseRegistrationRequestDto);

    // Olemasoleva osaleja profiil uueneb registreerumise vormi andmetega
    @Mapping(source = "firstName", target = "firstName")
    @Mapping(source = "lastName", target = "lastName")
    @Mapping(source = "email", target = "email")
    @Mapping(source = "phone", target = "phone")
    @Mapping(target = "id", ignore = true)
    void updateProfile(CourseRegistrationRequestDto courseRegistrationRequestDto, @MappingTarget Profile profile);

    @Mapping(source = "firstName", target = "firstName")
    @Mapping(source = "lastName", target = "lastName")
    @Mapping(source = "email", target = "email")
    @Mapping(source = "phone", target = "phone")
    MyParticipantDto toMyParticipantDto(Profile profile);

    // "Minu andmed": osalejata kasutajale luuakse profiil; e-post (trimmitud) määrab UserService
    @Mapping(source = "firstName", target = "firstName")
    @Mapping(source = "lastName", target = "lastName")
    @Mapping(source = "phone", target = "phone")
    @Mapping(target = "email", ignore = true)
    Profile toProfile(ProfileUpdateRequestDto profileUpdateRequestDto);

    // "Minu andmed": olemasoleva osaleja profiil; e-post (trimmitud) määrab UserService
    @Mapping(source = "firstName", target = "firstName")
    @Mapping(source = "lastName", target = "lastName")
    @Mapping(source = "phone", target = "phone")
    @Mapping(target = "email", ignore = true)
    @Mapping(target = "id", ignore = true)
    void updateProfile(ProfileUpdateRequestDto profileUpdateRequestDto, @MappingTarget Profile profile);
}
