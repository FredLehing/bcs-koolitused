# Admini registreerumiste nimekiri ja registreerumise vaade

**Vaated:** `AdminRegistrationsView.vue` (`/admin-registrations`, `adminRegistrationsRoute`) ja `AdminRegistrationView.vue` (`/admin-registration?courseParticipantId={id}`, `adminRegistrationRoute`)

**Roll:** Admin

**Vaste mockupis:** interaktiivne läbimäng `docs/mock-wireframe/loo-mock-vaade/admin-registrations-view/admin-registrations-view-labimang.html` (prototüübi kestas https://claude.ai/artifact/SqxcgWP1BoxUGZqFfSZCPY, vaated "Registreerumised" ja "Registreerumine (admin)")

> Mockupi pilt lisatakse hiljem.

Taustaks: märkmed `docs/mock-wireframe/markmed/admin-registrations-view-markmed.md` ja `admin-registration-view-markmed.md`, skeemid `admin-registrations-view-skeemid.md`. Eeskuju: `AdminEnquiriesView.vue` ja `AdminEnquiryView.vue` (sama muster: nimekiri + üks kirje), `LecturerView.vue` (`returnTo`).

## Kasutajavoog

Admin avab navbari menüüst "Admin" → "Registreerumised" nimekirja (vaikimisi tulevaste toimumiskordade registreerunud, uusimad üleval), otsib ja sorteerib, avab silma ikooniga registreerumise, muudab staatust, tasumist, sülearvuti vajadust või admini märkmeid ja salvestab. `/admin-course` osalejate tabelist silmaga avatud registreerumisest viib "← Tagasi" toimumiskorra juurde tagasi.

## Kasutajaliidese elemendid

### Nimekiri

| Element | Tüüp | Kirjeldus/käitumine |
|---|---|---|
| Menüülink "Registreerumised" | navbar, menüü "Admin" | kohe "Koolituste päringud" järel (i18n `navbar.manageRegistrations`, en "Registrations") |
| Pealkiri "Registreerumised" | h1 | |
| Otsinguväli | `type="search"` | frontendis: osaleja nimi, e-post, koolituse nimi (sisaldab, tõstutundetu) |
| "Näita ka loobunud" | lüliti, vaikimisi väljas | uus päring `includeCancelled` |
| "Näita ka toimunud" | lüliti, vaikimisi väljas | uus päring `includePast` |
| Tabel | Bootstrap tabel | Registreerus \| Osaleja \| E-post \| Koolitus \| Toimumisaeg \| Tasunud \| Vajab sülearvutit \| Staatus \| Tegevused; loobunu rida tuhmim (`text-body-secondary`) |
| Toimumisaeg | link | `formatLocalDate(start) – formatLocalDate(end)` → `/admin-course?courseId={id}`; toimunul märgis "Toimunud" |
| Tasunud, Vajab sülearvutit | `CheckMark` | ✓ / ✗ |
| Sorteeritavad veerud | `SortableColumnHeader` | Registreerus, Osaleja, Koolitus, Toimumisaeg, Tasunud, Vajab sülearvutit, Staatus (registreerunud → loobunud); vaikimisi backendi järjekord (uusimad eespool) |
| Staatus | `CourseParticipantStatusBadge.vue` (uus) | "Registreerunud" (`text-bg-primary`) / "Loobunud" (`text-bg-secondary`) |
| Vaata | silma ikoon (`PhEye`) | → `/admin-registration?courseParticipantId={id}` |
| "Kokku N registreerumist" | tekst | filtreeritud ridade arv |

### Registreerumise vaade

| Element | Tüüp | Kirjeldus/käitumine |
|---|---|---|
| Tagasilink | link | `returnTo` olemasolul (ja `NavigationService.isInternalPath`) "← Tagasi" → `returnTo`, muidu "← Kõik registreerumised" → `/admin-registrations` |
| Pealkiri "Registreerumine", all osaleja nimi | h1 | |
| Kaart "Osaleja" | `fieldset` + `dl`, ainult lugemiseks | Nimi, E-post (`mailto:`), Telefon (`tel:`), Konto e-post (ainult kui erineb e-postist) |
| Kaart "Toimumiskord" | `fieldset` + `dl`, ainult lugemiseks | Koolitus, Toimumisaeg, Staatus (`CourseStatusBadge` + "Toimunud"); link "Ava toimumiskord" → `/admin-course?courseId={id}` |
| Kaart "Registreerumine" | vorm | Staatus: raadionupud Registreerunud / Loobunud; lülitid "Tasunud", "Vajab sülearvutit"; "Osaleja lisainfo" ainult lugemiseks (`notes`, tühi → "—", `white-space: pre-wrap`); "Admini märkmed" `textarea`; Registreerus ja Viimati muudetud (`formatDateTime`) |
| "Salvesta" | nupp | `PUT`; staatuse muutmisel enne `ConfirmModal` ("Märgi osaleja loobunuks?" / "Taasta registreerumine?"); pärast edukat kutset registreerumine uuesti ja eduteade "Registreerumine salvestatud" samas vaates |
| "Tühista" | nupp | taastab laaditud väärtused |

### `/admin-course` muudatus (`CourseParticipantsTable.vue`)

- Viimane veerg "Tegevused": silma ikoon → `/admin-registration?courseParticipantId={id}&returnTo=/admin-course?courseId={courseId}`.
- Staatuse märgis `CourseParticipantStatusBadge`-iga (sama mis nimekirjas).

## Käitumine

- Ainult adminile, muidu `NotAuthorizedView`.
- Kuupäev-kellaaeg: `FormatService.formatDateTime(instant)`; toimumisaeg `formatLocalDate`.
- Keele vahetusel mõlemas vaates päring uuesti (`contentLang`).
- Vead: nimekirja päring → veavaade; registreerumise vaates 404 → veavaade; salvestamise viga → `AlertDanger` vaates.

## API kutsed

- `GET /api/admin-registrations?contentLang=&includeCancelled=&includePast=` — `docs/tasks/backend/GET-api-admin-registrations.md`
- `GET /api/admin-registration/{courseParticipantId}?contentLang=` — `docs/tasks/backend/GET-api-admin-registration-courseParticipantId.md`
- `PUT /api/admin-registration/{courseParticipantId}` — `docs/tasks/backend/PUT-api-admin-registration-courseParticipantId.md`

## Komponendid ja failistruktuur

- `views/AdminRegistrationsView.vue`, `views/AdminRegistrationView.vue`, `components/common/CourseParticipantStatusBadge.vue`, `api-services/CourseParticipantService.js` (uued)
- `App.vue`, `router/index.js`, `NavigationService.js`, `components/course/CourseParticipantsTable.vue`, `locales/et.json`, `en.json` (muudetakse)

## Vastuvõtu kriteeriumid

- [ ] Menüülink ja mõlemad vaated töötavad
- [ ] Otsing, kaks lülitit, sorteerimine, "Kokku N registreerumist"
- [ ] Salvestamine, staatuse muutmise kinnitus, "Tühista" ja eduteade
- [ ] `/admin-course` silm ja "← Tagasi" `returnTo` kaudu
- [ ] Tekstid et/en
