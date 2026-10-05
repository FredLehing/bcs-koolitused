# Koolituse tagasiside — tööde järjekord

`ParticipantFeedbackFormView.vue` (uus), `ParticipantCoursesView.vue` (nupud). Haru `EVELY` (töö on commit'imata kuni kasutaja ülevaatuseni).

Järjekord: **andmebaas ja entity'd → "Minu koolitused" väljad → vormi lugemine → lisamine → muutmine → frontend**. Andmebaasi skripte (`1_reset` → `2_create` → `3_import`) kontrollib kasutaja DB-taski järel.

Allikad: skeemid `participant-feedback-form-view-skeemid.md`, läbimäng `participant-feedback-form-view-labimang.html`, märkmed `docs/mock-wireframe/markmed/participant-feedback-form-view-markmed.md` ja `participant-courses-view-markmed.md`. Taskifailid ja kood on tehtud 2026-10-01 (commit'imata, ootab kasutaja ülevaatust).

## 0. Eeltöö

| # | Töö | Taskifail | Keerukus | Märkus |
|---|---|---|---|---|
| 0.1 | Märkmed läbimängust: Vaate märkmed + API märkmed (GET, POST, PUT), `MyRegistrationDto` uued väljad | `docs/mock-wireframe/markmed/participant-feedback-form-view-markmed.md`, `participant-courses-view-markmed.md` | — | ✅ tehtud 2026-10-01 |
| 0.2 | DDL + seed: `feedback_criteria`, `feedback_criteria_translation`, `feedback` (veerud + `UNIQUE`), `course_participant_feedback` (`CHECK`), FK-d, `course_participant` rida 10, `setval`; entity'd, repositooriumid, enumid `FeedbackStatus` / `FeedbackCriteriaStatus`, `Error` koodid | `docs/tasks/backend/feedback-db-changes.md` | keskmine | ✅ kood tehtud; kõik järgmised sõltuvad sellest; vana `INSERT INTO feedback DEFAULT VALUES` eemaldatakse |

## 1. Etapp — backend

| # | Teenus / töö | Taskifail | Keerukus | Märkus |
|---|---|---|---|---|
| 1.1 | `MyRegistrationDto`: `canGiveFeedback`, `hasFeedback` (`GET /api/user/{userId}/registrations`) | `docs/tasks/backend/registration-feedback-flags.md` | lihtne | ✅ kood tehtud; reegel nagu `canCancel`; `end_date <= täna` |
| 1.2 | `GET /api/user/{userId}/registration/{courseParticipantId}/feedback` | `docs/tasks/backend/GET-api-user-userId-registration-courseParticipantId-feedback.md` | keskmine | ✅ kood tehtud; tõlge `contentLang` / põhikeel; aktiivsed + vastatud kustutatud; `updatedAt` = vastuste `MAX(updated_at)` |
| 1.3 | `POST /api/user/{userId}/registration/{courseParticipantId}/feedback` | `docs/tasks/backend/POST-api-user-userId-registration-courseParticipantId-feedback.md` | keskmine | ✅ kood tehtud; kontrollid (jaotis 7 skeemides) jagatakse PUT-iga |
| 1.4 | `PUT /api/user/{userId}/registration/{courseParticipantId}/feedback` | `docs/tasks/backend/PUT-api-user-userId-registration-courseParticipantId-feedback.md` | keskmine | ✅ kood tehtud; uuendab + lisab vastused; staatus H → U |

Iga taski järel `./gradlew test`. Soovitus: 1.2 → 1.3 → 1.4 järjest (ühine kontrollimeetod `getValidFeedbackRegistrationBy(...)` ja kriteeriumide komplekti kontroll).

## 2. Etapp — frontend

| # | Töö | Taskifail | Märkus |
|---|---|---|---|
| 2.1 | `ParticipantRegistrationItem.vue`: nupp "Anna tagasisidet" / "Vaata tagasisidet" | `docs/tasks/frontend/participant-courses-feedback-button.md` | ✅ kood tehtud; pärast 1.1; nupp ka "Tulevased" plokis viimasel päeval |
| 2.2 | `ParticipantFeedbackFormView.vue` + `FeedbackCriteriaItem.vue` + router + `UserService.js` + i18n (et/en) | `docs/tasks/frontend/participant-feedback-form-view.md` | ✅ kood tehtud; pärast 1.2–1.4; `ProfileMenu` aktiivne "Minu koolitused" |

Frontendi järel `npm run lint` ja build scratchpadi koopias (vt juurkausta `CLAUDE.md`).

## Lahtised küsimused

- Profiili skeemides (`../profile-view/profile-view-skeemid.md`, jaotis 1) on ettepanek lisada Annale `course_participant` read 10 ja 11 (toimumiskorrad 10 ja 9). Seda pole seed'is tehtud; see plaan kasutab rida **10** toimumiskorrale 7. Kui profiili ettepanek hiljem tehakse, tuleb id-d kokku leppida.
- Profiili läbimängu (`profile-view-labimang.html`) "Minu koolitused" nuppe ei ole veel muudetud — prototüübi kestas viib `/participant-courses` vana läbimänguni; tagasiside nupud on näha selle kausta läbimängus.
- Mockupi pildid (Balsamiq) — käsud skeemide failis, jaotis 9.

## Seis (2026-10-01)

Kõik taskid on koodis tehtud, **commit'imata** (kasutaja vaatab enne üle).

- `./gradlew test` roheline (284 testi, uued: `FeedbackServiceTest`, `FeedbackRequestDtoValidationTest`, `CourseParticipantServiceTest.findMyRegistrations_setsFeedbackFlags`).
- SQL-skriptid jooksutati läbi PGlite'is (in-memory PostgreSQL); `CHECK` ja `UNIQUE` lükkasid vigased read tagasi.
- Backend käivitati PGlite vastu ja teenuseid prooviti `curl`-iga (kõik vastused ja veakoodid nagu taskides); frontend testiti brauseris (Playwright) `npm ci` koopias: nupud, uus vorm, valideerimine, salvestamine, lugemisrežiim, Muuda/Tühista/Salvesta, tooltip, keelatud olukord.
- `npm run lint` (oxlint + eslint) ja `vite build` läbisid scratchpadi koopias.
- Täpsustus: "Muudetud" kuvatakse, kui `updatedAt` **kuupäev** erineb `createdAt` kuupäevast (esimese salvestuse ajatemplid erinevad millisekundite võrra).
- **Kasutaja kontrollib:** DB skriptid Windowsi PostgreSQL-is (`1_reset` → `2_create` → `3_import`) ja rakendus IntelliJ-st.
