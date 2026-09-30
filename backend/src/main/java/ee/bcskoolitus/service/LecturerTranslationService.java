package ee.bcskoolitus.service;

import ee.bcskoolitus.LecturerStatus;
import ee.bcskoolitus.controller.lecturertranslation.dto.LecturerTranslationDto;
import ee.bcskoolitus.infrastructure.exception.PrimaryKeyNotFoundException;
import ee.bcskoolitus.persistance.lecturer.translation.LecturerTranslation;
import ee.bcskoolitus.persistance.lecturer.translation.LecturerTranslationMapper;
import ee.bcskoolitus.persistance.lecturer.translation.LecturerTranslationRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class LecturerTranslationService {

    private final LecturerTranslationRepository lecturerTranslationRepository;
    private final LecturerTranslationMapper lecturerTranslationMapper;

    @Transactional(readOnly = true)
    public LecturerTranslationDto getLecturerTranslation(Integer lecturerTranslationId) {
        LecturerTranslation lecturerTranslation = getValidLecturerTranslationBy(lecturerTranslationId);
        return lecturerTranslationMapper.toLecturerTranslationDto(lecturerTranslation);
    }

    // Kustutatud koolitaja (status D) tõlge on nagu olematu → 404
    public LecturerTranslation getValidLecturerTranslationBy(Integer lecturerTranslationId) {
        LecturerTranslation lecturerTranslation = lecturerTranslationRepository.findById(lecturerTranslationId)
                .orElseThrow(() -> new PrimaryKeyNotFoundException("lecturerTranslationId", lecturerTranslationId));
        if (LecturerStatus.DELETED.getCode().equals(lecturerTranslation.getLecturer().getStatus())) {
            throw new PrimaryKeyNotFoundException("lecturerTranslationId", lecturerTranslationId);
        }
        return lecturerTranslation;
    }

    // Teisele koolitajale kuuluv tõlge käitub nagu olematu tõlge (404)
    public LecturerTranslation getValidLecturerTranslationBy(Integer lecturerTranslationId, Integer lecturerId) {
        return lecturerTranslationRepository.findLecturerTranslationBy(lecturerTranslationId, lecturerId)
                .orElseThrow(() -> new PrimaryKeyNotFoundException("lecturerTranslationId", lecturerTranslationId));
    }
}
