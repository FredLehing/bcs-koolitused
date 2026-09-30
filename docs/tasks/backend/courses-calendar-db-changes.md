# Toimumiskordade vaadete ja registreerumise andmebaasi ning entity muudatused

**Teenus:** — (eeltöö; teenuseid ei lisa)

**Kasutavad vaated:** `AdminAllCoursesView.vue`, `AdminCourseView.vue`, `CoursesView.vue`, `CourseView.vue`, `CourseRegistrationView.vue`, `SignupView.vue`

> Andmebaasi ettepanek: `docs/mock-wireframe/loo-mock-vaade/courses-view/courses-view-skeemid.md`, jaotis 1. Läbimängud: `docs/mock-wireframe/loo-mock-vaade/courses-view/admin-all-courses-view-labimang.html`, `docs/mock-wireframe/loo-mock-vaade/courses-view/courses-view-labimang.html`, `docs/mock-wireframe/loo-mock-vaade/courses-view/course-registration-view-labimang.html`.

Kõik selle katusharu (`feature/RAIN-courses`) backend taskid sõltuvad sellest. Andmebaasi skripte käivitab ja kontrollib kasutaja (`1_reset` → `2_create` → `3_import`).

## Sisend

Skeemide fail `docs/mock-wireframe/loo-mock-vaade/courses-view/courses-view-skeemid.md`, jaotis 1 (tabelid, view'd, seed-andmed). Osalemisvorm (`option`, `option_translation`, `enquiry.option_id`) on juba eemaldatud.

## Väljund

### 1. `docs/database/2_create.sql`

- `course`: uus veerg `is_promoted boolean NOT NULL DEFAULT false` (`meeting_link` järel).
- `course_participant.status`: `varchar(3)` → `varchar(1)` (`R` = registreerunud, `C` = loobunud); piirang `CONSTRAINT course_participant_uq UNIQUE (course_id, participant_id)`.
- `participant`: piirang `CONSTRAINT participant_user_uq UNIQUE (user_id)` (kasutajal üks oma osaleja).
- `"user"`: piirang `CONSTRAINT user_email_uq UNIQUE (email)`.
- View `course_summary`: `participant_count` loeb ainult `R` osalejaid (`AND cp.status = 'R'`).
- Uued view'd `admin_course_summary` (pärast `admin_training_summary`-t) ja `public_course_summary` — SQL skeemide failist.

### 2. `docs/database/3_import.sql`

- Toimumiskord 1: `is_promoted = true`; uued toimumiskorrad 9–13 (tabel skeemide failis) ja `course_lecturer` read 9–12.
- Profiilid 5–8, kasutajad 4–7 (roll `participant`, parool `parool123`), osalejad 2–5 (igaüks oma kasutajaga).
- `course_participant`: olemasolevad read `'REG'` → `'R'`, lisanduvad read 3–9 (üks `C`).
- `enquiry`: lisandub päring 5 (käsitletud, toimumiskord 12).
- `setval` read uute tabelite/ID-de jaoks (vajadusel).

### 3. Backend

- `CourseParticipantStatus` enum (baaspakk, nagu `CourseStatus`): `REGISTERED("R")`, `CANCELLED("C")`.
- `Course` entity: väli `isPromoted` (`is_promoted`, `@NotNull`).
- `Participant` entity: seos `profile` (`@ManyToOne`, `profile_id`, `NOT NULL`) — praegu puudub.
- `CourseParticipant` entity: seos `participant` (`@ManyToOne`, `participant_id`, `NOT NULL`) — praegu puudub.
- Repositooriumid: `ParticipantRepository` (`findByUserId`), `ProfileRepository`, `CourseParticipantRepository` (`findByCourseIdAndParticipantId`, `findByCourseIdOrderByCreatedAt…`), `RoleRepository` (kui puudub; vaja konto loomisel).
- View entity'd (`@Immutable`) + repositooriumid: `persistance/view/admincoursesummary/AdminCourseSummary`, `persistance/view/publiccoursesummary/PublicCourseSummary` (eeskuju `AdminTrainingSummary`).

## Eesmärk

Uued vaated loevad nimekirjad view'dest (arvud, tõlked, avalikkus), registreerumine vajab osaleja–profiili–toimumiskorra seoseid ja unikaalsuspiiranguid.

## Seotud andmebaasi tabelid

`course`, `course_participant`, `participant`, `profile`, `"user"`, `course_lecturer`, `enquiry`, view'd `course_summary`, `admin_training_summary`.

## Veaolukorrad

—

## Vastuvõtu kriteeriumid

- [ ] `2_create.sql` ja `3_import.sql` käivituvad veata (kontrollib kasutaja)
- [ ] `/admin-all-courses` näidisarvud: toimumiskord 1 → osalejaid 3, tasunud 2, huvilisi 1; 12 → 2 / 1 / 1
- [ ] Entity'd, enum, repositooriumid ja view entity'd on olemas
- [ ] Olemasolevad testid lähevad läbi (`course_summary` osalejate arv muutub ainult `C` rea võrra)
