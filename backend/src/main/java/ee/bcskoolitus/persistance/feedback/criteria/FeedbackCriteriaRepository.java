package ee.bcskoolitus.persistance.feedback.criteria;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface FeedbackCriteriaRepository extends JpaRepository<FeedbackCriteria, Integer> {

    // Vormi järjekord: sequence, võrdsuse korral id
    List<FeedbackCriteria> findAllByOrderBySequenceAscIdAsc();
}
