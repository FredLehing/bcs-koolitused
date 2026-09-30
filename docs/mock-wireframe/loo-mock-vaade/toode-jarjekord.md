# Koolitajad, koolituse kalender ja avalik koolitajate leht — tööde järjekord

Katusharu `feature/RAIN-courses-lecturers`. Kõik vaated koos: prototüübi kest `index.html` (artifact https://claude.ai/artifact/SqxcgWP1BoxUGZqFfSZCPY).

Järjekord: **andmebaas → koolitajate teenused → mitu koolitajat → kalendri teenused → avaliku lehe teenused → frontend (mockidega kohe alustatav) → AI tõlge**. Andmebaasi skripte (`1_reset` → `2_create` → `3_import`) kontrollib kasutaja iga DB-taski järel.

Mockid ja otsused: `admin-lecturers-view/`, `admin-training-courses-view/`, `lecturers-view/`, `form-view/` (koolituse vorm), `trainings-view/`.

---

## 1. Andmebaas ja koolitajad (backend)

| # | Töö | Taskifail | Keerukus | Märkus |
|---|---|---|---|---|
| 1.1 | Koolitaja DDL + seed (`status`, tõlkeväljad, `lecturer_photo`, view `admin_lecturer_summary`), entity'd | `docs/tasks/backend/lecturer-db-changes.md` | keskmine | kõik järgmised sõltuvad |
| 1.2 | Mitu koolitajat: `training_lecturer`, `course_lecturer`; `TrainingDto.lecturers`, `lecturerIds` | `docs/tasks/backend/training-lecturers-multiple.md` | keerukas | **muudab töötavat koodi** (TrainingService, DTO-d, testid) |
| 1.3 | Kustutatud koolitaja peitmine (`GET /api/lecturers`, `lecturerIds`) | `docs/tasks/backend/lecturer-deleted-status.md` | lihtne | |
| 1.4 | Pilditeenus + pildi normaliseerimise abiklass | `docs/tasks/backend/GET-api-lecturer-lecturerId-photo.md` | keskmine | vajalik 1.8, 1.9 ja avalikule lehele |
| 1.5 | `GET /api/admin-lecturers` | `docs/tasks/backend/GET-api-admin-lecturers.md` | keskmine | view entity |
| 1.6 | `DELETE /api/lecturer/{id}`, `PUT …/restore` | `DELETE-api-lecturer-lecturerId.md`, `PUT-api-lecturer-lecturerId-restore.md` | lihtne | |
| 1.7 | Vormi lugemine: `GET /api/lecturer/{id}`, `…/lecturer-translations`, `GET /api/lecturer-translation/{id}` | `GET-api-lecturer-lecturerId.md`, `GET-api-lecturer-lecturerId-lecturer-translations.md`, `GET-api-lecturer-translation-lecturerTranslationId.md` | lihtne | |
| 1.8 | `POST /api/lecturer` | `POST-api-lecturer.md` | keskmine | 3 tabelit, pilt |
| 1.9 | `PUT /api/lecturer/{id}` | `PUT-api-lecturer-lecturerId.md` | keskmine | `photo` / `isPhotoRemoved` |
| 1.10 | `POST /api/lecturer/{id}/lecturer-translation` | `POST-api-lecturer-lecturerId-lecturer-translation.md` | lihtne | |
| 1.11 | `GET /api/lecturer-summary/{id}` | `GET-api-lecturer-summary-lecturerId.md` | lihtne | koolituse lehe `LecturerCard` |

## 2. Koolituse kalender (backend)

| # | Töö | Taskifail | Keerukus | Märkus |
|---|---|---|---|---|
| 2.1 | `CourseStatus`, view `course_summary`, seed | `docs/tasks/backend/course-db-changes.md` | keskmine | pärast 1.2 |
| 2.2 | `GET /api/admin-training/{trainingId}` | `GET-api-admin-training-trainingId.md` | keskmine | |
| 2.3 | `GET /api/training/{trainingId}/courses` | `GET-api-training-trainingId-courses.md` | keskmine | view entity |
| 2.4 | `GET /api/rooms` | `GET-api-rooms.md` | lihtne | |
| 2.5 | `GET /api/course/{id}`, `POST …/course`, `PUT /api/course/{id}`, `DELETE /api/course/{id}` | `GET-api-course-courseId.md`, `POST-api-training-trainingId-course.md`, `PUT-api-course-courseId.md`, `DELETE-api-course-courseId.md` | keskmine | `COURSE_END_BEFORE_START` |

## 3. Avalik koolitajate leht (backend)

| # | Töö | Taskifail | Keerukus |
|---|---|---|---|
| 3.1 | `GET /api/lecturer-summaries` | `GET-api-lecturer-summaries.md` | lihtne |
| 3.2 | `GET /api/lecturer-profile/{id}` | `GET-api-lecturer-profile-lecturerId.md` | keskmine |

## 4. Frontend (mockidega kohe alustatav)

| # | Töö | Taskifail | Märkus |
|---|---|---|---|
| 4.1 | Termin "koolitaja" | `docs/tasks/frontend/lecturer-terminology.md` | väike, sõltumatu |
| 4.2 | Koolituse vormi mitu koolitajat + `LecturersPicker` | `docs/tasks/frontend/training-form-lecturers.md` | pärast 1.2; `LecturersPicker` kasutab ka 4.6 |
| 4.3 | `LecturerAvatar` + `LecturerCard` + `/training` "Koolitajad" | `docs/tasks/frontend/lecturer-card.md` | |
| 4.4 | Koolitajate haldus | `docs/tasks/frontend/admin-lecturers-view.md` | |
| 4.5 | Koolitaja vorm | `docs/tasks/frontend/lecturer-form-view.md` | `TranslationFlags` üldistamine |
| 4.6 | Koolituse kalender (+ AdminTrainingsView ikoon, TrainingFormView kiirnupp) | `docs/tasks/frontend/admin-training-courses-view.md` | |
| 4.7 | Toimumiskorra vorm | `docs/tasks/frontend/course-form-view.md` | |
| 4.8 | Meie koolitajad (+ navbar) | `docs/tasks/frontend/lecturers-view.md` | |
| 4.9 | Koolitaja detailvaade | `docs/tasks/frontend/lecturer-view.md` | |

## 5. Hiljem

| # | Töö | Taskifail | Märkus |
|---|---|---|---|
| 5.1 | Koolitaja AI tõlge | `docs/tasks/backend/GET-api-lecturer-lecturerId-ai-translation.md` | pärast koolituse AI tõlget; seni frontendi mock |
| 5.2 | `TrainingsView` filtrid | `docs/tasks/frontend/trainings-view.md` | läbimäng `trainings-view/` näitab plaanitud filtreid |

---

## Lahtised küsimused

- `room.status` (`VAB` / `KIN`) tähendus — praegu kuvatakse kõik ruumid.
- `/training` läbimäng (koolituse leht koos koolitajate kaartidega) puudub; kestas on kohatäide.
- Mockupi pildid (Balsamiq) — käsud skeemide failides.
