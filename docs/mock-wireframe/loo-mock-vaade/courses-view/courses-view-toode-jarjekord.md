# Toimumiskordade vaated ja registreerumine — tööde järjekord

Katusharu `feature/RAIN-courses`. Kõik vaated koos: prototüübi kest `../index.html` (artifact https://claude.ai/artifact/SqxcgWP1BoxUGZqFfSZCPY).

Allikad: skeemid `courses-view-skeemid.md`, märkmed `docs/mock-wireframe/markmed/` (`admin-all-courses-view-`, `admin-course-view-`, `courses-view-`, `course-view-`, `course-registration-view-`, `signup-view-markmed.md`), läbimängud `admin-all-courses-view-labimang.html`, `courses-view-labimang.html`, `course-registration-view-labimang.html`.

Järjekord: **andmebaas → admini teenused → avalikud teenused → registreerumise teenused → frontend**. Andmebaasi skripte (`1_reset` → `2_create` → `3_import`) kontrollib kasutaja pärast taski 0.1. Frontendi saab alustada mockidega kohe pärast 0.1.

Tehtud (enne taske): osalemisvormi eemaldamine (`option`, `option_translation`, `enquiry.option_id`, "Vorm" admini päringute vaadetes).

## 0. Andmebaas

| # | Töö | Taskifail | Keerukus | Märkus |
|---|---|---|---|---|
| 0.1 | `is_promoted`, `R`/`C`, piirangud, view'd `admin_course_summary` ja `public_course_summary`, seed, entity seosed | `docs/tasks/backend/courses-calendar-db-changes.md` | keskmine | kõik järgmised sõltuvad |

## 1. Admin (backend)

| # | Töö | Taskifail | Keerukus |
|---|---|---|---|
| 1.1 | `isPromoted` toimumiskorra teenustes | `docs/tasks/backend/course-is-promoted.md` | lihtne |
| 1.2 | `GET /api/admin-courses` | `docs/tasks/backend/GET-api-admin-courses.md` | keerukas (filtrid, sorteerimine, leheküljestus) |
| 1.3 | `GET /api/admin-course/{courseId}` | `docs/tasks/backend/GET-api-admin-course-courseId.md` | keskmine |
| 1.4 | `GET /api/course/{courseId}/participants` | `docs/tasks/backend/GET-api-course-courseId-participants.md` | lihtne |
| 1.5 | `GET /api/course/{courseId}/enquiries` | `docs/tasks/backend/GET-api-course-courseId-enquiries.md` | lihtne |

## 2. Avalik (backend)

| # | Töö | Taskifail | Keerukus |
|---|---|---|---|
| 2.1 | `GET /api/courses` | `docs/tasks/backend/GET-api-courses.md` | keerukas (filtrid, rahastus) |
| 2.2 | `GET /api/course-summary/{courseId}` | `docs/tasks/backend/GET-api-course-summary-courseId.md` | keskmine |
| 2.3 | `POST /api/enquiry` | `docs/tasks/backend/POST-api-enquiry.md` | keskmine |

## 3. Registreerumine (backend)

| # | Töö | Taskifail | Keerukus | Märkus |
|---|---|---|---|---|
| 3.1 | `POST /api/user` (konto loomine) | `docs/tasks/backend/POST-api-user.md` | keskmine | 3 tabelit, `EMAIL_TAKEN` |
| 3.2 | `GET /api/user/{userId}/participant` | `docs/tasks/backend/GET-api-user-userId-participant.md` | lihtne | |
| 3.3 | `GET /api/course/{courseId}/participant-status` | `docs/tasks/backend/GET-api-course-courseId-participant-status.md` | lihtne | |
| 3.4 | `POST /api/course/{courseId}/participant` | `docs/tasks/backend/POST-api-course-courseId-participant.md` | keerukas | transaktsioon, 3 uut `Error`-it |

## 4. Frontend

| # | Töö | Taskifail | Märkus |
|---|---|---|---|
| 4.1 | Toimumiskorra vormi lüliti "Esile tõstetud" | `docs/tasks/frontend/course-form-is-promoted.md` | pärast 1.1 |
| 4.2 | Koolituste kalender (admin) + menüülink | `docs/tasks/frontend/admin-all-courses-view.md` | |
| 4.3 | Toimumiskorra ülevaade (admin) | `docs/tasks/frontend/admin-course-view.md` | |
| 4.4 | Avalik kalender + "Koolitused" rippmenüü | `docs/tasks/frontend/courses-view.md` | |
| 4.5 | Avalik toimumiskorra leht + `EnquiryModal` | `docs/tasks/frontend/course-view.md` | "Registreeru" suunab 4.6/4.7 vaadetesse |
| 4.6 | Konto loomine + login `redirect` + navbar "Loo konto" | `docs/tasks/frontend/signup-view.md` | enne 4.7 |
| 4.7 | Registreerumise vaade | `docs/tasks/frontend/course-registration-view.md` | |

## Hiljem

- Registreerumise tühistamine kasutaja poolt, kolleegi registreerimine, "Minu koolitused", e-kirja kinnitused.
- Osalejate haldus `/admin-course` vaates (tasumine, loobumine, lisamine); mahutavus.
- `EnquiryModal` `/training` lehel (üldine päring); paroolide räsimine.
