package ee.bcskoolitus.persistance.lecturer.photo;

import ee.bcskoolitus.persistance.lecturer.Lecturer;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.Instant;

// Eraldi tabelis, et pildi baite (bytea) ei loetaks koos koolitajaga (nimekirjad, "Vali koolitaja" otsing).
// 1:1 lecturer'iga (lecturer_photo_uq); rida puudub = pilti pole.
@Getter
@Setter
@Entity
@EntityListeners(AuditingEntityListener.class)
@Table(name = "lecturer_photo", schema = "bcs_koolitused")
public class LecturerPhoto {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false)
    private Integer id;

    @NotNull
    @OneToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "lecturer_id", nullable = false)
    private Lecturer lecturer;

    @NotNull
    @Column(name = "photo", nullable = false)
    private byte[] photo;

    // image/png, image/jpeg või image/webp
    @Size(max = 50)
    @NotNull
    @Column(name = "content_type", nullable = false, length = 50)
    private String contentType;

    @NotNull
    @CreatedDate
    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @NotNull
    @LastModifiedDate
    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;


}
