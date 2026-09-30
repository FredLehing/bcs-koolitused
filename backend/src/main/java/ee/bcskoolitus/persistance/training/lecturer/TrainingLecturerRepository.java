package ee.bcskoolitus.persistance.training.lecturer;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface TrainingLecturerRepository extends JpaRepository<TrainingLecturer, Integer> {

    // Koolitaja laetakse kohe kaasa (nimi läheb vastusesse)
    @Query("""
            select tl from TrainingLecturer tl
            join fetch tl.lecturer
            where tl.training.id = :trainingId
            order by tl.sortOrder""")
    List<TrainingLecturer> findTrainingLecturersBy(Integer trainingId);

    // Kustutab kohe andmebaasist (mitte flush'i ajal) — samad paarid saab samas transaktsioonis uuesti lisada
    // ilma training_lecturer_uq veata
    @Modifying(flushAutomatically = true)
    @Query("delete from TrainingLecturer tl where tl.training.id = :trainingId")
    void deleteTrainingLecturersBy(Integer trainingId);
}
