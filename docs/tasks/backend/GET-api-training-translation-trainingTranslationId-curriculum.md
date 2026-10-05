# Koolituse õppekava allalaadimine ja failinimi tõlke andmetes

**Teenused:** `GET /api/training-translation/{trainingTranslationId}/curriculum` (uus), `GET /api/training-translation/{trainingTranslationId}` (muudetakse — vastusesse kaks välja)

**Kasutav vaade:** `TrainingFormView.vue` (`/training-form?trainingId={id}&trainingTranslationId={id}`, `state: "update"`); hiljem `TrainingView.vue` (`/training`)

> Mockupi pilt lisatakse hiljem. Seni vt plaani `docs/mock-wireframe/loo-mock-vaade/form-view/training-curriculum-plaan.md`, märkmeid `docs/mock-wireframe/markmed/training-form-view-state-update-markmed.md` (API märkmed `GET /api/training-translation/{trainingTranslationId}` ja `…/curriculum`) ja läbimängu `docs/mock-wireframe/loo-mock-vaade/form-view/training-form-view-labimang.html` (artifact https://claude.ai/artifact/3VjRgmnQK7Ck7qWVHuRK9b).

Eeldab taski `training-curriculum-db-changes.md`. Testimiseks on vaja ka `training-curriculum-upload.md` (seed'is õppekavasid pole) — teha võib enne või pärast seda.

## Sisend

| Nimi | Tüüp | Kirjeldus |
|---|---|---|
| `trainingTranslationId` (path) | Integer | Tõlke ID |

Request body puudub.

## Väljund

### 1. `GET /api/training-translation/{trainingTranslationId}/curriculum` (uus)

**Response (200 OK):** faili baidid (mitte JSON), controller tagastab `ResponseEntity<byte[]>` (nagu `GET /api/lecturer/{lecturerId}/photo`):

| Päis | Väärtus |
|---|---|
| `Content-Type` | `application/pdf` |
| `Content-Disposition` | `attachment; filename="java-algkursus-oppekava.pdf"` — `file_name` veerust, Springi `ContentDisposition.attachment().filename(...)` |
| `Cache-Control` | `no-cache` — fail võib sama URL-i all vahetuda |

Controller'i koht: `TrainingTranslationController`.

**Ligipääs:** backendis autentimist pole, seega on fail kättesaadav nii mustandi (`U`) kui publitseeritud (`P`) koolitusel — vorm peab saama ka mustandi faili. Avalik `/training` vaade näitab niikuinii ainult publitseeritud koolitusi. Piirang ("mustand ainult adminile") lisatakse koos autentimisega.

### 2. `GET /api/training-translation/{trainingTranslationId}` (muudetakse)

`TrainingTranslationDto`-sse lisanduvad väljad:

| Väli | Tüüp | Kirjeldus |
|---|---|---|
| `curriculumFileName` | String / `null` | `training_translation_curriculum.file_name`; `null` = õppekava pole |
| `curriculumFileSize` | Integer / `null` | `file_size` baitides; `null` = õppekava pole |

```json
{
  "trainingTranslationId": 1,
  "trainingId": 1,
  "languageId": 1,
  "languageCode": "et",
  "title": "Java algkursus",
  "shortDescription": "Java programmeerimise alused algajatele.",
  "description": "<p>Kursusel õpitakse Java süntaksit, objektorienteeritud programmeerimist ja põhilisi andmestruktuure.</p>",
  "curriculumFileName": "java-algkursus-oppekava.pdf",
  "curriculumFileSize": 846213
}
```

Nimi ja suurus loetakse päringuga, mis **ei loe `file` baite** (vt repository `training-curriculum-db-changes.md`-s). Olemasolev 404 kustutatud koolituse korral jääb.

## Eesmärk

Admin näeb koolituse vormis avatud tõlke õppekava failinime ja suurust ning saab faili alla laadida (klikk failinimel). Hiljem kasutab sama allalaadimise teenust avalik koolituse vaade.

## Seotud andmebaasi tabelid

Vt `docs/database/2_create.sql`: `training_translation_curriculum` (`file`, `file_name`, `file_size`), `training_translation`, `training` (`status`). Seed'is õppekavasid pole — lisa enne testimist üks fail `PUT /api/training/1`-ga (`training-curriculum-upload.md`).

## Veaolukorrad

| Olukord | Status code | Response body |
|---|---|---|
| `trainingTranslationId` ei leidu, tõlke koolitus on kustutatud (`D`) või tõlkel pole õppekava (ainult `…/curriculum`) | 404 Not Found | `{ "message": "Ei leidnud primary keyd 'trainingTranslationId' väärtusega: 123", "errorCode": "PRIMARY_KEY_NOT_FOUND" }` |
| Ootamatu serveripoolne viga | 500 Internal Server Error | Standardne vea response body (vastavalt projekti globaalsele error handler'ile) |

404 tuleneb olemasolevast mustrist (`TrainingTranslationService.getTrainingTranslation`, pildil `getValidLecturerPhotoBy`).

## Vastuvõtu kriteeriumid

- [ ] `GET …/curriculum` tagastab faili baidid, `Content-Type: application/pdf`, `Content-Disposition` salvestatud failinimega ja `Cache-Control: no-cache`
- [ ] Brauseris URL-i avamine laadib faili alla õige nimega
- [ ] Õppekavata tõlge, olematu tõlge või kustutatud koolitus → 404
- [ ] `GET /api/training-translation/{id}` tagastab `curriculumFileName` ja `curriculumFileSize` (õppekavata `null`), `file` baite ei loeta
- [ ] Teenustel on automaattestid
