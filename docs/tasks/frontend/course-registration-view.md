# Toimumiskorrale registreerumine

**Vaated:** `CourseRegistrationView.vue` (`/course-registration?courseId={id}`, `courseRegistrationRoute`)

**Roll:** Kasutaja (sisse logitud, roll participant); sisse logimata → `/login?redirect=`, admin → `/not-authorized`

**Vaste mockupis:** interaktiivne läbimäng `docs/mock-wireframe/loo-mock-vaade/courses-view/course-registration-view-labimang.html` (prototüübi kest https://claude.ai/artifact/SqxcgWP1BoxUGZqFfSZCPY)

> Mockupi pilt lisatakse hiljem (Balsamiq AI käsk: `docs/mock-wireframe/loo-mock-vaade/courses-view/courses-view-skeemid.md`, jaotis 8 "CourseRegistrationView").

Taustaks: märkmed `docs/mock-wireframe/markmed/course-registration-view-markmed.md`, skeemid `docs/mock-wireframe/loo-mock-vaade/courses-view/courses-view-skeemid.md`.

Eeldab backend taske `GET-api-course-summary-courseId.md`, `GET-api-course-courseId-participant-status.md`, `GET-api-user-userId-participant.md`, `POST-api-course-courseId-participant.md` ja frontend taski `signup-view.md` (login `redirect`).

## Kasutajavoog

Kasutaja vajutab `/course` lehel "Registreeru" (vajadusel logib sisse või loob konto), kontrollib eeltäidetud andmeid, märgib sülearvuti vajaduse, lisab lisainfo ja registreerub; tagasi `/course` lehele eduteatega.

## Kasutajaliidese elemendid

| Element | Tüüp | Kirjeldus/käitumine |
|---|---|---|
| Pealkiri "Registreerumine" | h1 | |
| Kaart "Toimumiskord" (vasak) | `dl` | koolitus, toimumisaeg, päevi/tunde, hind, toimumisviis, koolitajad; link "Tagasi toimumiskorra lehele" |
| Kaart "Osaleja andmed" (parem) | vorm | Eesnimi*, Perekonnanimi*, E-post*, Telefon* (eeltäidetud), vihje "Andmed on täidetud sinu profiilist; muudatused salvestatakse ka profiili.", linnuke "Vajan koolitusel sülearvutit", "Lisainfo" (textarea) |
| "Registreeru" / "Tühista" | nupud | kontroll (kohustuslikud, e-post) → `POST` → `/course?courseId=` eduteatega; "Tühista" → `/course` |
| Juba registreerunud | `AlertSuccess` vormi asemel | "Oled sellele toimumiskorrale juba registreerunud." |
| Täis | `AlertDanger` vormi asemel | "Kohad on täis — küsi lisainfot toimumiskorra lehelt." |

## Käitumine

- Router guard: sisse logimata → `/login?redirect=<praegune rada>`; admin → `NotAuthorizedView`.
- Backendi vead (`COURSE_FULL`, `ALREADY_REGISTERED`, `REGISTRATION_CLOSED`) → `AlertDanger` backendi `message`-iga; 404 → veavaade.
- Eduteade `/course` lehel: router `state` või query (nt `?registered=1`, mis eemaldatakse `router.replace`-iga).

## API kutsed

- `GET /api/course-summary/{courseId}?contentLang=`, `GET /api/course/{courseId}/participant-status?userId=`, `GET /api/user/{userId}/participant`, `POST /api/course/{courseId}/participant`

## Komponendid ja failistruktuur

- `views/CourseRegistrationView.vue`, `api-services/UserService.js` (uued)
- `router/index.js` (rada + guard), `NavigationService.js`, `CourseService.js`, `locales/*` (muudetakse)

## Vastuvõtu kriteeriumid

- [ ] Guard ja redirect töötavad
- [ ] Eeltäitmine, kontroll, registreerumine, tagasi eduteatega
- [ ] Juba registreerunud / täis olekud
- [ ] Tekstid et/en, lint ja build puhtad
