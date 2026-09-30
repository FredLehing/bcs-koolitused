# Admini koolitajate nimekiri

**Teenus:** `GET /api/admin-lecturers?contentLang={contentLang}&includeDeleted={includeDeleted}`

**Kasutav vaade:** `AdminLecturersView.vue` (`/admin-lecturers`)

> Mockupi pilt lisatakse hiljem. Seni vt läbimängu `docs/mock-wireframe/loo-mock-vaade/admin-lecturers-view/admin-lecturers-view-labimang.html` ja märkmeid `docs/mock-wireframe/markmed/admin-lecturers-view-markmed.md`.

Eeldab taski `lecturer-db-changes.md` (view `admin_lecturer_summary`, `lecturer_photo`, `lecturer.status`).

## Sisend

**Query parameetrid:**

| Nimi | Tüüp | Kohustuslik | Kirjeldus |
|---|---|---|---|
| `contentLang` | String | jah | Keel, milles tagastatakse `title` (ametinimetus) ja `lecturerTranslationId` (`"et"` / `"en"`) |
| `includeDeleted` | Boolean | ei (vaikimisi `false`) | `true` = ka kustutatud koolitajad (`status = 'D'`) |

Request body't pole. Leheküljestust, sorteerimise parameetreid ja otsingut pole — nimekiri on väike ja otsing nime järgi tehakse frontendis.

## Väljund

**Response (200 OK):** `List<AdminLecturerSummaryDto>` (ettepanek), sorteeritud `fullName` järgi (A → Õ, lisaks `lecturerId`).

```json
[
  {
    "lecturerId": 2,
    "lecturerTranslationId": 3,
    "fullName": "Merje Vaide",
    "title": "Projektijuht/lektor",
    "status": "A",
    "hasAllTranslations": true,
    "missingTranslationLanguageCodes": [],
    "trainingCount": 2,
    "upcomingCourseCount": 1,
    "updatedAt": "2026-07-15T06:00:00Z"
  },
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
  {
    "lecturerId": 1,
    "lecturerTranslationId": 1,
    "fullName": "Rain Tüür",
    "title": "Lektor/konsultant",
    "status": "A",
    "hasAllTranslations": true,
    "missingTranslationLanguageCodes": [],
    "trainingCount": 3,
    "upcomingCourseCount": 2,
    "updatedAt": "2026-07-15T06:00:00Z"
  },
  ...
]
```

Näidises on 3 esimest ja Rain Tüür; kokku 8 aktiivset koolitajat (vt `lecturer-db-changes.md`, seed).

`includeDeleted=true` korral lisandub (tähestiku järjekorras) Virve Räni (`"status": "D"`, `"title": "Lektor/konsultant"`, `missingTranslationLanguageCodes: ["en"]`, `updatedAt` `2026-09-01T13:00:00Z`).

Väljade selgitused:

- **Pilte ei tagastata** — nimekiri ei loe tabelit `lecturer_photo`.
- `title`, `lecturerTranslationId` — `contentLang` keele tõlge, selle puudumisel **põhikeele** (`language.is_main_language`) oma. "Muuda" link avab selle tõlke.
- `status` — `"A"` aktiivne, `"D"` kustutatud (frontend: tuhm rida, märgis "Kustutatud", nupp "Taasta").
- `missingTranslationLanguageCodes` — tõlkekeeled (`language.requires_translation = true`), milles tõlge puudub; tühi list, kui kõik on olemas (view annab `NULL` / komadega stringi — teisenda listiks nagu `GET /api/admin-trainings` puhul).
- `trainingCount` — aktiivsed koolitused (`training.status <> 'D'`), kus ta on vaikimisi koolitaja.
- `upcomingCourseCount` — toimumiskorrad, kus ta on koolitaja, `end_date >= täna` ja `status NOT IN ('D', 'X')`. Kui `> 0`, on nimekirjas prügikast keelatud. **NB:** arvud sõltuvad `course` seed-andmetest; näidises olevad arvud eeldavad kalendri seed-andmete ettepanekut (`admin-training-courses-view-skeemid.md`, jaotis 2). Praeguste andmetega (2 toimumiskorda, staatus `'AVA'`) on Rain Tüüril ja Merje Vaidel kummalgi 1.
- `updatedAt` — hiliseim `lecturer`, `lecturer_translation` ja `lecturer_photo` `updated_at` (`Instant`).

## Eesmärk

Admin näeb `AdminLecturersView` tabelis kõiki koolitajaid (vaikimisi ainult aktiivseid) koos ametinimetuse, tõlgete seisu ja seotud koolituste arvuga. Sealt liigub ta koolitaja vormile ("Muuda"), kustutab (`DELETE-api-lecturer-lecturerId.md`) või lülitiga "Näita kustutatud" taastab koolitaja (`PUT-api-lecturer-lecturerId-restore.md`). Keele vahetusel navbaris laaditakse nimekiri uuesti.

## Seotud andmebaasi tabelid

Vt `docs/database/2_create.sql` ja `lecturer-db-changes.md`.

### admin_lecturer_summary (view, uus)

Üks rida koolitaja ja iga tõlkekeele kohta (sama idee nagu `admin_training_summary`). SQL: `docs/mock-wireframe/loo-mock-vaade/admin-lecturers-view/admin-lecturers-view-skeemid.md`, jaotis 1. Backendis `@Immutable` view entity (eeskuju: `persistance/view/admintrainingsummary/AdminTrainingSummary`).

| Veerg | Kasutus |
|---|---|
| `content_language_code` | filter `contentLang` (alati) |
| `status` | filter: `includeDeleted=false` → `status = 'A'` |
| `lecturer_translation_id`, `title`, `full_name` | väljad; järjestus `full_name`, `lecturer_id` |
| `missing_translation_language_codes`, `has_all_translations` | Tõlked ✓/✗ |
| `training_count`, `upcoming_course_count` | `bigint` → `Long` / `Integer` |
| `updated_at` | Uuendatud |

Seotud tabelid: `lecturer`, `lecturer_translation`, `lecturer_photo`, `language`, `training`, `course`.

## Veaolukorrad

| Olukord | Status code | Response body |
|---|---|---|
| Ootamatu serveripoolne viga | 500 Internal Server Error | Standardne vea response body (vastavalt projekti globaalsele error handler'ile) |

Mockupi märkmetes on `Veateated: —`. Tundmatu `contentLang` annab tühja listi (view's pole selle keele ridu), mitte vea — sama käitumine nagu `GET /api/admin-trainings`.

## Vastuvõtu kriteeriumid

- [ ] Endpoint `GET /api/admin-lecturers` on olemas ja tagastab `AdminLecturerSummaryDto` listi
- [ ] Vaikimisi ainult aktiivsed; `includeDeleted=true` → ka kustutatud
- [ ] `title` ja `lecturerTranslationId` on `contentLang` keeles, puudumisel põhikeeles (`en` korral Kersti Laidvee → et tõlge 5)
- [ ] `missingTranslationLanguageCodes` on list (Kersti Laidvee `["en"]`, teistel `[]`)
- [ ] `trainingCount` ja `upcomingCourseCount` vastavad andmebaasile
- [ ] `updatedAt` arvestab ka tõlgete ja pildi muutmist
- [ ] Sorteeritud `fullName` järgi; pilte ei loeta
- [ ] Teenusel on automaattestid
