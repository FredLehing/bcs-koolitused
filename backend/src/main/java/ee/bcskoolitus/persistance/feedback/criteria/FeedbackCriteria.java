package ee.bcskoolitus.persistance.feedback.criteria;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.Instant;

// Tagasiside kriteerium; tekst (title, description) on tõlgetes
@Getter
@Setter
@Entity
@EntityListeners(AuditingEntityListener.class)
@Table(name = "feedback_criteria", schema = "bcs_koolitused")
public class FeedbackCriteria {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false)
    private Integer id;

    @NotNull
    @Column(name = "sequence", nullable = false)
    private Integer sequence;

    // FeedbackCriteriaStatus: A = aktiivne, D = kustutatud
    @Size(max = 1)
    @NotNull
    @Column(name = "status", nullable = false, length = 1)
    private String status;

    @NotNull
    @CreatedDate
    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @NotNull
    @LastModifiedDate
    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;
}
