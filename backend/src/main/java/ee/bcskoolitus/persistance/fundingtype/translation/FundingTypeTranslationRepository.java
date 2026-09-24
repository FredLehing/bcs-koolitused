package ee.bcskoolitus.persistance.fundingtype.translation;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface FundingTypeTranslationRepository extends JpaRepository<FundingTypeTranslation, Integer> {
    @Query("""
            select ftt from FundingTypeTranslation ftt
            where ftt.language.code = :contentLang
            and exists (
                        select tft from TrainingFundingType tft
                        where tft.training.id = :trainingId and tft.fundingType = ftt.fundingType)
            order by ftt.fundingType.id asc""")
    List<FundingTypeTranslation> findTrainingFundingTypeTranslationsBy(Integer trainingId, String contentLang);


}
