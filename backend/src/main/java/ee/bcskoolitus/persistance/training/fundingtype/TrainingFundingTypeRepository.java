package ee.bcskoolitus.persistance.training.fundingtype;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface TrainingFundingTypeRepository extends JpaRepository<TrainingFundingType, Integer> {

    List<TrainingFundingType> findAllByTraining_IdOrderByFundingType_IdAsc(Integer trainingId);
}
