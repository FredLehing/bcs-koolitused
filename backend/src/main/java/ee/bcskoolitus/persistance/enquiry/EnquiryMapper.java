package ee.bcskoolitus.persistance.enquiry;

import ee.bcskoolitus.controller.enquiry.dto.CourseEnquiryDto;
import org.mapstruct.*;

import java.util.List;

@Mapper(unmappedTargetPolicy = ReportingPolicy.IGNORE, componentModel = MappingConstants.ComponentModel.SPRING)
public interface EnquiryMapper {

    @Mapping(source = "id", target = "enquiryId")
    @Mapping(source = "createdAt", target = "createdAt")
    @Mapping(expression = "java(enquiry.getProfile().getFirstName() + \" \" + enquiry.getProfile().getLastName())", target = "fullName")
    @Mapping(source = "profile.email", target = "email")
    @Mapping(source = "companyName", target = "companyName")
    @Mapping(source = "status", target = "status")
    CourseEnquiryDto toCourseEnquiryDto(Enquiry enquiry);

    List<CourseEnquiryDto> toCourseEnquiryDtos(List<Enquiry> enquiries);
}
