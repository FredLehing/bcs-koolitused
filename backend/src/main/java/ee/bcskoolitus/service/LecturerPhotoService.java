package ee.bcskoolitus.service;

import ee.bcskoolitus.infrastructure.exception.ForbiddenException;
import ee.bcskoolitus.infrastructure.exception.PrimaryKeyNotFoundException;
import ee.bcskoolitus.infrastructure.util.PhotoNormalizer;
import ee.bcskoolitus.persistance.lecturer.Lecturer;
import ee.bcskoolitus.persistance.lecturer.photo.LecturerPhoto;
import ee.bcskoolitus.persistance.lecturer.photo.LecturerPhotoRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.Base64;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static ee.bcskoolitus.Error.PHOTO_TOO_LARGE;
import static ee.bcskoolitus.Error.PHOTO_TYPE_NOT_ALLOWED;

// Koolitaja pilt (lecturer_photo): kontroll, normaliseerimine, salvestamine ja versioon (photoVersion)
@Service
@RequiredArgsConstructor
public class LecturerPhotoService {

    static final Set<String> ALLOWED_CONTENT_TYPES = Set.of("image/png", "image/jpeg", "image/webp");
    static final int MAX_PHOTO_BYTES = 2 * 1024 * 1024;

    private final LecturerPhotoRepository lecturerPhotoRepository;

    // Pildita koolitaja → 404 (sama sõnum nagu olematu koolitaja korral)
    public LecturerPhoto getValidLecturerPhotoBy(Integer lecturerId) {
        return lecturerPhotoRepository.findLecturerPhotoBy(lecturerId)
                .orElseThrow(() -> new PrimaryKeyNotFoundException("lecturerId", lecturerId));
    }

    // photoVersion = lecturer_photo.updated_at epoch-sekundites; null = pilti pole
    public Long findPhotoVersion(Integer lecturerId) {
        return lecturerPhotoRepository.findLecturerPhotoUpdatedAtBy(lecturerId)
                .map(Instant::getEpochSecond)
                .orElse(null);
    }

    // lecturerId → photoVersion; pildita koolitajat mapis pole
    public Map<Integer, Long> findPhotoVersions(List<Integer> lecturerIds) {
        Map<Integer, Long> photoVersions = new HashMap<>();
        if (lecturerIds.isEmpty()) {
            return photoVersions;
        }
        for (Object[] row : lecturerPhotoRepository.findLecturerPhotoUpdatedAtsBy(lecturerIds)) {
            photoVersions.put((Integer) row[0], ((Instant) row[1]).getEpochSecond());
        }
        return photoVersions;
    }

    // Lisab või asendab pildi; Base64 õigsust kontrollib DTO (@ValidBase64)
    public void saveLecturerPhoto(Lecturer lecturer, String photoBase64, String photoContentType) {
        byte[] normalizedPhoto = createNormalizedPhoto(photoBase64, photoContentType);
        LecturerPhoto lecturerPhoto = lecturerPhotoRepository.findLecturerPhotoBy(lecturer.getId())
                .orElseGet(LecturerPhoto::new);
        lecturerPhoto.setLecturer(lecturer);
        lecturerPhoto.setPhoto(normalizedPhoto);
        lecturerPhoto.setContentType(PhotoNormalizer.CONTENT_TYPE);
        lecturerPhotoRepository.save(lecturerPhoto);
    }

    public void deleteLecturerPhoto(Integer lecturerId) {
        lecturerPhotoRepository.deleteLecturerPhotoBy(lecturerId);
    }

    // Tüüp → suurus → kas on loetav pilt (loetamatu fail on "vale tüüp")
    static byte[] createNormalizedPhoto(String photoBase64, String photoContentType) {
        if (photoContentType == null || !ALLOWED_CONTENT_TYPES.contains(photoContentType)) {
            throw new ForbiddenException(PHOTO_TYPE_NOT_ALLOWED.getMessage(), PHOTO_TYPE_NOT_ALLOWED.name());
        }
        byte[] photoBytes = Base64.getDecoder().decode(photoBase64);
        if (photoBytes.length > MAX_PHOTO_BYTES) {
            throw new ForbiddenException(PHOTO_TOO_LARGE.getMessage(), PHOTO_TOO_LARGE.name());
        }
        byte[] normalizedPhoto = PhotoNormalizer.normalize(photoBytes);
        if (normalizedPhoto == null) {
            throw new ForbiddenException(PHOTO_TYPE_NOT_ALLOWED.getMessage(), PHOTO_TYPE_NOT_ALLOWED.name());
        }
        return normalizedPhoto;
    }
}
