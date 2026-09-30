# Avalik koolituste kalender

**Vaated:** `CoursesView.vue` (`/courses`, `coursesRoute`)

**Roll:** Kõik rollid (admin näeb kaardil pliiatsit)

**Vaste mockupis:** interaktiivne läbimäng `docs/mock-wireframe/loo-mock-vaade/courses-view/courses-view-labimang.html` (prototüübi kest https://claude.ai/artifact/SqxcgWP1BoxUGZqFfSZCPY)

> Mockupi pilt lisatakse hiljem (Balsamiq AI käsk: `docs/mock-wireframe/loo-mock-vaade/courses-view/courses-view-skeemid.md`, jaotis 8 "CoursesView").

Taustaks: märkmed `docs/mock-wireframe/markmed/courses-view-markmed.md`, skeemid `docs/mock-wireframe/loo-mock-vaade/courses-view/courses-view-skeemid.md`. Eeskuju: `TrainingsView.vue` + `TrainingCard.vue` (otsing, filtrid, leheküljestus).

Eeldab backend taski `GET-api-courses.md`.

## Kasutajavoog

Külastaja avab "Koolitused" → "Koolituste kalender", filtreerib perioodi, toimumisviisi, keele, kategooria ja rahastuse järgi, otsib ning avab toimumiskorra "Vaata lähemalt".

## Kasutajaliidese elemendid

| Element | Tüüp | Kirjeldus/käitumine |
|---|---|---|
| Navbar "Koolitused" | rippmenüü (nagu "Admin") | "Meie koolitused" → `/trainings`, "Koolituste kalender" → `/courses` (i18n `navbar.ourTrainings`, `navbar.coursesCalendar`) |
| Pealkiri "Koolituste kalender" | h1 | |
| Filtrid (vasak veerg) | `CourseFilters.vue` | Alates, Kuni (`DateInput`), Toimumisviis (raadio Kõik/Kohapeal/Veebis), lüliti "Peida täis", Koolituse keel, Koolituse kategooria, Rahastus (raadio); link "Tühjenda filtrid" kui mõni valitud; muutus → kohe päring, `page = 0` |
| Otsing | nagu `/trainings` | "Otsi"/Enter, ×/Esc, küsimärgi tooltip, tulemuste rida + "Tühista otsing" |
| Toimumiskorra kaart | `CourseCard.vue` | kuupäevaplokk ("05.–09. okt 2026", "5 päeva · 40 t"; en "5–9 Oct 2026"), pealkiri, lühikirjeldus, kategooria, rahastus, "Koolitaja: …", märgised Kohapeal/Veebis, õppekeele lipp (`FlagIcon`), hind, "Täis" märgis, "Vaata lähemalt" → `/course?courseId=`; esile tõstetud: ☆ + kollakas taust; adminile pliiats → `/course-form?courseId=` |
| `PaginationNav` | | limit 5 |
| Tühi tulemus | tekst | "Valitud filtritele vastavaid toimumiskordi ei leitud." / otsingu korral nagu `/trainings` |

## Käitumine

- Keele vahetusel uuesti kaardid ja valikud; filtrid ja leht jäävad (tühjaks jäänud lehelt eelmisele).

## API kutsed

- `GET /api/courses?...` (uus), `GET /api/categories`, `GET /api/languages`, `GET /api/funding-types` (olemas)

## Komponendid ja failistruktuur

- `views/CoursesView.vue`, `components/course/CourseCard.vue`, `components/forms/CourseFilters.vue` (uued)
- `App.vue` (rippmenüü), `router/index.js`, `NavigationService.js`, `CourseService.js`, `locales/*` (muudetakse)

## Vastuvõtu kriteeriumid

- [ ] Navbari rippmenüü, filtrid, otsing, leheküljestus
- [ ] Kaart: kõik elemendid, esile tõstetud, täis
- [ ] Tekstid et/en, lint ja build puhtad
