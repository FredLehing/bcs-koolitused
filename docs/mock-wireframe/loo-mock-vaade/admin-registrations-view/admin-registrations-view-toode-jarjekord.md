# Registreerumiste haldus — tööde järjekord

`AdminRegistrationsView.vue`, `AdminRegistrationView.vue`. Katusharu `feature/RAIN-registrations` (võetud `feature/RAIN-courses`-ist).

Allikad: skeemid `admin-registrations-view-skeemid.md`, märkmed `docs/mock-wireframe/markmed/admin-registrations-view-markmed.md` ja `admin-registration-view-markmed.md`, läbimäng `admin-registrations-view-labimang.html` (prototüübi kestas `../index.html`).

Andmebaasi skripte (`1_reset` → `2_create` → `3_import`) kontrollib kasutaja pärast taski 0.1.

| # | Töö | Taskifail | Keerukus | Märkus |
|---|---|---|---|---|
| 0.1 | `admin_notes`, view `admin_registration_summary`, seed, entity | `docs/tasks/backend/registration-db-changes.md` | lihtne | kõik järgmised sõltuvad |
| 1.1 | `GET /api/admin-registrations` | `docs/tasks/backend/GET-api-admin-registrations.md` | lihtne | |
| 1.2 | `GET /api/admin-registration/{courseParticipantId}` | `docs/tasks/backend/GET-api-admin-registration-courseParticipantId.md` | lihtne | |
| 1.3 | `PUT /api/admin-registration/{courseParticipantId}` | `docs/tasks/backend/PUT-api-admin-registration-courseParticipantId.md` | keskmine | valideerimine |
| 2.1 | Mõlemad vaated + navbar + router + `/admin-course` silm | `docs/tasks/frontend/admin-registrations-view.md` | keskmine | |

## Seis ja üleanne (2026-10-01)

**Tehtud** (katusharus `feature/RAIN-registrations`, iga task oma harust `--no-ff`): 0.1, 1.1, 1.2, 1.3. Backendi testid läbivad (225). Andmebaasi skripte pole veel käivitatud — kasutaja kontrollib `1_reset` → `2_create` → `3_import` ja käivitab backendi (`ddl-auto=none`, view entity vigu näeb alles päringuga).

**Järgmine: 2.1 frontend** (uus sessioon, haru `RAIN-admin-registrations-view` katusharust). Lähtu taskist `docs/tasks/frontend/admin-registrations-view.md`; eeskujud `AdminEnquiriesView.vue` / `AdminEnquiryView.vue` (nimekiri + üks kirje, `EnquiryStatusBadge`), `LecturerView.vue` (`returnTo` + `NavigationService.isInternalPath`), `CourseParticipantsTable.vue` (silm + staatuse märgis). Backendi lepingud:

- `GET /api/admin-registrations?contentLang=&includeCancelled=&includePast=` → `AdminRegistrationSummaryDto[]` (`courseParticipantId, registeredAt, participantName, email, courseId, trainingTitle, courseStartDate, courseEndDate, isPast, hasPaid, requiresLaptop, status`).
- `GET /api/admin-registration/{courseParticipantId}?contentLang=` → `AdminRegistrationDto` (lisaks `notes, adminNotes, createdAt, updatedAt, phone, accountEmail, courseStatus`).
- `PUT /api/admin-registration/{courseParticipantId}` body `{ status, hasPaid, requiresLaptop, adminNotes }` → 200 tühi; 400 `INCORRECT_INPUT`, 404 `PRIMARY_KEY_NOT_FOUND`.
- `returnTo` `/admin-course` silmalt: `encodeURIComponent('/admin-course?courseId=' + courseId)` (router `query` teeb seda ise).

Buildi kontroll scratchpadis (juur-`CLAUDE.md`), `npm run lint` otse. Pärast 2.1: katusharu `--no-ff` master'isse alles siis, kui `feature/RAIN-courses` on master'is (katusharu on võetud sellest).

## Hiljem

- Osaleja (inimese) vaade kõigi tema registreerumistega, sh kontaktandmete muutmine.
- Osaleja lisamine admini poolt; e-kirja teavitus staatuse muutusel; mahutavus.
