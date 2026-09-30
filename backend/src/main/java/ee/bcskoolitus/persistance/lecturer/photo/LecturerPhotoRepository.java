package ee.bcskoolitus.persistance.lecturer.photo;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.Optional;

public interface LecturerPhotoRepository extends JpaRepository<LecturerPhoto, Integer> {

    @Query("select lp from LecturerPhoto lp where lp.lecturer.id = :lecturerId")
    Optional<LecturerPhoto> findLecturerPhotoBy(Integer lecturerId);
}
