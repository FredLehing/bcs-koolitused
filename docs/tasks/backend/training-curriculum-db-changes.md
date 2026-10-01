# Koolituse õppekava: andmebaas, entity ja failinime puhastus

**Teenus:** — (eeltöö õppekava teenustele)

**Kasutav vaade:** `TrainingFormView.vue` (`/training-form`, kõik olekud)

> Mockupi pilt lisatakse hiljem. Seni vt plaani `docs/mock-wireframe/loo-mock-vaade/form-view/training-curriculum-plaan.md`, skeeme `docs/mock-wireframe/loo-mock-vaade/form-view/training-form-view-skeemid.md` (jaotised "Otsused", 9 ja 10) ja läbimängu `docs/mock-wireframe/loo-mock-vaade/form-view/training-form-view-labimang.html` (artifact https://claude.ai/artifact/3VjRgmnQK7Ck7qWVHuRK9b).

Kõik õppekava taskid sõltuvad sellest. Tee see esimesena, järgmisena `training-curriculum-upload.md`.

## Sisend

Teenuse sisendeid pole — task lisab andmebaasi tabeli, entity, repository, failinime puhastuse abiklassi ja veakoodid.

## Väljund

### 1. `docs/database/2_create.sql`

Uus tabel `training_translation_curriculum` (1:1 `training_translation`-iga; rida puudub = õppekava pole). Faili baidid on eraldi tabelis, et neid ei loetaks koos tõlkega (nimekirjad, avalik vaade) — sama muster nagu `lecturer_photo`.

```sql
-- Table: training_translation_curriculum (1:1 training_translation'iga; rida puudub = õppekava pole)
CREATE TABLE training_translation_curriculum
(
    id                      serial       NOT NULL,
    training_translation_id int          NOT NULL,
    file                    bytea        NOT NULL,
    file_name               varchar(255) NOT NULL,
    file_size               int          NOT NULL,
    created_at              timestamp    NOT NULL,
    updated_at              timestamp    NOT NULL,
    CONSTRAINT training_translation_curriculum_pk PRIMARY KEY (id),
    CONSTRAINT training_translation_curriculum_uq UNIQUE (training_translation_id)
);

-- Reference: training_translation_curriculum_training_translation (table: training_translation_curriculum)
ALTER TABLE training_translation_curriculum
    ADD CONSTRAINT training_translation_curriculum_training_translation
        FOREIGN KEY (training_translation_id)
            REFERENCES training_translation (id)
            NOT DEFERRABLE
                INITIALLY IMMEDIATE
;
```

| Veerg | Tähendus |
|---|---|
| `file` | PDF-faili baidid |
| `file_name` | backendi tehtud failinimi, nt `tehisaru-toovahendid-arendajale-oppekava.pdf` (vt "Failinime reegel") |
| `file_size` | faili suurus baitides — vorm kuvab "(1,2 MB)" ilma faili lugemata |

Tabeli koht failis: tähestikulises järjekorras teiste tabelite vahel (`training_translation` järel), viide `Reference` plokkide hulgas.

`1_reset_database.sql` ei muutu (kustutab kogu skeemi). `3_import.sql`-i seed'i ei lisata — testfaili baite SQL-i ei panda; õppekava lisatakse vormist.

### 2. Entity ja repository

Pakett `persistance/training/translation/curriculum/` (nagu `persistance/lecturer/photo/`):

- `TrainingTranslationCurriculum` — `@ManyToOne`/`@OneToOne` seos `TrainingTranslation`-iga (`training_translation_id`), `byte[] file`, `fileName`, `fileSize`, `createdAt`, `updatedAt` (auditeerimine nagu teistel entity'del).
- `TrainingTranslationCurriculumRepository`:
  - `findTrainingTranslationCurriculumBy(trainingTranslationId)` → `Optional`;
  - `deleteTrainingTranslationCurriculumBy(trainingTranslationId)`;
  - päring, mis tagastab ainult `file_name` ja `file_size` (ilma `file` baitideta) — kasutab `GET /api/training-translation/{id}` (vt `GET-api-training-translation-trainingTranslationId-curriculum.md`).

### 3. Failinime puhastus: `infrastructure/util/FileNameSanitizer.java`

Staatiline abiklass (nagu `HtmlSanitizer`). Backendis seni sellist puhastust pole.

`createCurriculumFileName(String title, String label, Integer trainingId)` → `<pealkiri>-<sõna>.pdf`

#### Failinime reegel

1. Pealkiri ja sõna (`curriculumLabel`) puhastatakse eraldi:
   - `java.text.Normalizer` NFD + diakriitikute eemaldamine (`\p{M}`): `õ→o`, `ä→a`, `ö→o`, `ü→u`, `š→s`, `ž→z`;
   - väiketähtedeks (`Locale.ROOT`);
   - kõik, mis pole `a–z` ega `0–9`, → `-`; korduvad `-` üheks, otstest ära;
   - pealkirja osa kuni 100 märki (lõigatakse viimase `-` kohalt).
2. Kui pealkirjast jääb tühi tekst (nt ainult sümbolid või kirillitsa) → `koolitus-<trainingId>`.
3. Kui sõnast jääb tühi tekst → see osa jäetakse ära (`<pealkiri>.pdf`).

| Pealkiri | Sõna | Tulemus |
|---|---|---|
| Tehisaru töövahendid arendajale | Õppekava | `tehisaru-toovahendid-arendajale-oppekava.pdf` |
| AI tools for developers | Curriculum | `ai-tools-for-developers-curriculum.pdf` |
| Java & Spring: algkursus (2026) | Õppekava | `java-spring-algkursus-2026-oppekava.pdf` |
| Java algkursus | Õppekava | `java-algkursus-oppekava.pdf` |
| Курс Java | Õppekava | `java-oppekava.pdf` (kirillitsa kaob, transliteratsiooni ei tehta) |
| Курс (trainingId 12) | Õppekava | `koolitus-12-oppekava.pdf` |
| Java algkursus | `!!!` | `java-algkursus.pdf` |

### 4. Veakoodid `Error.java`

```java
CURRICULUM_TYPE_NOT_ALLOWED("Lubatud on ainult PDF-fail"),
CURRICULUM_TOO_LARGE("Õppekava on liiga suur, lubatud kuni 10 MB"),
```

(`PHOTO_*` koodide järele.) Kasutab `training-curriculum-upload.md`.

## Eesmärk

Admin saab koolituse vormis (`/training-form`) lisada igale tõlkele ühe PDF-faili "Õppekava". See task loob andmete hoidmise ja failinime reegli; teenused tulevad järgmistes taskides.

## Seotud andmebaasi tabelid

Vt `docs/database/2_create.sql`.

### `training_translation`

```sql
CREATE TABLE training_translation
(
    id                serial       NOT NULL,
    training_id       int          NOT NULL,
    language_id       int          NOT NULL,
    title             varchar(255) NOT NULL,
    short_description varchar(255) NOT NULL,
    description       text         NOT NULL,
    created_at        timestamp    NOT NULL,
    updated_at        timestamp    NOT NULL,
    CONSTRAINT training_translation_pk PRIMARY KEY (id),
    CONSTRAINT training_translation_uq UNIQUE (training_id, language_id)
);
```

Näidisandmed (`3_import.sql`): 1 "Java algkursus" (et), 2 "Java Basics" (en), 3 "Projektijuhtimise põhitõed" (et) … Õppekavasid seed'is pole.

## Veaolukorrad

Teenust pole, seega HTTP veaolukordi pole. `FileNameSanitizer` ei viska erindit — tühja tulemuse korral kasutatakse varunime.

## Vastuvõtu kriteeriumid

- [x] `2_create.sql`: tabel `training_translation_curriculum` + FK; skriptid `1_reset` → `2_create` → `3_import` jooksevad vigadeta (kasutaja kontrollib)
- [x] Entity ja repository on olemas; nime ja suuruse päring ei loe `file` veergu
- [x] `FileNameSanitizer` annab tabeli näidete tulemused (ühiktestid iga rea kohta)
- [x] `Error` enumis on `CURRICULUM_TYPE_NOT_ALLOWED` ja `CURRICULUM_TOO_LARGE`
- [x] Olemasolevad testid lähevad läbi
