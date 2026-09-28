package ee.bcskoolitus.persistance.lecturer;

import ee.bcskoolitus.controller.lecturer.dto.LecturerDto;
import org.mapstruct.*;

import java.util.Base64;
import java.util.List;

@Mapper(unmappedTargetPolicy = ReportingPolicy.IGNORE, componentModel = MappingConstants.ComponentModel.SPRING)
public interface LecturerMapper {

    @Mapping(source = "id", target = "lecturerId")
    @Mapping(source = "fullName", target = "lecturerName")
    @Mapping(source = "photo", target = "lecturerPhoto", qualifiedByName = "bytesToBase64")
    LecturerDto toLecturerDto(Lecturer lecturer);

    List<LecturerDto> toLecturerDtos(List<Lecturer> lecturers);

    // Foto (bytea) tagastatakse Base64 stringina — mitte StringBytesConverter-iga, mis teeb UTF-8 teisenduse
    @Named("bytesToBase64")
    default String bytesToBase64(byte[] photo) {
        return photo == null ? "" : Base64.getEncoder().encodeToString(photo);
    }

}
