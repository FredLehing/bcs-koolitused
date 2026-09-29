package ee.bcskoolitus.persistance.view.trainingsummary;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

// Filtreeritud päring koostatakse TrainingSummarySpecifications abil (otsingusõnade arv on muutuv)
public interface TrainingSummaryRepository extends JpaRepository<TrainingSummary, Long>, JpaSpecificationExecutor<TrainingSummary> {

}
