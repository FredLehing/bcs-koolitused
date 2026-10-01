# Avalehe täiendus: suunavad kaardid ja järgmised koolitused

**Vaade:** `HomeView.vue` (`/`, `homeRoute`)

**Roll:** Kõik rollid (sh külastajad; admin näeb kaartidel pliiatsit)

**Vaste mockupis:** interaktiivne läbimäng `docs/mock-wireframe/loo-mock-vaade/home-view/home-view-labimang.html` (prototüübi kestas https://claude.ai/artifact/SqxcgWP1BoxUGZqFfSZCPY, vaade "Avaleht")

> Mockupi pilt lisatakse hiljem.

Taustaks: märkmed `docs/mock-wireframe/markmed/home-view-markmed.md`, skeemid `home-view-skeemid.md`. Eeskuju: `CoursesView.vue` (`CourseCard.vue` kaardid, `contentLang` watcher).

## Kasutajavoog

Külastaja avab avalehe, näeb otsingut ja kahte suurt kaarti ("Meie koolitused", "Koolituste kalender"), mis selgitavad, kuhu kumbki viib. Allpool on järgmised 5 toimumiskorda, kuhu saab registreeruda; "Vaata lähemalt" avab toimumiskorra. Lõpus kutsub lause ja nupp vaatama kogu kalendrit.

## Kasutajaliidese elemendid

| Element | Tüüp | Kirjeldus/käitumine |
|---|---|---|
| Pealkiri, alapealkiri, otsing | olemas | ei muutu (otsing → `/trainings?searchText=`) |
| Kaart "Meie koolitused" | `RouterLink` kaardina | ikoon `PhBooks`, tekst "Vaata kõiki meie koolitusi ja nende sisu.", "Vaata koolitusi →"; → `/trainings` (`trainingsRoute`) |
| Kaart "Koolituste kalender" | `RouterLink` kaardina | ikoon `PhCalendarDots`, tekst "Vali sobiv toimumisaeg ja registreeru.", "Vaata kalendrit →"; → `/courses` (`coursesRoute`) |
| Kaartide paigutus | Bootstrap `row g-4`, `col-md-6` | kitsal ekraanil üksteise all; hover'il vari ja `border-primary` |
| "Meie järgmised 5 koolitust" | h2 | ainult siis, kui toimumiskordi on |
| Toimumiskordade kaardid | `CourseCard.vue` | kuni 5 tk, sama mis `/courses` ("Vaata lähemalt" → `/course?courseId=`, adminile pliiats) |
| Turunduslause | tekst | "Leia endale sobiv koolitus — uusi toimumiskordi lisandub pidevalt." |
| "Vaata kõiki toimuvaid koolitusi →" | `RouterLink` nupuna (`btn btn-primary`) | → `/courses` |

## Käitumine

- Avamisel `GET /api/next-courses?contentLang=&limit=5`; keele vahetusel uuesti (`contentLang` watcher).
- Tühi vastus või päringu viga → plokk "Meie järgmised 5 koolitust" peidetud (veavaatesse ei suunata); suunavad kaardid ja turunduslause jäävad.
- Tekstid i18n-is (`homeView.*`), et ja en.

## API kutsed

- `GET /api/next-courses?contentLang=&limit=5` — `docs/tasks/backend/GET-api-next-courses.md` (`CourseService.sendGetNextCoursesRequest(contentLang, limit)`)

## Komponendid ja failistruktuur

- `views/HomeView.vue`, `api-services/CourseService.js`, `locales/et.json`, `en.json` (muudetakse)
- `components/course/CourseCard.vue` (olemas, ei muutu)

## Vastuvõtu kriteeriumid

- [x] Kaks suunavat kaarti viivad `/trainings` ja `/courses` vaatesse
- [x] Kuni 5 järgmist avatud toimumiskorda `CourseCard`-idena; tühjal vastusel plokk peidetud
- [x] Keele vahetusel kaardid uues keeles
- [x] Turunduslause ja nupp `/courses` vaatesse
- [x] Tekstid et/en
