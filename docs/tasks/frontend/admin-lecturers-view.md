# Admini koolitajate nimekiri: otsing, kustutamine ja taastamine

**Vaade:** `AdminLecturersView.vue`, route `/admin-lecturers` (nimi nt `adminLecturersRoute`)

**Roll:** Admin

**Vaste mockupis:** interaktiivne läbimäng `docs/mock-wireframe/loo-mock-vaade/admin-lecturers-view/admin-lecturers-view-labimang.html` (artifact https://claude.ai/artifact/HNuz21ynvnWCWtYfB1SfzX)

> Mockupi pilt lisatakse hiljem (Balsamiq AI käsk: `admin-lecturers-view-skeemid.md`, jaotis 10).

Taustaks: märkmed `docs/mock-wireframe/markmed/admin-lecturers-view-markmed.md`, otsused ja skeemid `docs/mock-wireframe/loo-mock-vaade/admin-lecturers-view/admin-lecturers-view-skeemid.md`, tööde järjekord `admin-lecturers-view-toode-jarjekord.md` (samas kaustas). Vorm on eraldi taskis `lecturer-form-view.md`.

## Kasutajavoog

Admin avab navbari menüüst "Admin" → "Koolitajad" tabeli, kus on vaikimisi kõik aktiivsed koolitajad nime järgi. Otsinguväli filtreerib nimekirja trükkimise ajal. Rea pliiatsiga avaneb koolitaja vorm, prügikastiga saab koolitaja kustutada (kui tal pole tulevasi toimumiskordi). Lüliti "Näita kustutatud" toob nähtavale kustutatud koolitajad, keda saab taastada. Pealkirja real on nupp "+ Lisa uus koolitaja".

## Kasutajaliidese elemendid

| Element | Tüüp | Kirjeldus/käitumine |
|---|---|---|
| Menüülink "Lisa uus koolitaja" | navbar, menüü "Admin" | Olemasolevate linkide järel eraldaja, siis see link → `/lecturer-form` (i18n `navbar.addLecturer`) |
| Menüülink "Koolitajad" | navbar, menüü "Admin" | → `/admin-lecturers` (i18n `navbar.manageLecturers`) |
| Pealkiri "Koolitajad" | h2 | |
| "+ Lisa uus koolitaja" | nupp (primary), pealkirja real paremal | → `/lecturer-form`; kitsal ekraanil pealkirja all |
| "Otsi nime järgi…" | tekstiväli | Filtreerib **frontendis** trükkimise ajal (`fullName` sisaldab, tõstutundetu); API kutset ei tehta |
| "Näita kustutatud" | lüliti (`form-switch`), vaikimisi väljas | Muutmisel uus päring `includeDeleted=true/false` |
| Tabel | Bootstrap tabel | Veerud: Nimi \| Ametinimetus \| Tõlked \| Koolitusi \| Tulevasi toimumiskordi \| Uuendatud \| Tegevused. **Pilte ei kuvata** |
| Tõlked | ✓ / ✗ | ✓ roheline, kui `hasAllTranslations`; ✗ punane, tooltip "Puudub: en" (`missingTranslationLanguageCodes`) — sama nagu `AdminTrainingsView` |
| Uuendatud | kuupäev | `updatedAt` kujul `30/09/2026` |
| "Muuda" | pliiatsi ikoon | → `/lecturer-form?lecturerId={id}&lecturerTranslationId={id}` |
| "Kustuta" | prügikasti ikoon (`LecturerDeleteButton.vue`) | Kinnituse modal → `DELETE`. Kui `upcomingCourseCount > 0`: `disabled`, tooltip "Koolitajal on tulevasi toimumiskordi — vali neile enne teine koolitaja" |
| Kustutatud rida | tuhm rida | Märgis "Kustutatud" ja nupp "Taasta" (kinnitusega); Muuda ja Kustuta peidus |
| "Kokku N koolitajat" | tekst tabeli all | Filtreeritud ridade arv (i18n mitmus) |
| "Koolitajaid ei leitud" | tühi rida | Kui filtreeritud nimekiri on tühi |

## Käitumine ja valideerimine

1. Avamisel: `GET /api/admin-lecturers?contentLang={UI keel}&includeDeleted=false`. Mitte-admin → `NotAuthorizedView` (sama kontroll nagu `AdminTrainingsView`).
2. Otsing: `computed: filteredLecturers` (`searchText` järgi); päringut ei tehta.
3. Lüliti "Näita kustutatud" → sama päring `includeDeleted` uue väärtusega; otsingusõna jääb.
4. Kustutamine (`LecturerDeleteButton`): modal "Kas soovid koolitaja „{fullName}“ kustutada? Ta kaob „Vali koolitaja“ valikust ja koolitaja kaarte enam ei kuvata." → `DELETE /api/lecturer/{lecturerId}` → emit `event-lecturer-deleted` → vaade laadib nimekirja uuesti, eduteade "Koolitaja kustutatud". 403 `LECTURER_HAS_UPCOMING_COURSES` (nt teises aknas lisati toimumiskord) → emit `event-delete-error` backendi `message`-iga, vaade näitab seda ja laadib nimekirja uuesti. Päringu ajal nupp `disabled`.
5. Taastamine: "Taasta" → modal "Kas soovid koolitaja „{fullName}“ taastada? Teda saab siis jälle koolitustele ja toimumiskordadele valida." → `PUT /api/lecturer/{lecturerId}/restore` → nimekiri uuesti, eduteade "Koolitaja taastatud".
6. Keele vahetus navbaris (`watch: contentLang`) → nimekiri uuesti (`title` ja `lecturerTranslationId` sõltuvad keelest); otsingusõna ja lüliti jäävad.
7. Muu viga (404, 500) → `NavigationService.navigateToErrorView()`.

## API kutsed

### `GET /api/admin-lecturers`

**Backend task:** `docs/tasks/backend/GET-api-admin-lecturers.md`

`AdminLecturerSummaryDto.java` — response (200), sorteeritud `fullName` järgi:

```json
[
  {
    "lecturerId": 3,
    "lecturerTranslationId": 5,
    "fullName": "Kersti Laidvee",
    "title": "Lektor/konsultant",
    "status": "A",
    "hasAllTranslations": false,
    "missingTranslationLanguageCodes": ["en"],
    "trainingCount": 1,
    "upcomingCourseCount": 0,
    "updatedAt": "2026-09-20T07:00:00Z"
  },
  ...
]
```

**Veateated:** — (500 → üldine veavaade)

### `DELETE /api/lecturer/{lecturerId}`

**Backend task:** `docs/tasks/backend/DELETE-api-lecturer-lecturerId.md`. Request ja response body puuduvad.

| Status code | errorCode | message | Frontend käitumine |
|---|---|---|---|
| 403 | `LECTURER_HAS_UPCOMING_COURSES` | "Koolitajal on tulevasi toimumiskordi, vali neile enne teine koolitaja" | emit `event-delete-error` → vaade näitab `message`-i, nimekiri uuesti |
| 404 | `PRIMARY_KEY_NOT_FOUND` | "Ei leidnud primary keyd 'lecturerId' väärtusega: 123" | `NavigationService.navigateToErrorView()` |

### `PUT /api/lecturer/{lecturerId}/restore`

**Backend task:** `docs/tasks/backend/PUT-api-lecturer-lecturerId-restore.md`. Request ja response body puuduvad. 404 → üldine veavaade.

## Mock-vastused

Kuni backend valmib, sama muster nagu `AdminTrainingsView` (päris axios-kutse kommentaaris, `mockResponse(MockDatabase.…)`). `MockDatabase.js`-i koolitajad `lecturer-db-changes.md` seed-andmete järgi (9 koolitajat, tõlked, `status`, arvud) ja meetodid `getAdminLecturers(contentLang, includeDeleted)`, `deleteLecturer(lecturerId)` (403, kui `upcomingCourseCount > 0`), `restoreLecturer(lecturerId)`. Eeskuju: läbimängu skripti `adminLecturersDto`.

## Komponendid ja failistruktuur

| Fail | Uus / muudetakse | Sisu |
|---|---|---|
| `views/AdminLecturersView.vue` | uus | Vaade; olek `lecturers`, `searchText`, `includeDeleted`; `computed: filteredLecturers` |
| `components/common/LecturerDeleteButton.vue` | uus | Propsid `lecturerId`, `fullName`, `upcomingCourseCount`; `disabled` + tooltip; `ConfirmModal` + `DELETE`; emits `event-lecturer-deleted`, `event-delete-error` (muster: `TrainingDeleteButton.vue`) |
| `components/common/LecturerRestoreButton.vue` | uus | "Taasta" + `ConfirmModal` + `PUT …/restore`; emit `event-lecturer-restored` |
| `api-services/LecturerService.js` | muudetakse | + `sendGetAdminLecturersRequest(contentLang, includeDeleted)`, `sendDeleteLecturerRequest(lecturerId)`, `sendPutLecturerRestoreRequest(lecturerId)` |
| `NavigationService.js` | muudetakse | + `navigateToAdminLecturersView()`, `navigateToLecturerFormView(query)` |
| `router/index.js` | muudetakse | + `/admin-lecturers` |
| `App.vue` | muudetakse | Menüüsse "Admin" eraldaja + "Lisa uus koolitaja", "Koolitajad" |
| `locales/et.json`, `locales/en.json` | muudetakse | Vaate tekstid (`adminLecturers.*`), menüülingid |

Kuupäeva vorming ja ✓/✗ tooltip: kasuta sama lahendust mis `AdminTrainingsView`-s.

## Vastuvõtu kriteeriumid

- [ ] Menüüs "Admin" on eraldaja järel "Lisa uus koolitaja" ja "Koolitajad"
- [ ] Mitte-admin suunatakse `NotAuthorizedView`-le
- [ ] Avamisel kuvatakse aktiivsed koolitajad nime järgi, 7 veergu, pilte pole, "Kokku N koolitajat"
- [ ] Tõlgete ✓/✗ ja tooltip "Puudub: en"; kuupäev kujul `30/09/2026`
- [ ] Otsing filtreerib trükkimise ajal ilma API kutseta; tühi tulemus "Koolitajaid ei leitud"
- [ ] Tulevaste toimumiskordadega koolitaja prügikast on keelatud koos tooltip'iga
- [ ] Kustutamine: kinnitus → eduteade, rida kaob; 403 näidatakse backendi tekstiga
- [ ] "Näita kustutatud" näitab tuhmid read märgise ja nupuga "Taasta"; taastamine töötab
- [ ] "Muuda" avab vormi õige `lecturerTranslationId`-ga; "+ Lisa uus koolitaja" avab tühja vormi
- [ ] Keele vahetus laadib nimekirja uuesti, otsing ja lüliti jäävad
