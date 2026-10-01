# Koolituse tagasiside andmebaasi ja entity muudatused

**Teenus:** — (eeltöö; teenuseid ei lisa)

**Kasutav vaade:** `ParticipantFeedbackFormView.vue`, `ParticipantCoursesView.vue`

> Mockupi pilt lisatakse hiljem. Seni vt läbimängu `docs/mock-wireframe/loo-mock-vaade/participant-feedback-form-view/participant-feedback-form-view-labimang.html` ja skeeme `participant-feedback-form-view-skeemid.md` (jaotis 1).

Kõik tagasiside taskid sõltuvad sellest. Andmebaasi skripte käivitab ja kontrollib kasutaja (`1_reset` → `2_create` → `3_import`).

## Sisend

Andmebaasi ettepanek failist `docs/mock-wireframe/loo-mock-vaade/participant-feedback-form-view/participant-feedback-form-view-skeemid.md`, jaotis 1 (DDL ja DML on seal täielikult kirjas).

## Väljund

### 1. `docs/database/2_create.sql`

- Uued tabelid `feedback_criteria` (`sequence`, `status` A/D, auditiveerud) ja `feedback_criteria_translation` (`title` varchar(50), `description` varchar(255), `UNIQUE (feedback_criteria_id, language_id)`).
- Olemasolev tabel `feedback` (seni ainult `id`) saab veerud `course_participant_id` (`UNIQUE`), `status` varchar(1) (N/U/H), `created_at`, `updated_at`.
- Uus tabel `course_participant_feedback`: `feedback_id`, `feedback_criteria_id`, `score` (`CHECK (score BETWEEN 1 AND 10)`), `feedback_text` varchar(255) NULL, auditiveerud, `UNIQUE (feedback_id, feedback_criteria_id)`.
- Viis välisvõtit (skeemide tabel "Viited") faili lõpus, olemasoleva `ALTER TABLE … ADD CONSTRAINT … FOREIGN KEY` mustri järgi.
- `course_participant` **ei muutu**.

### 2. `docs/database/3_import.sql`

- Eemaldatakse read `INSERT INTO feedback DEFAULT VALUES;` (2 tk).
- `feedback_criteria`: 5 rida (id 1–5, `sequence` 1–5, `A`).
- `feedback_criteria_translation`: 10 rida (iga kriteerium `et` ja `en`).
- `course_participant`: olemasolevasse INSERT-i rida **10** — Anna Saar (osaleja 1), toimumiskord 7, `R`, tasutud.
- `feedback`: rida 1 (`course_participant_id = 10`, `H`).
- `course_participant_feedback`: 5 rida (tagasiside 1 vastused, hinded 9, 10, 8, 7, 9; kommentaar kriteeriumidel 2 ja 4).
- `setval` read: `feedback_criteria`, `feedback_criteria_translation`, `feedback`, `course_participant_feedback`.

### 3. Backend

- Enumid baaspaketis (nagu `RoomStatus`): `FeedbackStatus` — `NEW("N")`, `UPDATED("U")`, `HISTORICAL("H")`; `FeedbackCriteriaStatus` — `ACTIVE("A")`, `DELETED("D")`.
- Entity'd + repositooriumid: `FeedbackCriteria`, `FeedbackCriteriaTranslation`, `Feedback` (entity seni puudub), `CourseParticipantFeedback`. Kõigil `@EntityListeners(AuditingEntityListener.class)`, `createdAt` (`@CreatedDate`, `updatable = false`), `updatedAt` (`@LastModifiedDate`) — vt `backend/CLAUDE.md`.
- `Error` enum, uued väärtused:
  - `FEEDBACK_NOT_ALLOWED("Sellele koolitusele ei saa tagasisidet anda")`
  - `FEEDBACK_ALREADY_EXISTS("Tagasiside on juba antud")`
  - `FEEDBACK_NOT_FOUND("Tagasisidet ei leitud")`
  - `FEEDBACK_CRITERIA_CHANGED("Tagasiside küsimused on vahepeal muutunud, laadi leht uuesti")`

## Eesmärk

Andmemudel osaleja tagasiside jaoks: kriteeriumid koos tõlgetega, tagasiside "päis" registreerumise kohta (staatus admini ülevaatuseks) ja vastused kriteeriumide kaupa.

## Seotud andmebaasi tabelid

`feedback_criteria`, `feedback_criteria_translation`, `feedback`, `course_participant_feedback`, `course_participant` (uus seed rida), `language`.

## Veaolukorrad

—

## Vastuvõtu kriteeriumid

- [ ] `2_create.sql` ja `3_import.sql` käivituvad veata (kontrollib kasutaja)
- [ ] `score` väljaspool 1–10 ja teine tagasiside samale registreerumisele lükatakse andmebaasis tagasi
- [ ] Enumid, entity'd ja repositooriumid on olemas; `createdAt`/`updatedAt` täidab auditeerimine
- [ ] `Error` enumis on 4 uut väärtust
- [ ] Olemasolevad testid lähevad läbi
