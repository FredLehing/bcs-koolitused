# Avalik toimumiskorra leht ja "Küsi lisainfot"

**Vaated:** `CourseView.vue` (`/course?courseId={id}`, `courseRoute`), modal `EnquiryModal.vue`

**Roll:** Kõik rollid (admin: pliiats, nuppe "Registreeru" ja "Küsi lisainfot" ei näe)

**Vaste mockupis:** interaktiivne läbimäng `docs/mock-wireframe/loo-mock-vaade/courses-view/courses-view-labimang.html` (prototüübi kest https://claude.ai/artifact/SqxcgWP1BoxUGZqFfSZCPY)

> Mockupi pilt lisatakse hiljem (Balsamiq AI käsk: `docs/mock-wireframe/loo-mock-vaade/courses-view/courses-view-skeemid.md`, jaotis 8 "CourseView").

Taustaks: märkmed `docs/mock-wireframe/markmed/course-view-markmed.md`, skeemid `docs/mock-wireframe/loo-mock-vaade/courses-view/courses-view-skeemid.md`. Eeskuju: `TrainingView.vue` (paigutus 8/4, `RichTextContent`, `LecturerCard`, `$route.query` jälgija).

Eeldab backend taske `GET-api-course-summary-courseId.md`, `POST-api-enquiry.md`, `GET-api-course-courseId-participant-status.md`. Registreerumise vaade: `course-registration-view.md`.

## Kasutajavoog

Külastaja avab toimumiskorra, loeb koolituse kirjeldust, vahetab lingiga sama koolituse teise toimumiskorra, küsib modaliga lisainfot või vajutab "Registreeru".

## Kasutajaliidese elemendid

| Element | Tüüp | Kirjeldus/käitumine |
|---|---|---|
| Kaart "Koolitus" (vasak) | `fieldset` | pealkiri (+ admini pliiats → `/course-form?courseId=`), toimumisaeg (+ "Toimunud"), lühikirjeldus, `RichTextContent`, link "Kõik selle koolituse toimumiskorrad" → `/training`; põhikeele varuvariandi märkus |
| Kaart "Toimumiskorrad" (parem, "Toimumiskord" kohal) | lingid üksteise all | `12/10/2026 – 15/10/2026 · Kohapeal` (+ "Täis"); praegune paksus kirjas ▸, mitte link; teised `<RouterLink replace>` → `/course?courseId=` ilma `returnTo`-ta; `$route.query` jälgija laadib andmed uuesti, ajalukku uut kirjet ei lisandu; peidus kui teisi pole |
| Kaart "Toimumiskord" (parem) | `dl` | Toimumisaeg, Päevi, Akad. tunde, Hind, Toimumisviis, Koolituse keel (lipp), Kategooria, Rahastus, "Täis" märgis |
| "Registreeru" | nupp | sisse logimata → `/login?redirect=/course-registration?courseId=`; kasutaja → `/course-registration?courseId=`; registreerunud (`participant-status` = `R`) → märge "✓ Oled sellele toimumiskorrale registreerunud"; täis → keelatud "Kohad on täis"; möödunud → keelatud; admin → peidus |
| "Küsi lisainfot" | nupp | avab `EnquiryModal`; möödunud → keelatud; admin → peidus |
| Kaart "Koolitajad" | `LecturerCard` iga koondvastuse `lecturers` kirje kohta | prop `lecturerSummary`, kaardi JSON-päringud puuduvad; peidus kui pole |
| Eduteade | `AlertSuccess` | "Registreerumine õnnestus! …" (registreerumise vaatest tulles) ja "Aitäh! Sinu päring on saadetud, võtame peagi ühendust." |

### Modal `EnquiryModal.vue` (`BaseModal` peal)

| Element | Kirjeldus |
|---|---|
| Pealkiri "Küsi lisainfot" + kontekst | koolituse nimi · toimumisaeg |
| Väljad | Eesnimi*, Perekonnanimi*, E-post*, Telefon*, Ettevõte, Sõnum* (`maxlength=255`, loendur "N / 255"); tekst "Kasutame sinu andmeid ainult päringule vastamiseks." |
| "Saada" / "Tühista" | kontroll (kohustuslikud, e-post) → `POST /api/enquiry` ise → emit `event-enquiry-sent`; viga modalis (`AlertDanger`); ×/Esc/taust sulgeb |

Propsid: `trainingId`, `courseId` (valikuline — hiljem `/training` lehel), `title`, `startDate`, `endDate`.

## Käitumine

- `$route.query.courseId` muutus (toimumiskordade lingid) → uus laadimine.
- `participant-status` kutsutakse ainult sisseloginud mitte-admin kasutajale.
- Keele vahetusel uuesti. 404 → veavaade.

## API kutsed

- `GET /api/course-summary/{courseId}?contentLang=`, `GET /api/course/{courseId}/participant-status?userId=`, `POST /api/enquiry` (uued); `GET /api/lecturer/{lecturerId}/photo?v={photoVersion}` (olemas, ainult pilt; kaardi tekstid koondvastuses)

## Komponendid ja failistruktuur

- `views/CourseView.vue`, `components/modals/EnquiryModal.vue` (uued)
- `router/index.js`, `NavigationService.js`, `CourseService.js`, `EnquiryService.js`, `locales/*` (muudetakse)

## Vastuvõtu kriteeriumid

- [ ] Paigutus, toimumiskordade lingid, koolitajad
- [ ] "Registreeru" kõik olekud rolliti; adminile nuppe pole
- [ ] Modal: kontroll, saatmine, eduteade
- [ ] Tekstid et/en, lint ja build puhtad

## Koolitajakaartide päringud

Koolitajate kaartide tekstid ja foto versioonid tulevad vaate koondvastuse
`lecturers` massiivist. LecturerCard ei tee JSON-päringuid ega pea oma
laadimisolekut. Keelevahetusel laadib andmed uuesti vaade. Kursuse
registreerumisstaatus (ainult sisseloginud kasutajale) ja fotode failipäringud
on endiselt eraldi. `/lecturers` ja `/lecturer` töötavad juba sama põhimõttega.
