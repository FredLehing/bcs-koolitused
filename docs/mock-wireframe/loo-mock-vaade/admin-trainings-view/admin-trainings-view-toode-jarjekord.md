# AdminTrainingsView.vue — tööde järjekord

Järjekord: **andmebaas ja soft delete → tabeli teenused → frontend (mockidega kohe alustatav) → olemasolevate teenuste reeglid**.

Frontendi saab alustada kohe mock-vastustega (vt `docs/tasks/frontend/admin-trainings-view.md`, "Mock-vastused") ja ühendada päris teenustega jooksvalt, kui backend valmib.

Allikad: märkmed `docs/mock-wireframe/markmed/admin-trainings-view-markmed.md`, skeemid `admin-trainings-view-skeemid.md`, läbimäng `admin-trainings-view-labimang.html`.

---

## 0. Eeltöö

| # | Töö | Märkus |
|---|---|---|
| 0.1 | View `admin_training_summary` `2_create.sql`-i ja andmebaas uuesti (`1_reset` → `2_create` → `3_import`) | SQL on `GET-api-admin-trainings.md` taskis; **pole veel andmebaasis käivitatud** — kontrolli esimesena |

---

## 1. Etapp — backend

| # | Teenus / töö | Taskifail | Keerukus | Märkus |
|---|---|---|---|---|
| 1.1 | `DELETE /api/training/{trainingId}` | `docs/tasks/backend/DELETE-api-training-trainingId.md` | lihtne | lisab `TrainingStatus.DELETED("D")` — vajalik kõigile järgmistele |
| 1.2 | `PUT /api/training/{trainingId}/restore` | `docs/tasks/backend/PUT-api-training-trainingId-restore.md` | lihtne | tegevusteenus, sobib teha koos 1.1-ga |
| 1.3 | `PUT /api/training/{trainingId}/publish` ja `/unpublish` | `PUT-api-training-trainingId-publish.md`, `PUT-api-training-trainingId-unpublish.md` (täiendatud) | lihtne | pole veel implementeeritud; nüüd lisaks `403 TRAINING_DELETED` |
| 1.4 | `GET /api/training-titles` | `docs/tasks/backend/GET-api-training-titles.md` | lihtne | vajab view'd (0.1) |
| 1.5 | `GET /api/admin-trainings` | `docs/tasks/backend/GET-api-admin-trainings.md` | keerukas | view entity, Specificationid (7 filtrit), 7 sorteerimisvõimalust, leheküljestus, `fundingTypes` |
| 1.6 | Kustutatud ja mustandis koolituste peitmine | `docs/tasks/backend/training-deleted-status.md` | keskmine | muudab 6 olemasolevat teenust: avalik `GET /api/trainings` ainult `P`; kustutatud = 404 |

1.1–1.4 on väikesed ja sõltumatud (peale `TrainingStatus.DELETED`), 1.5 on selle vaate põhitöö, 1.6 puudutab olemasolevat koodi ja teisi vaateid.

**Etapi tulemus:** kõik vaate teenused on Swaggerist testitavad.

---

## 2. Etapp — frontend

| # | Töö | Taskifail | Märkus |
|---|---|---|---|
| 2.1 | `TrainingStatusButton.vue` + `TrainingFormView` üleviimine | `docs/tasks/frontend/training-status-button.md` | väike, jagatud; TrainingFormView käitumine ei tohi muutuda |
| 2.2 | `AdminTrainingsView.vue` + `PaginationNav.vue` + `TrainingDeleteButton.vue` + navbar + router | `docs/tasks/frontend/admin-trainings-view.md` | põhitöö; `PaginationNav` võetakse kasutusele ka `TrainingsView`-s |

2.1 enne 2.2-te, sest tabel kasutab staatuse nuppu. Mõlemat saab teha mockidega enne 1. etapi lõppu.

**Etapi tulemus:** admin haldab koolitusi tabelis.

---

## Lahtised küsimused

- Kustutamise keeld tulevaste toimumiskordade korral — lisatakse, kui `course` teenused valmivad (`DELETE-api-training-trainingId.md`, "Avatud küsimused").
- Mockupi pilt (Balsamiq) — lisatakse taskidesse, kui vaade on Balsamiqis olemas (käsk: `admin-trainings-view-skeemid.md`, jaotis 9).
