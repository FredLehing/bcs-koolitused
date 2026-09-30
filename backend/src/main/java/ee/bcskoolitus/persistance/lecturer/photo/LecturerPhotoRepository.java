package ee.bcskoolitus.persistance.lecturer.photo;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

public interface LecturerPhotoRepository extends JpaRepository<LecturerPhoto, Integer> {

    @Query("select lp from LecturerPhoto lp where lp.lecturer.id = :lecturerId")
    Optional<LecturerPhoto> findLecturerPhotoBy(Integer lecturerId);

    // Ainult pildi versioon (updated_at) — pildi baite ei loeta
    @Query("select lp.updatedAt from LecturerPhoto lp where lp.lecturer.id = :lecturerId")
    Optional<Instant> findLecturerPhotoUpdatedAtBy(Integer lecturerId);

    // Nimekirja jaoks: [lecturerId, updatedAt] paarid, pildi baite ei loeta
    @Query("select lp.lecturer.id, lp.updatedAt from LecturerPhoto lp where lp.lecturer.id in :lecturerIds")
    List<Object[]> findLecturerPhotoUpdatedAtsBy(List<Integer> lecturerIds);

    @Modifying(flushAutomatically = true)
    @Query("delete from LecturerPhoto lp where lp.lecturer.id = :lecturerId")
    void deleteLecturerPhotoBy(Integer lecturerId);
}
