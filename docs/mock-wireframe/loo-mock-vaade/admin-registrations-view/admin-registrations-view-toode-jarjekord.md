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

**Tehtud** (katusharus `feature/RAIN-registrations`, iga task oma harust `--no-ff`): 0.1, 1.1, 1.2, 1.3, 2.1. Backendi testid läbivad (225), frontendi lint ja build läbivad. Andmebaasi skripte pole veel käivitatud — kasutaja kontrollib `1_reset` → `2_create` → `3_import` ja käivitab backendi (`ddl-auto=none`, view entity vigu näeb alles päringuga).

**2.1 märkused:** `CourseParticipantsTable` sai propi `courseId` (silma `returnTo` jaoks). `NavigationService`-it ei muudetud — vaated kasutavad `RouterLink`-e, uut navigeerimismeetodit polnud vaja. Registreerumise vaates on "Registreerus" väli `createdAt` (DTO-s `registeredAt` puudub). Salvestamise mis tahes viga → `AlertDanger` (backendi teade või "Salvestamine ebaõnnestus").

**Järgmine:** kasutaja testib brauseris pärast andmebaasi skriptide käivitamist. Seejärel katusharu `--no-ff` master'isse alles siis, kui `feature/RAIN-courses` on master'is (katusharu on võetud sellest).

## Hiljem

- Osaleja (inimese) vaade kõigi tema registreerumistega, sh kontaktandmete muutmine.
- Osaleja lisamine admini poolt; e-kirja teavitus staatuse muutusel; mahutavus.
