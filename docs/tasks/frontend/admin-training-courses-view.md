# Koolituse kalender (toimumiskordade tabel)

**Vaade:** `AdminTrainingCoursesView.vue`, route `/admin-training-courses?trainingId={id}` (nimi `adminTrainingCoursesRoute`)

**Roll:** Admin

**Vaste mockupis:** läbimäng `docs/mock-wireframe/loo-mock-vaade/admin-training-courses-view/admin-training-courses-view-labimang.html` (artifact https://claude.ai/artifact/1uyc8i6XoVHNjePmaGyzLX). Kõiki vaateid saab koos läbi mängida prototüübi kestas `docs/mock-wireframe/loo-mock-vaade/index.html` (artifact https://claude.ai/artifact/SqxcgWP1BoxUGZqFfSZCPY).

> Mockupi pilt lisatakse hiljem (Balsamiq AI käsk: `admin-training-courses-view-skeemid.md`, jaotis 9).

Taustaks: otsused ja skeemid `docs/mock-wireframe/loo-mock-vaade/admin-training-courses-view/admin-training-courses-view-skeemid.md`, märkmed `docs/mock-wireframe/markmed/admin-training-courses-view-markmed.md`.

## Kasutajavoog

Admin avab koolituste haldusest (rea ikoon "Kalender") või koolituse vormist (kiirnupp "Kalender") koolituse kalendri. Ülal on koolituse kaart (nimi, staatus, kategooria, õppekeel, toimumiskoht, **koolitajad nimedena**, rahastus, sätted, lingid "Vaata" / "Muuda"), vaikimisi peidetud kirjelduse kaart ja tabel toimumiskordadega. Nupuga "+ Lisa toimuv koolitus" avaneb toimumiskorra vorm.

## Kasutajaliidese elemendid

| Element | Tüüp | Kirjeldus/käitumine |
|---|---|---|
| "Koolituse kalender" + "Koolituste haldus" + "+ Lisa toimuv koolitus" | pealkiri, nupud | → `/admin-trainings`; → `/course-form?trainingId={id}` |
| Koolituse kaart (`TrainingSummaryCard.vue`) | kaart | andmed `GET /api/admin-training/{trainingId}`; koolitajad nimedena komadega (`lecturers`), puuduvad → "—"; "Vaata" → `/training?...`, "Muuda" → `/training-form?...` |
| "▾ Näita kirjeldust" / "▴ Peida kirjeldus" | link + kaart (`CollapsibleCard.vue`) | vaikimisi peidus; `RichTextContent` |
| "Näita ka möödunud" | lüliti | `includePast` → uus päring |
| Tabel | Bootstrap tabel | Algus \| Lõpp \| Päevi \| Akad. tunde \| Hind (€) \| Koolitajad \| Ruum \| Staatus \| Osalejaid \| Märkmed ✓/✗ \| Veebilink ✓/✗ \| Tegevused; kuupäevad `30/09/2026`, hind `490,00` |
| Staatuse märgis (`CourseStatusBadge.vue`) | märgis | Mustand / Avatud / Täis / Tühistatud; möödunud real lisaks "Toimunud", rida tuhmim |
| Sorteeritavad veerud | `SortableColumnHeader.vue` | Algus, Hind, Staatus, Osalejaid — **ainult frontendis**: 1. klõps kasvav, 2. kahanev, 3. vaikimisi järjestus; staatus U → O → F → X |
| "Muuda" / "Kustuta" | ikoonid | → `/course-form?courseId={id}`; `CourseDeleteButton.vue` (kinnitus; osalejate korral hoiatus "Kaalu pigem tühistamist") |
| Tühi tabel / "Kokku N toimumiskorda" | tekst | "Toimumiskordi pole veel lisatud" / "Tulevasi toimumiskordi pole" |

## Käitumine ja valideerimine

1. Avamisel: `GET /api/admin-training/{trainingId}?contentLang=` ja `GET /api/training/{trainingId}/courses?includePast=false`. Kustutatud / olematu koolitus (404) → üldine veavaade; mitte-admin → `NotAuthorizedView`.
2. Keele vahetusel laaditakse uuesti ainult koolituse andmed.
3. Lüliti ja kustutamine sorteerimist ei lähtesta.
4. Kustutamine: `DELETE /api/course/{courseId}` → tabel uuesti, eduteade "Toimumiskord kustutatud".
5. Vormilt tagasi tulles näidatakse vormi eduteadet ("Toimumiskord lisatud" / "salvestatud").

## API kutsed

| Kutse | Backend task |
|---|---|
| `GET /api/admin-training/{trainingId}` | `GET-api-admin-training-trainingId.md` |
| `GET /api/training/{trainingId}/courses` | `GET-api-training-trainingId-courses.md` |
| `DELETE /api/course/{courseId}` | `DELETE-api-course-courseId.md` |

**Veateated:** 404 → üldine veavaade; 500 → üldine veavaade.

## Komponendid ja failistruktuur

| Fail | Uus / muudetakse | Sisu |
|---|---|---|
| `views/AdminTrainingCoursesView.vue` | uus | olek `training`, `courses`, `includePast`, `isDescriptionOpen`, `sortBy`, `sortDirection`; `computed: sortedCourses` |
| `components/training/TrainingSummaryCard.vue` | uus | koolituse kaart (hiljem ka `TrainingView`) |
| `components/common/CollapsibleCard.vue`, `CourseStatusBadge.vue`, `CheckMark.vue`, `CourseDeleteButton.vue` | uus | vt skeemid, jaotis 7 |
| `views/AdminTrainingsView.vue` | muudetakse | rea tegevustesse ikoon **"Kalender"** (kustutatud real peidus) → `/admin-training-courses?trainingId={id}` |
| `views/TrainingFormView.vue` | muudetakse | kiirnupp **"Kalender"** (olekutes `update`, `new-translation`) |
| `api-services/CourseService.js`, `TrainingService.js` | uus / muudetakse | kutsed |
| `router/index.js`, `NavigationService.js` | muudetakse | `/admin-training-courses`, `navigateToAdminTrainingCoursesView(trainingId)` |
| `locales/*.json` | muudetakse | `adminTrainingCourses.*` |

Mock-vastused: kuni backend valmib, sama muster nagu `AdminTrainingsView` (seed: `course-db-changes.md`).

## Vastuvõtu kriteeriumid

- [ ] Kalender avaneb koolituste halduse ikoonist ja koolituse vormi kiirnupust
- [ ] Koolituse kaart (koolitajad nimedena), peidetud kirjeldus, "Vaata" / "Muuda"
- [ ] Tabel 12 veeruga, tulevased eespool, lüliti möödunud jaoks, "Toimunud" märgis
- [ ] Frontendi sorteerimine 4 veerus (3. klõps lähtestab), API kutset ei tehta
- [ ] Kustutamine kinnitusega, osalejate hoiatus; tühja tabeli tekstid
- [ ] 404 → veavaade; mitte-admin → NotAuthorizedView
