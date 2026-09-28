package ee.bcskoolitus.service;

import ee.bcskoolitus.controller.lecturer.dto.LecturerDto;
import ee.bcskoolitus.infrastructure.exception.PrimaryKeyNotFoundException;
import ee.bcskoolitus.persistance.lecturer.Lecturer;
import ee.bcskoolitus.persistance.lecturer.LecturerMapper;
import ee.bcskoolitus.persistance.lecturer.LecturerRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class LecturerService {

    private final LecturerRepository lecturerRepository;
    private final LecturerMapper lecturerMapper;

    public List<LecturerDto> findLecturers(String search) {
        List<Lecturer> lecturers = lecturerRepository.findLecturersBy(search.trim());
        return lecturerMapper.toLecturerDtos(lecturers);
    }

    public Lecturer getValidLecturerBy(Integer lecturerId, String fieldName) {
        return lecturerRepository.findById(lecturerId)
                .orElseThrow(() -> new PrimaryKeyNotFoundException(fieldName, lecturerId));
    }
}
