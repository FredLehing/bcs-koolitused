package ee.bcskoolitus.persistance.training.translation.curriculum;

import ee.bcskoolitus.persistance.training.translation.TrainingTranslation;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.Instant;

// Eraldi tabelis, et faili baite (bytea) ei loetaks koos tõlkega (nimekirjad, avalik vaade).
// 1:1 training_translation'iga (training_translation_curriculum_uq); rida puudub = õppekava pole.
@Getter
@Setter
@Entity
@EntityListeners(AuditingEntityListener.class)
@Table(name = "training_translation_curriculum", schema = "bcs_koolitused")
public class TrainingTranslationCurriculum {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false)
    private Integer id;

    @NotNull
    @OneToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "training_translation_id", nullable = false)
    private TrainingTranslation trainingTranslation;

    @NotNull
    @Column(name = "file", nullable = false)
    private byte[] file;

    // Backendi tehtud failinimi (FileNameSanitizer), nt tehisaru-toovahendid-arendajale-oppekava.pdf
    @Size(max = 255)
    @NotNull
    @Column(name = "file_name", nullable = false)
    private String fileName;

    // Baitides — vorm kuvab suuruse ilma faili lugemata
    @NotNull
    @Column(name = "file_size", nullable = false)
    private Integer fileSize;

    @NotNull
    @CreatedDate
    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @NotNull
    @LastModifiedDate
    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

}
