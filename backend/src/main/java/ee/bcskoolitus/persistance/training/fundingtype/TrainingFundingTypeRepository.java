package ee.bcskoolitus.persistance.training.fundingtype;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface TrainingFundingTypeRepository extends JpaRepository<TrainingFundingType, Integer> {

    List<TrainingFundingType> findAllByTraining_IdOrderByFundingType_IdAsc(Integer trainingId);

    // Kustutab kohe andmebaasist (mitte flush'i ajal) — samad paarid saab samas transaktsioonis uuesti lisada
    // ilma training_funding_type_uq veata
    @Modifying(flushAutomatically = true)
    @Query("delete from TrainingFundingType tft where tft.training.id = :trainingId")
    void deleteTrainingFundingTypesBy(Integer trainingId);
}
