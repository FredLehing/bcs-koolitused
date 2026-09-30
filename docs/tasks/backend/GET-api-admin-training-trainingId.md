# Ühe koolituse admini ülevaade (kalendri koolituse kaart)

**Teenus:** `GET /api/admin-training/{trainingId}?contentLang={contentLang}`

**Kasutavad vaated:** `AdminTrainingCoursesView.vue` (koolituse kaart ja kirjeldus), `CourseFormView.vue` (koolituse nimi, uue toimumiskorra koolitajad)

> Mockupi pilt lisatakse hiljem. Seni vt läbimängu `docs/mock-wireframe/loo-mock-vaade/admin-training-courses-view/admin-training-courses-view-labimang.html` ja märkmeid `docs/mock-wireframe/markmed/admin-training-courses-view-markmed.md` (JSON-näited).

Eeldab taske `training-lecturers-multiple.md`, `training-deleted-status.md` (tehtud).

## Sisend

| Nimi | Tüüp | Kirjeldus |
|---|---|---|
| `trainingId` (path) | Integer | Koolituse ID |
| `contentLang` (query) | String | Nimede ja kirjelduse keel |

## Väljund

**Response (200 OK):** `AdminTrainingDto` (ettepanek) — `trainingId`, `trainingTranslationId`, `title`, `description` (HTML), `categoryName`, `trainingLanguageCode`, `trainingLanguageFlagIconCode`, `locationName`, `lecturers: [{ lecturerId, lecturerName }]` (`training_lecturer`, `sort_order` järjekorras), `status`, `isOrderable`, `isPromoted`, `fundingTypes`. JSON: `docs/mock-wireframe/markmed/admin-training-courses-view-markmed.md`. Pilte ei tagastata (kalender näitab koolitajaid ainult nimedena). Puuduva `contentLang` tõlke korral põhikeele `title`, `description`, `categoryName`, `trainingTranslationId`. Andmed: `admin_training_summary` (training_id + contentLang) + `training` (location) + `training_lecturer` + kuvatud tõlke `description`.

## Eesmärk

Kalendri ülaosas on koolituse andmed (mustand ka); toimumiskorra vorm võtab siit koolituse nime ja uue toimumiskorra vaikimisi koolitajad.

## Seotud andmebaasi tabelid

`admin_training_summary` (view), `training`, `location`, `training_lecturer`, `lecturer`, `training_translation`.

## Veaolukorrad

| Olukord | Status code | Response body |
|---|---|---|
| Koolitust pole või see on kustutatud | 404 Not Found | `{ "message": "Ei leidnud primary keyd 'trainingId' väärtusega: 123", "errorCode": "PRIMARY_KEY_NOT_FOUND" }` |
| Ootamatu serveripoolne viga | 500 Internal Server Error | Standardne vea response body (vastavalt projekti globaalsele error handler'ile) |

`getValidActiveTrainingBy` (olemas).

## Vastuvõtu kriteeriumid

- [ ] Endpoint tagastab `AdminTrainingDto` (ka mustandi)
- [ ] Põhikeele varuvariant; `lecturers` järjekorras
- [ ] Kustutatud / olematu → 404
- [ ] Teenusel on automaattestid
