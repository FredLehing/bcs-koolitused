package ee.bcskoolitus.service;

import ee.bcskoolitus.infrastructure.exception.ForbiddenException;
import ee.bcskoolitus.persistance.lecturer.Lecturer;
import ee.bcskoolitus.persistance.lecturer.photo.LecturerPhoto;
import ee.bcskoolitus.persistance.lecturer.photo.LecturerPhotoRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.time.Instant;
import java.util.Base64;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class LecturerPhotoServiceTest {

    @Mock
    private LecturerPhotoRepository lecturerPhotoRepository;

    @InjectMocks
    private LecturerPhotoService lecturerPhotoService;

    @Test
    void saveLecturerPhoto_newPhotoIsNormalizedToJpeg() throws IOException {
        Lecturer lecturer = createLecturer();
        when(lecturerPhotoRepository.findLecturerPhotoBy(2)).thenReturn(Optional.empty());

        lecturerPhotoService.saveLecturerPhoto(lecturer, createPngBase64(), "image/png");

        ArgumentCaptor<LecturerPhoto> lecturerPhotoCaptor = ArgumentCaptor.forClass(LecturerPhoto.class);
        verify(lecturerPhotoRepository).save(lecturerPhotoCaptor.capture());
        LecturerPhoto savedLecturerPhoto = lecturerPhotoCaptor.getValue();
        assertSame(lecturer, savedLecturerPhoto.getLecturer());
        assertEquals("image/jpeg", savedLecturerPhoto.getContentType());
        assertEquals((byte) 0xFF, savedLecturerPhoto.getPhoto()[0]);
    }

    @Test
    void saveLecturerPhoto_existingPhotoIsReplaced() throws IOException {
        LecturerPhoto existingLecturerPhoto = new LecturerPhoto();
        existingLecturerPhoto.setId(7);
        existingLecturerPhoto.setPhoto(new byte[]{1});
        when(lecturerPhotoRepository.findLecturerPhotoBy(2)).thenReturn(Optional.of(existingLecturerPhoto));

        lecturerPhotoService.saveLecturerPhoto(createLecturer(), createPngBase64(), "image/png");

        verify(lecturerPhotoRepository).save(existingLecturerPhoto);
        assertEquals(7, existingLecturerPhoto.getId());
        assertEquals((byte) 0xFF, existingLecturerPhoto.getPhoto()[0]);
    }

    @Test
    void saveLecturerPhoto_notAllowedTypeThrows() throws IOException {
        ForbiddenException exception = assertThrows(ForbiddenException.class,
                () -> lecturerPhotoService.saveLecturerPhoto(createLecturer(), createPngBase64(), "image/gif"));

        assertEquals("PHOTO_TYPE_NOT_ALLOWED", exception.getErrorCode());
        verify(lecturerPhotoRepository, never()).save(any());
    }

    @Test
    void saveLecturerPhoto_missingTypeThrows() throws IOException {
        ForbiddenException exception = assertThrows(ForbiddenException.class,
                () -> lecturerPhotoService.saveLecturerPhoto(createLecturer(), createPngBase64(), null));

        assertEquals("PHOTO_TYPE_NOT_ALLOWED", exception.getErrorCode());
    }

    @Test
    void saveLecturerPhoto_tooLargePhotoThrows() {
        String tooLargePhoto = Base64.getEncoder().encodeToString(new byte[LecturerPhotoService.MAX_PHOTO_BYTES + 1]);

        ForbiddenException exception = assertThrows(ForbiddenException.class,
                () -> lecturerPhotoService.saveLecturerPhoto(createLecturer(), tooLargePhoto, "image/jpeg"));

        assertEquals("PHOTO_TOO_LARGE", exception.getErrorCode());
        assertEquals("Pilt on liiga suur, lubatud kuni 2 MB", exception.getMessage());
    }

    @Test
    void saveLecturerPhoto_notAnImageThrowsTypeNotAllowed() {
        String notAnImage = Base64.getEncoder().encodeToString("hello".getBytes());

        ForbiddenException exception = assertThrows(ForbiddenException.class,
                () -> lecturerPhotoService.saveLecturerPhoto(createLecturer(), notAnImage, "image/png"));

        assertEquals("PHOTO_TYPE_NOT_ALLOWED", exception.getErrorCode());
    }

    @Test
    void findPhotoVersion_returnsEpochSecondsOrNull() {
        when(lecturerPhotoRepository.findLecturerPhotoUpdatedAtBy(1)).thenReturn(Optional.of(Instant.ofEpochSecond(1784095200)));
        when(lecturerPhotoRepository.findLecturerPhotoUpdatedAtBy(2)).thenReturn(Optional.empty());

        assertEquals(1784095200L, lecturerPhotoService.findPhotoVersion(1));
        assertNull(lecturerPhotoService.findPhotoVersion(2));
    }

    @Test
    void findPhotoVersions_returnsOnlyLecturersWithPhoto() {
        List<Object[]> rows = List.<Object[]>of(new Object[]{1, Instant.ofEpochSecond(100)});
        when(lecturerPhotoRepository.findLecturerPhotoUpdatedAtsBy(List.of(1, 2))).thenReturn(rows);

        assertEquals(Map.of(1, 100L), lecturerPhotoService.findPhotoVersions(List.of(1, 2)));
    }

    private static Lecturer createLecturer() {
        Lecturer lecturer = new Lecturer();
        lecturer.setId(2);
        return lecturer;
    }

    private static String createPngBase64() throws IOException {
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        ImageIO.write(new BufferedImage(50, 30, BufferedImage.TYPE_INT_ARGB), "png", outputStream);
        return Base64.getEncoder().encodeToString(outputStream.toByteArray());
    }
}
