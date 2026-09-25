package ee.bcskoolitus.persistance.view.trainingsummary;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface TrainingSummaryRepository extends JpaRepository<TrainingSummary, Long> {
    @Query("""
            select ts from TrainingSummary ts
            where (:categoryId = 0 or ts.categoryId = :categoryId)
            and (:fundingTypeId = 0 or exists (
                        select tft from TrainingFundingType tft
                        where tft.training = ts.training and tft.fundingType.id = :fundingTypeId))
            and (:trainingLanguageId = 0 or ts.trainingLanguageId = :trainingLanguageId)
            and ts.translationLanguageCode = :contentLang
            order by ts.training.isPromoted desc , ts.title""")
    Page<TrainingSummary> findFilteredTrainingSummariesBy(Integer categoryId, Integer fundingTypeId, Integer trainingLanguageId, String contentLang, Pageable pageable);

}
