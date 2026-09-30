# Koolitaja olemasolevad tõlked (lipukesed)

**Teenus:** `GET /api/lecturer/{lecturerId}/lecturer-translations`

**Kasutav vaade:** `LecturerFormView.vue` (`state: "update"` ja `"new-translation"`)

> Mockupi pilt lisatakse hiljem. Seni vt läbimängu `docs/mock-wireframe/loo-mock-vaade/admin-lecturers-view/admin-lecturers-view-labimang.html` ja märkmeid `docs/mock-wireframe/markmed/lecturer-form-view-state-update-markmed.md`.

Sama muster nagu `GET-api-training-trainingId-training-translations.md`. Eeldab taske `lecturer-db-changes.md` ja `lecturer-deleted-status.md`.

## Sisend

**Path variable:**

| Nimi | Tüüp | Kirjeldus |
|---|---|---|
| `lecturerId` | Integer | Koolitaja ID (`lecturer.id`) |

Query parameetreid ja request body't pole.

## Väljund

**Response (200 OK):** `List<LecturerTranslationItemDto>` (ettepanek), sorteeritud `language.id` järgi (põhikeel esimesena).

Kersti Laidvee (`lecturerId = 3`) — ainult põhikeel:

```json
[
  {
    "lecturerTranslationId": 5,
    "languageId": 1,
    "languageCode": "et",
    "isMainLanguage": true
  }
]
```

Rain Tüür (`lecturerId = 1`):

```json
[
  { "lecturerTranslationId": 1, "languageId": 1, "languageCode": "et", "isMainLanguage": true },
  { "lecturerTranslationId": 2, "languageId": 2, "languageCode": "en", "isMainLanguage": false }
]
```

- Frontend võrdleb `languageCode` väärtusi `GET /api/languages` tõlkekeeltega (`requiresTranslation = true`): tõlge olemas → värviline lipp (klikk avab selle tõlke), puudub → hall lipp (klikk → `state: "new-translation"`).
- `isMainLanguage` tuleb `language.is_main_language` veerust; uue tõlke olekus laaditakse selle rea `lecturerTranslationId` järgi põhikeele tekstid eeltäitmiseks.

## Eesmärk

`LecturerFormView` näitab tõlkelippe (`TranslationFlags.vue`) ja leiab uue tõlke eeltäitmiseks põhikeele tõlke ID — sama tõlkeloogika nagu `TrainingFormView`-l.

## Seotud andmebaasi tabelid

Vt `docs/database/2_create.sql` ja `lecturer-db-changes.md`: `lecturer_translation`, `language`.

Näidisandmed: Rain Tüür ja Merje Vaide — et + en; Kersti Laidvee — ainult et; Virve Räni — kustutatud → 404.

## Veaolukorrad

| Olukord | Status code | Response body |
|---|---|---|
| `lecturerId` ei leidu või koolitaja on kustutatud | 404 Not Found | `{ "message": "Ei leidnud primary keyd 'lecturerId' väärtusega: 123", "errorCode": "PRIMARY_KEY_NOT_FOUND" }` |
| Ootamatu serveripoolne viga | 500 Internal Server Error | Standardne vea response body (vastavalt projekti globaalsele error handler'ile) |

404: `LecturerService.getValidActiveLecturerBy(lecturerId, "lecturerId")`.

## Vastuvõtu kriteeriumid

- [ ] Endpoint `GET /api/lecturer/{lecturerId}/lecturer-translations` on olemas
- [ ] Tagastab kõik koolitaja tõlked koos `languageCode` ja `isMainLanguage` väljaga
- [ ] Kustutatud või olematu koolitaja → 404 `PRIMARY_KEY_NOT_FOUND`
- [ ] Teenusel on automaattestid
