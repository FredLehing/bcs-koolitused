package ee.bcskoolitus.persistance.user;

import ee.bcskoolitus.controller.login.dto.LoginResponse;
import org.mapstruct.*;

@Mapper(unmappedTargetPolicy = ReportingPolicy.IGNORE, componentModel = MappingConstants.ComponentModel.SPRING)
public interface UserMapper {

    @Mapping(source = "id", target = "userId")
    @Mapping(source = "role.name", target = "roleName")
    @Mapping(ignore = true, target = "systemLanguages")
    LoginResponse toLoginResponse(User user);

}