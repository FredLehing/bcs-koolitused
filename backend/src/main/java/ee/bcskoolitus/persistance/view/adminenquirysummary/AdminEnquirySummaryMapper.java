package ee.bcskoolitus.persistance.view.adminenquirysummary;

import ee.bcskoolitus.controller.enquiry.dto.AdminEnquiryDto;
import ee.bcskoolitus.controller.enquiry.dto.AdminEnquirySummaryDto;
import org.mapstruct.*;

import java.util.List;

@Mapper(unmappedTargetPolicy = ReportingPolicy.IGNORE, componentModel = MappingConstants.ComponentModel.SPRING)
public interface AdminEnquirySummaryMapper {

    @Mapping(source = "enquiryId", target = "enquiryId")
    @Mapping(source = "createdAt", target = "createdAt")
    @Mapping(source = "fullName", target = "fullName")
    @Mapping(source = "email", target = "email")
    @Mapping(source = "companyName", target = "companyName")
    @Mapping(source = "trainingTitle", target = "trainingTitle")
    @Mapping(source = "courseStartDate", target = "courseStartDate")
    @Mapping(source = "courseEndDate", target = "courseEndDate")
    @Mapping(source = "optionName", target = "optionName")
    @Mapping(source = "status", target = "status")
    AdminEnquirySummaryDto toAdminEnquirySummaryDto(AdminEnquirySummary adminEnquirySummary);

    List<AdminEnquirySummaryDto> toAdminEnquirySummaryDtos(List<AdminEnquirySummary> adminEnquirySummaries);

    @Mapping(source = "enquiryId", target = "enquiryId")
    @Mapping(source = "createdAt", target = "createdAt")
    @Mapping(source = "status", target = "status")
    @Mapping(source = "trainingId", target = "trainingId")
    @Mapping(source = "trainingTranslationId", target = "trainingTranslationId")
    @Mapping(source = "trainingTitle", target = "trainingTitle")
    @Mapping(source = "courseId", target = "courseId")
    @Mapping(source = "courseStartDate", target = "courseStartDate")
    @Mapping(source = "courseEndDate", target = "courseEndDate")
    @Mapping(source = "optionName", target = "optionName")
    @Mapping(source = "companyName", target = "companyName")
    @Mapping(source = "message", target = "message")
    @Mapping(source = "fullName", target = "fullName")
    @Mapping(source = "email", target = "email")
    @Mapping(source = "phone", target = "phone")
    AdminEnquiryDto toAdminEnquiryDto(AdminEnquirySummary adminEnquirySummary);
}
