package ee.bcskoolitus.persistance.training.fundingtype;

import ee.bcskoolitus.persistance.fundingtype.FundingType;
import ee.bcskoolitus.persistance.training.Training;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
@Table(name = "training_funding_type", schema = "bcs_koolitused")
public class TrainingFundingType {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false)
    private Integer id;

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "training_id", nullable = false)
    private Training training;

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "funding_type_id", nullable = false)
    private FundingType fundingType;


}