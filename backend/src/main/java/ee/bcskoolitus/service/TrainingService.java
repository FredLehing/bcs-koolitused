package ee.bcskoolitus.service;

import ee.bcskoolitus.persistance.training.TrainingRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class TrainingService {


    private final TrainingRepository trainingRepository;

    public void getTrainings(Integer categoryId, Integer fundingTypeId, Integer limit, Integer page, String trainingLang, String translationLang) {
        Pageable pageable = PageRequest.of(page, limit);

    }
}
