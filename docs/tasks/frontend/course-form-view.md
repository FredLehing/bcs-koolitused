# Toimumiskorra vorm (uus ja muutmine)

**Vaade:** `CourseFormView.vue`, route `/course-form?trainingId={id}` (uus) või `/course-form?courseId={id}` (muutmine), nimi `courseFormRoute`

**Roll:** Admin

**Vaste mockupis:** sama läbimäng mis kalendril (`docs/mock-wireframe/loo-mock-vaade/admin-training-courses-view/admin-training-courses-view-labimang.html`, nupp "+ Lisa toimuv koolitus" või pliiats). Kõiki vaateid saab koos läbi mängida prototüübi kestas `docs/mock-wireframe/loo-mock-vaade/index.html` (artifact https://claude.ai/artifact/SqxcgWP1BoxUGZqFfSZCPY).

> Mockupi pilt lisatakse hiljem (Balsamiq AI käsk: `admin-training-courses-view-skeemid.md`, jaotis 9).

Taustaks: märkmed `docs/mock-wireframe/markmed/course-form-view-markmed.md`, otsused `admin-training-courses-view-skeemid.md`.

## Kasutajavoog

Admin avab kalendrist uue toimumiskorra vormi (eeltäidetud koolituse koolitajatega, staatus "Mustand") või olemasoleva muutmise. Täidab kuupäevad (päevade arv pakutakse tööpäevadest), tunnid, hinna, koolitajad, ruumi, veebilingi, märkmed ja staatuse ning salvestab; vorm suunab tagasi kalendrisse.

## Kasutajaliidese elemendid

| Element | Tüüp | Kirjeldus/käitumine |
|---|---|---|
| Pealkiri + koolituse nimi, kiirnupp "Kalender" | | "Uus toimumiskord" / "Toimumiskorra muutmine"; → `/admin-training-courses?trainingId={id}` |
| Algus *, Lõpp * | `<input type="date">` | |
| Päevi * | number ≥ 1 | kui admin pole muutnud, täidetakse tööpäevade (E–R) arvuga; vihje "Arvutatud tööpäevadest, saad muuta"; muutmisel käsitsi |
| Akadeemilisi tunde *, Hind (€) * | number | ≥ 1; ≥ 0 |
| Staatus * | select | Mustand / Avatud / Täis / Tühistatud (`D` pole valikus) |
| **Koolitajad** | nimekiri | valitud koolitajad **nimedena**; × eemaldab, ↑ ↓ muudab järjekorda; "+ Lisa koolitaja" → `LecturerSelectModal.vue` (juba valitud ei pakuta); võib jääda tühjaks |
| Ruum | `RoomsDropdown.vue` | esimene valik "Ruum puudub" |
| Veebilink, Märkmed | input, textarea | |
| "Salvesta", "Tagasi", prügikast (muutmisel) | nupud | `CourseDeleteButton.vue` |
| Vead | `AlertDanger.vue` | "Täida kõik kohustuslikud väljad", "Lõppkuupäev ei saa olla varasem kui alguskuupäev", backendi `message` |

## Käitumine ja valideerimine

1. Uus: `GET /api/admin-training/{trainingId}` (nimi, `lecturers` → eeltäidetud koolitajad), `GET /api/rooms`. Muutmine: `GET /api/course/{courseId}` → `trainingId` → `GET /api/admin-training/{trainingId}`, `GET /api/rooms`.
2. Kontroll enne saatmist: kohustuslikud väljad, `endDate ≥ startDate`.
3. "Salvesta": uus → `POST /api/training/{trainingId}/course` (`userId` localStorage'ist, `lecturerIds` järjekorras); muutmine → `PUT /api/course/{courseId}`. Õnnestumisel → kalendrisse eduteatega.
4. Kustutamine (muutmisel) → kinnitus → `DELETE` → kalendrisse.
5. 403 `COURSE_END_BEFORE_START` → `message`; 404 / 500 → üldine veavaade.

## API kutsed

| Kutse | Backend task |
|---|---|
| `GET /api/admin-training/{trainingId}` | `GET-api-admin-training-trainingId.md` |
| `GET /api/course/{courseId}` | `GET-api-course-courseId.md` |
| `GET /api/rooms` | `GET-api-rooms.md` |
| `GET /api/lecturers?search=` | `GET-api-lecturers.md` (olemas; ainult aktiivsed — `lecturer-deleted-status.md`) |
| `POST /api/training/{trainingId}/course` | `POST-api-training-trainingId-course.md` |
| `PUT /api/course/{courseId}` | `PUT-api-course-courseId.md` |
| `DELETE /api/course/{courseId}` | `DELETE-api-course-courseId.md` |

## Komponendid ja failistruktuur

| Fail | Uus / muudetakse | Sisu |
|---|---|---|
| `views/CourseFormView.vue` | uus | olekud `new` / `update` |
| `components/forms/LecturersPicker.vue` | uus, jagatud | valitud koolitajate nimekiri (× ↑ ↓) + "+ Lisa koolitaja" + `LecturerSelectModal`; `v-model` = `lecturerIds` (kasutab ka `TrainingFormView`, vt `training-form-lecturers.md`) |
| `components/forms/RoomsDropdown.vue` | uus | |
| `components/common/CourseDeleteButton.vue` | uus (kalendri taskist) | |
| `api-services/CourseService.js`, `RoomService.js` | uus | |
| `router/index.js`, `NavigationService.js`, `locales/*.json` | muudetakse | |

## Vastuvõtu kriteeriumid

- [ ] Kaks olekut URL-ist; uus on eeltäidetud koolituse koolitajatega ja staatusega "Mustand"
- [ ] Päevade arvu pakkumine tööpäevadest, käsitsi muutmise järel enam ei muutu
- [ ] Koolitajate nimekiri (lisa, eemalda, järjekord), ainult nimed
- [ ] Valideerimine ja veateated; salvestamine → kalender eduteatega
- [ ] Kustutamine muutmise olekus
