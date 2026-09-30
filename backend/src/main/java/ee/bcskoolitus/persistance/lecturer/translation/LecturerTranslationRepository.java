package ee.bcskoolitus.persistance.lecturer.translation;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;

public interface LecturerTranslationRepository extends JpaRepository<LecturerTranslation, Integer> {

    List<LecturerTranslation> findAllByLecturer_IdOrderByLanguage_IdAsc(Integer lecturerId);

    boolean existsByLecturer_IdAndLanguage_Id(Integer lecturerId, Integer languageId);

    @Query("select lt from LecturerTranslation lt where lt.id = :lecturerTranslationId and lt.lecturer.id = :lecturerId")
    Optional<LecturerTranslation> findLecturerTranslationBy(Integer lecturerTranslationId, Integer lecturerId);

    // Kuvamiseks sobivad tõlked: contentLang keele tõlge (kui on) eespool, põhikeele tõlge tagavaraks
    @Query("""
            select lt from LecturerTranslation lt
            join fetch lt.language l
            where lt.lecturer.id in :lecturerIds
              and (l.code = :contentLang or l.isMainLanguage = true)
            order by case when l.code = :contentLang then 0 else 1 end""")
    List<LecturerTranslation> findDisplayLecturerTranslationsBy(List<Integer> lecturerIds, String contentLang);
}
