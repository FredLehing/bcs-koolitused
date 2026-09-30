# Koolitajate haldus — tööde järjekord

`AdminLecturersView.vue`, `LecturerFormView.vue`, `LecturerCard.vue`.

Järjekord: **andmebaas ja entity'd → nimekirja teenused → vormi teenused → koolitaja kaart → frontend (mockidega kohe alustatav) → AI tõlge**.

Frontendi saab alustada kohe mock-vastustega (vt frontend taskide jaotist "Mock-vastused") ja ühendada päris teenustega jooksvalt, kui backend valmib.

Allikad: skeemid `admin-lecturers-view-skeemid.md`, märkmed `docs/mock-wireframe/markmed/admin-lecturers-view-markmed.md` ja `lecturer-form-view-state-*-markmed.md`, läbimäng `admin-lecturers-view-labimang.html`.

Koolituse kalender (`admin-training-courses-view`) sõltub sellest tööst: `lecturer_photo`, `LecturerCard` ja `GET /api/lecturer-summary/{lecturerId}` peavad olema enne kalendrit valmis.

---

## 0. Eeltöö

| # | Töö | Taskifail | Märkus |
|---|---|---|---|
| 0.1 | DDL + seed: `lecturer.status`, `lecturer.photo` eemaldamine, `lecturer_translation` väljad, `lecturer_photo`, view `admin_lecturer_summary`; entity'd, `LecturerStatus`, `LecturerDto` ilma pildita | `docs/tasks/backend/lecturer-db-changes.md` | **kõik järgmised sõltuvad sellest**; andmebaasi kontrollib kasutaja (`1_reset` → `2_create` → `3_import`) |

---

## 1. Etapp — backend

| # | Teenus / töö | Taskifail | Keerukus | Märkus |
|---|---|---|---|---|
| 1.1 | Kustutatud koolitaja peitmine: `GET /api/lecturers` ainult aktiivsed, `getValidActiveLecturerBy`, koolituse `defaultLecturerId` | `docs/tasks/backend/lecturer-deleted-status.md` | lihtne | teised teenused kasutavad `getValidActiveLecturerBy`-d |
| 1.2 | `GET /api/admin-lecturers` | `docs/tasks/backend/GET-api-admin-lecturers.md` | keskmine | view entity (eeskuju `AdminTrainingSummary`) |
| 1.3 | `DELETE /api/lecturer/{lecturerId}` | `docs/tasks/backend/DELETE-api-lecturer-lecturerId.md` | lihtne | `403 LECTURER_HAS_UPCOMING_COURSES` |
| 1.4 | `PUT /api/lecturer/{lecturerId}/restore` | `docs/tasks/backend/PUT-api-lecturer-lecturerId-restore.md` | lihtne | sobib teha koos 1.3-ga |
| 1.5 | `GET /api/lecturer/{lecturerId}` | `docs/tasks/backend/GET-api-lecturer-lecturerId.md` | lihtne | Base64 pilt |
| 1.6 | `GET /api/lecturer/{lecturerId}/lecturer-translations` | `docs/tasks/backend/GET-api-lecturer-lecturerId-lecturer-translations.md` | lihtne | |
| 1.7 | `GET /api/lecturer-translation/{lecturerTranslationId}` | `docs/tasks/backend/GET-api-lecturer-translation-lecturerTranslationId.md` | lihtne | |
| 1.8 | `POST /api/lecturer` | `docs/tasks/backend/POST-api-lecturer.md` | keskmine | 3 tabelit ühes transaktsioonis, pildi kontroll (ühine meetod) |
| 1.9 | `PUT /api/lecturer/{lecturerId}` | `docs/tasks/backend/PUT-api-lecturer-lecturerId.md` | keskmine | pildi lisamine / asendamine / eemaldamine |
| 1.10 | `POST /api/lecturer/{lecturerId}/lecturer-translation` | `docs/tasks/backend/POST-api-lecturer-lecturerId-lecturer-translation.md` | lihtne | `TRANSLATION_EXISTS` on olemas |
| 1.11 | `GET /api/lecturer-summary/{lecturerId}` | `docs/tasks/backend/GET-api-lecturer-summary-lecturerId.md` | lihtne | vajalik ka kalendrile |
| 1.12 | `GET /api/lecturer/{lecturerId}/ai-translation` | `docs/tasks/backend/GET-api-lecturer-lecturerId-ai-translation.md` | keerukas | **pärast koolituse AI tõlget** (`GET-api-training-trainingId-ai-translation.md`), sama lahendus |

1.5–1.7 ja 1.10 on väikesed lugemis- / lisamisteenused; 1.8–1.9 on vormi põhitöö. 1.12 võib jääda hilisemaks — frontend kasutab seni mocki.

**Etapi tulemus:** kõik teenused (peale AI) on Swaggerist testitavad.

---

## 2. Etapp — frontend

| # | Töö | Taskifail | Märkus |
|---|---|---|---|
| 2.1 | Termin "lektor" → "koolitaja" (i18n) | `docs/tasks/frontend/lecturer-terminology.md` | väike, sõltumatu; võib teha kohe |
| 2.2 | `LecturerCard.vue` + `LecturerAvatar.vue` + `/training` parem veerg | `docs/tasks/frontend/lecturer-card.md` | väike, jagatud; kalender kasutab hiljem |
| 2.3 | `AdminLecturersView.vue` + kustutamise/taastamise nupud + navbar + router | `docs/tasks/frontend/admin-lecturers-view.md` | |
| 2.4 | `LecturerFormView.vue` + `PhotoUpload.vue` + `TranslationFlags` üldistamine | `docs/tasks/frontend/lecturer-form-view.md` | põhitöö; `TrainingFormView` käitumine ei tohi muutuda |

2.2 enne 2.4-t (`LecturerAvatar`). Kõiki saab teha mockidega enne 1. etapi lõppu.

**Etapi tulemus:** admin haldab koolitajaid; koolituse lehel on koolitaja kaart.

---

## Lahtised küsimused

- Avalik koolitajate leht (navbari "Koolitajad" menüüs "Ettevõttest", praegu `href="#"`) — kasutaks `description` välja; eraldi mock hiljem.
- Mockupi pilt (Balsamiq) — lisatakse taskidesse, kui vaated on Balsamiqis olemas (käsud: `admin-lecturers-view-skeemid.md`, jaotis 10).
