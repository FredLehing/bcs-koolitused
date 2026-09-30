# Kõigi koolituste toimumiskorrad (Koolituste kalender, admin)

**Vaated:** `AdminAllCoursesView.vue` (`/admin-all-courses`, `adminAllCoursesRoute`)

**Roll:** Admin

**Vaste mockupis:** interaktiivne läbimäng `docs/mock-wireframe/loo-mock-vaade/courses-view/admin-all-courses-view-labimang.html` (prototüübi kest https://claude.ai/artifact/SqxcgWP1BoxUGZqFfSZCPY)

> Mockupi pilt lisatakse hiljem (Balsamiq AI käsk: `docs/mock-wireframe/loo-mock-vaade/courses-view/courses-view-skeemid.md`, jaotis 8 "AdminAllCoursesView").

Taustaks: märkmed `docs/mock-wireframe/markmed/admin-all-courses-view-markmed.md`, skeemid `docs/mock-wireframe/loo-mock-vaade/courses-view/courses-view-skeemid.md`. Eeskuju: `AdminTrainingsView.vue` (otsing, `AdminTrainingFilters`, backendi sorteerimine, `PaginationNav`) ja `AdminTrainingCoursesView.vue` (toimumiskorra read).

Eeldab backend taski `GET-api-admin-courses.md` (kuni selle valmimiseni mock `api-services/mock`).

## Kasutajavoog

Admin avab "Admin" → "Koolituste kalender", näeb tulevasi toimumiskordi (lähim üleval), otsib koolituse nime järgi, avab filtrid, sorteerib, lülitab sisse möödunud, avab toimumiskorra (silm), muudab (pliiats), avab koolituse kalendri (kalendri ikoon või koolituse nimi) või kustutab.

## Kasutajaliidese elemendid

| Element | Tüüp | Kirjeldus/käitumine |
|---|---|---|
| Menüülink "Koolituste kalender" | navbar, menüü "Admin" | "Koolituste haldus" järel (i18n `navbar.manageCourses`, en "Course calendar") |
| Pealkiri "Koolituste kalender" | h1 | lisamise nuppu pole |
| Otsinguväli + "Otsi" | input + `<datalist>` | koolituse nimest; ettepanekud `GET /api/training-titles`; Enter = Otsi |
| "▾ Ava otsingu filtrid" + "N filtrit aktiivne" + "Tühjenda filtrid" | link, märgis | nagu `/admin-trainings` |
| Filtrikaart | `AdminCourseFilters.vue` | Periood alates–kuni (`DateInput`), Kategooria, Koolituse keel, Staatus (Kõik/Mustand/Avatud/Täis/Tühistatud), Toimumisviis (Kõik/Kohapeal/Veebis), Esile tõstetud (Kõik/Jah/Ei); "Filtreeri" / "Tühjenda filtrid" (mustand ↔ rakendatud) |
| "Näita ka möödunud" | lüliti | kohe uus päring `includePast` |
| Tabel | Bootstrap tabel | Algus \| Päevi \| Koolitus \| Hind \| Staatus \| Osalejad \| Tasunud \| Veebilink \| Huvilisi \| Tegevused |
| Koolitus | link | → `/admin-training-courses?trainingId=`; esile tõstetud ees ☆ |
| Staatus | `CourseStatusBadge` | + hall "Toimunud" möödunul; möödunud rida tuhmim |
| Tasunud | tekst | `paidCount / participantCount`; kõik tasunud → roheline; 0 → "—" |
| Veebilink | `CheckMark` | ✓/✗ |
| Sorteeritavad veerud | `SortableColumnHeader` | Algus, Koolitus, Hind, Staatus, Osalejad, Huvilisi (backendis) |
| Tegevused | ikoonid | `PhEye` → `/admin-course?courseId=`, `PhPencilSimple` → `/course-form?courseId=`, `PhCalendarBlank` → `/admin-training-courses?trainingId=`, `CourseDeleteButton` |
| "Kokku N toimumiskorda" + `PaginationNav` | | limit 10 |

## Käitumine

- Ainult adminile, muidu `NotAuthorizedView`.
- Iga otsing, filter, sorteerimine ja lüliti → `page = 0`; kustutamise järel uuesti laadimine (tühjaks jäänud lehelt eelmisele).
- Keele vahetusel uuesti tabel, pealkirjade ettepanekud ja kategooriad; filtrid, sorteerimine ja leht jäävad.
- Tühi tulemus: "Toimumiskordi ei leitud".

## API kutsed

- `GET /api/admin-courses?...` (uus), `GET /api/training-titles`, `GET /api/categories`, `GET /api/languages`, `DELETE /api/course/{courseId}` (olemas)

## Komponendid ja failistruktuur

- `views/AdminAllCoursesView.vue`, `components/forms/AdminCourseFilters.vue` (uued)
- `App.vue`, `router/index.js`, `NavigationService.js`, `api-services/CourseService.js`, `locales/et.json`, `en.json` (muudetakse)

## Vastuvõtu kriteeriumid

- [ ] Menüülink ja vaade töötavad; näidisandmetega 10 rida, lülitiga 12
- [ ] Otsing, filtrid, sorteerimine, leheküljestus, kustutamine
- [ ] Tegevuste ikoonid (sama järjekord nagu `/admin-trainings`)
- [ ] Tekstid et/en, `npm run lint` ja `npm run build` puhtad
