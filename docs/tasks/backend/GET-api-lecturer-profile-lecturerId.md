# Koolitaja avalik profiil

**Teenus:** `GET /api/lecturer-profile/{lecturerId}?contentLang={contentLang}`

**Kasutav vaade:** `LecturerView.vue` (`/lecturer?lecturerId={id}`)

> Mockupi pilt lisatakse hiljem. Seni vt läbimängu `docs/mock-wireframe/loo-mock-vaade/lecturers-view/lecturers-view-labimang.html` ja märkmeid `docs/mock-wireframe/markmed/lecturer-view-markmed.md` (JSON-näide).

Eeldab taske `lecturer-db-changes.md`, `training-lecturers-multiple.md`, `GET-api-lecturer-lecturerId-photo.md`.

## Sisend

| Nimi | Tüüp | Kirjeldus |
|---|---|---|
| `lecturerId` (path) | Integer | Koolitaja ID |
| `contentLang` (query) | String | Tekstide ja koolituste nimede keel |

## Väljund

**Response (200 OK):** `LecturerProfileDto` — `lecturerId`, `fullName`, `title`, `shortDescription`, `description` (HTML, `HtmlSanitizer`), `photoVersion`, `trainings: [{ trainingId, trainingTranslationId, title }]`.
- Tekstid `contentLang` keeles, puudumisel põhikeeles.
- `trainings` = **publitseeritud** koolitused (`status = 'P'`), mille koolitajate hulgas ta on (`training_lecturer`); `title` ja `trainingTranslationId` `contentLang` keeles, puudumisel põhikeeles; sorteeritud `title` järgi; tühi list, kui koolitusi pole.
- Tulevasi toimumiskordi **ei** tagastata (otsus: praegu ei).

## Eesmärk

Koolitaja detailvaade: suur pilt, nimi, amet, lühikirjeldus, kirjeldus ja lingid tema koolitustele.

## Seotud andmebaasi tabelid

`lecturer`, `lecturer_translation`, `lecturer_photo`, `training_lecturer`, `training`, `training_translation`. Näidisandmed: Rain Tüür → Git ja GitHub, Java algkursus, SQL ja andmebaasid, Spring Boot veebiarendus; Margus Sakk → tühi list (tema koolitused on mustandid).

## Veaolukorrad

| Olukord | Status code | Response body |
|---|---|---|
| Koolitajat pole või ta on kustutatud | 404 Not Found | `{ "message": "Ei leidnud primary keyd 'lecturerId' väärtusega: 123", "errorCode": "PRIMARY_KEY_NOT_FOUND" }` |
| Ootamatu serveripoolne viga | 500 Internal Server Error | Standardne vea response body (vastavalt projekti globaalsele error handler'ile) |

## Vastuvõtu kriteeriumid

- [ ] Endpoint tagastab profiili koos puhastatud kirjeldusega
- [ ] `trainings` ainult publitseeritud, `training_lecturer` kaudu, nime järgi
- [ ] Kustutatud / olematu koolitaja → 404
- [ ] Teenusel on automaattestid
