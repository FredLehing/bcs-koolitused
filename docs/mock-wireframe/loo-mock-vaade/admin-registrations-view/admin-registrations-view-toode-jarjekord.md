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

## Hiljem

- Osaleja (inimese) vaade kõigi tema registreerumistega, sh kontaktandmete muutmine.
- Osaleja lisamine admini poolt; e-kirja teavitus staatuse muutusel; mahutavus.
