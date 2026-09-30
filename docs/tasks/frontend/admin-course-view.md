# Toimumiskorra ülevaade (admin)

**Vaated:** `AdminCourseView.vue` (`/admin-course?courseId={id}`, `adminCourseRoute`)

**Roll:** Admin

**Vaste mockupis:** interaktiivne läbimäng `docs/mock-wireframe/loo-mock-vaade/courses-view/admin-all-courses-view-labimang.html` (prototüübi kest https://claude.ai/artifact/SqxcgWP1BoxUGZqFfSZCPY)

> Mockupi pilt lisatakse hiljem (Balsamiq AI käsk: `docs/mock-wireframe/loo-mock-vaade/courses-view/courses-view-skeemid.md`, jaotis 8 "AdminCourseView").

Taustaks: märkmed `docs/mock-wireframe/markmed/admin-course-view-markmed.md`, skeemid `docs/mock-wireframe/loo-mock-vaade/courses-view/courses-view-skeemid.md`. Eeskuju: `AdminEnquiryView.vue` (kaart `dl`-ga), `AdminRoomsView.vue` (frontendi otsing).

Eeldab backend taske `GET-api-admin-course-courseId.md`, `GET-api-course-courseId-participants.md`, `GET-api-course-courseId-enquiries.md`.

## Kasutajavoog

Admin avab toimumiskorra `/admin-all-courses` silmast, vaatab andmeid, osalejaid (otsing, tasumise filter, loobunud) ja huvilisi ning liigub edasi muutmisse, kalendrisse või päringu juurde.

## Kasutajaliidese elemendid

| Element | Tüüp | Kirjeldus/käitumine |
|---|---|---|
| Pealkiri "Toimumiskord" + koolituse nimi | h1 + alapealkiri | |
| Kiirnupud | nupud | "Muuda" → `/course-form?courseId=`, "Koolituse kalender" → `/admin-training-courses?trainingId=`, "Koolituste kalender" → `/admin-all-courses` |
| Kaart "Toimumiskord" | `fieldset` + `dl` | Koolitus (link `trainingRoute`), Toimumisaeg, Päevi, Akad. tunde, Hind, Koolitajad / "—", Ruum / "—", Veebilink (link) / "—", Staatus (`CourseStatusBadge` + "Toimunud"), Esile tõstetud (Jah/Ei), Märkmed (`pre-wrap`) / "—"; `O`/`F` korral link "Vaata avalikul lehel" → `/course?courseId=` |
| Tabel "Osalejad" | `CourseParticipantsTable.vue` | otsing (nimi/e-post), Tasumine (Kõik/Tasunud/Tasumata), lüliti "Näita ka loobunud"; Nimi \| E-post \| Telefon \| Registreerus \| Tasunud \| Sülearvuti \| Staatus \| Märkmed; loobunu rida tuhmim; "Kokku N osalejat, neist M tasunud" (`R` osalejad); tühi "Osalejaid pole" |
| Tabel "Huvilised" | `CourseEnquiriesTable.vue` | otsing (nimi/e-post/ettevõte), Staatus (Kõik/Uued/Käsitletud); Saabunud \| Nimi \| E-post \| Ettevõte \| Staatus (`EnquiryStatusBadge`) \| silm → `/admin-enquiry?enquiryId=`; tühi "Huvilisi pole" |

## Käitumine

- Ainult adminile. Tabelid ainult lugemiseks (osalejate haldus hiljem).
- Keele vahetusel toimumiskord uuesti (koolituse nimi). 404 → veavaade.
- Osaleja staatuse tekstid i18n `courseParticipantStatus.R` / `.C` ("Registreerunud" / "Loobunud").

## API kutsed

- `GET /api/admin-course/{courseId}?contentLang=`, `GET /api/course/{courseId}/participants`, `GET /api/course/{courseId}/enquiries`

## Komponendid ja failistruktuur

- `views/AdminCourseView.vue`, `components/course/CourseParticipantsTable.vue`, `components/course/CourseEnquiriesTable.vue` (uued)
- `router/index.js`, `NavigationService.js`, `CourseService.js`, `locales/*` (muudetakse)

## Vastuvõtu kriteeriumid

- [ ] Kõik kaardi väljad ja lingid
- [ ] Osalejate ja huviliste filtrid frontendis; kokkuvõtte rida
- [ ] Tekstid et/en, lint ja build puhtad
