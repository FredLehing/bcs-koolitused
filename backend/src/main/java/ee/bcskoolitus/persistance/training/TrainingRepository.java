package ee.bcskoolitus.persistance.training;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface TrainingRepository extends JpaRepository<Training, Integer> {
    @Query("""
            select t from Training t where (:categoryId is null or t.category.id = :categoryId) 
            and (:trainingLanguageCode is null or t.trainingLanguage.code = :trainingLanguageCode) 
            and (:fundingTypeId is null or exists (
                        select tft from TrainingFundingType tft 
                                    where tft.training = t and tft.fundingType.id = :fundingTypeId))""")

    Page<Training> findFilteredTrainingsBy(Integer categoryId, String trainingLanguageCode, Pageable pageable);


}