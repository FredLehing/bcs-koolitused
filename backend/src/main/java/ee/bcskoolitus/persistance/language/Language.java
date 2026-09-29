package ee.bcskoolitus.persistance.language;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
@Table(name = "language", schema = "bcs_koolitused")
public class Language {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false)
    private Integer id;

    @Size(max = 2)
    @NotNull
    @Column(name = "code", nullable = false, length = 2)
    private String code;

    @Size(max = 50)
    @NotNull
    @Column(name = "name", nullable = false, length = 50)
    private String name;

    @NotNull
    @Column(name = "is_main_language", nullable = false)
    private Boolean isMainLanguage;

    @NotNull
    @Column(name = "requires_translation", nullable = false)
    private Boolean requiresTranslation;

    @Size(max = 10)
    @NotNull
    @Column(name = "flag_icon_code", nullable = false, length = 10)
    private String flagIconCode;


}