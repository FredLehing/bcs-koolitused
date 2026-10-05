package ee.bcskoolitus.persistance.user;

import ee.bcskoolitus.controller.login.dto.LoginResponse;
import ee.bcskoolitus.controller.user.dto.AdminUserDto;
import org.mapstruct.*;

@Mapper(unmappedTargetPolicy = ReportingPolicy.IGNORE, componentModel = MappingConstants.ComponentModel.SPRING)
public interface UserMapper {

    @Mapping(source = "id", target = "userId")
    @Mapping(source = "role.name", target = "roleName")
    @Mapping(ignore = true, target = "systemLanguages")
    LoginResponse toLoginResponse(User user);

    // Osaleja väljad ja registreerumised määrab AdminUserService
    @Mapping(source = "id", target = "userId")
    @Mapping(source = "email", target = "email")
    @Mapping(source = "role.name", target = "roleName")
    @Mapping(source = "status", target = "status")
    @Mapping(source = "createdAt", target = "createdAt")
    @Mapping(target = "participantId", ignore = true)
    @Mapping(target = "participantName", ignore = true)
    @Mapping(target = "profileEmail", ignore = true)
    @Mapping(target = "phone", ignore = true)
    @Mapping(target = "registrations", ignore = true)
    AdminUserDto toAdminUserDto(User user);

}