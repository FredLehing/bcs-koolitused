package ee.bcskoolitus.controller.login.dto;

import ee.bcskoolitus.controller.common.dto.SystemLanguageDto;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class LoginResponse implements Serializable {
    private Integer userId;
    private String roleName;
    private List<SystemLanguageDto> systemLanguages;
}
