# Koolitaja uue tõlke lisamine

**Teenus:** `POST /api/lecturer/{lecturerId}/lecturer-translation`

**Kasutav vaade:** `LecturerFormView.vue` (`/lecturer-form?lecturerId={id}&languageId={id}`, `state: "new-translation"`)

> Mockupi pilt lisatakse hiljem. Seni vt läbimängu `docs/mock-wireframe/loo-mock-vaade/admin-lecturers-view/admin-lecturers-view-labimang.html` ja märkmeid `docs/mock-wireframe/markmed/lecturer-form-view-state-new-translation-markmed.md`.

Sama muster nagu `POST-api-training-trainingId-training-translation.md`. Eeldab taske `lecturer-db-changes.md` ja `lecturer-deleted-status.md`.

## Sisend

**Path variable:**

| Nimi | Tüüp | Kirjeldus |
|---|---|---|
| `lecturerId` | Integer | Koolitaja ID |

**Request body:** `LecturerTranslationCreateRequestDto` (ettepanek).

| Väli | Tüüp | Kohustuslik | Kirjeldus |
|---|---|---|---|
| `languageId` | Integer | jah | Tõlke keel (`language.id`) |
| `title` | String | jah, kuni 255 | Ametinimetus |
| `shortDescription` | String | jah, kuni 255 | Lühikirjeldus |
| `description` | String (HTML) | jah | Kirjeldus (`@HtmlNotBlank`) |

```json
{
  "languageId": 2,
  "title": "Lecturer/consultant",
  "shortDescription": "Adobe Photoshop, Illustrator, InDesign, Acrobat, Canva, Figma, digital marketing, e-learning design, Office applications.",
  "description": "<p>Adobe Photoshop, Illustrator, InDesign, Acrobat, Canva, Figma, digital marketing, e-learning design, Office applications.</p>"
}
```

## Väljund

**Response (200 OK):** `LecturerTranslationCreateResponseDto` (ettepanek).

```json
{
  "lecturerTranslationId": 16
}
```

Näidis: Kersti Laidvee (`lecturerId = 3`) inglise keele tõlge; ID eeldab seed-andmeid (15 tõlget). `description` puhastatakse `HtmlSanitizer`-iga. Vastuse järgi teeb frontend `router.replace(?lecturerId=3&lecturerTranslationId=16)` → `state: "update"`.

## Eesmärk

Admin klikib `LecturerFormView`-s hallil lipul, tõlgib põhikeele tekstiga eeltäidetud ametinimetuse, lühikirjelduse ja kirjelduse (soovi korral "Tee AI tõlge") ning vajutab "Lisa tõlge". Pärast seda on koolitajal tõlge ka selles keeles ja kaart kuvatakse selles keeles.

## Seotud andmebaasi tabelid

Vt `docs/database/2_create.sql` ja `lecturer-db-changes.md`: `lecturer_translation` (unikaalne `lecturer_id` + `language_id`), `language`, `lecturer`.

Näidisandmed: Kersti Laidvee (3) — puudub `en` tõlge (proovi lisamist); Rain Tüür (1) — `en` on olemas (proovi 403).

## Veaolukorrad

| Olukord | Status code | Response body |
|---|---|---|
| `lecturerId` ei leidu või koolitaja on kustutatud | 404 Not Found | `{ "message": "Ei leidnud primary keyd 'lecturerId' väärtusega: 123", "errorCode": "PRIMARY_KEY_NOT_FOUND" }` |
| `languageId` ei leidu | 404 Not Found | `{ "message": "Ei leidnud primary keyd 'languageId' väärtusega: 123", "errorCode": "PRIMARY_KEY_NOT_FOUND" }` |
| Selles keeles tõlge on juba olemas | 403 Forbidden | `{ "message": "Selles keeles tõlge on juba olemas", "errorCode": "TRANSLATION_EXISTS" }` |
| Kohustuslik väli puudub, on liiga pikk või `description` on tühi HTML | 400 Bad Request | `{ "message": "<väli>: <valideerimise teade>", "errorCode": "INCORRECT_INPUT" }` |
| Ootamatu serveripoolne viga | 500 Internal Server Error | Standardne vea response body (vastavalt projekti globaalsele error handler'ile) |

`TRANSLATION_EXISTS` on `Error` enumis juba olemas (koolituse tõlke jaoks). 404 ja 400 tulenevad olemasolevast mustrist.

## Vastuvõtu kriteeriumid

- [ ] Endpoint `POST /api/lecturer/{lecturerId}/lecturer-translation` on olemas ja tagastab `lecturerTranslationId`
- [ ] Tõlge salvestatakse (`description` puhastatud)
- [ ] Olemasolev keel → 403 `TRANSLATION_EXISTS`
- [ ] Kustutatud / olematu koolitaja või olematu keel → 404
- [ ] Valideerimisvead → 400 `INCORRECT_INPUT`
- [ ] Teenusel on automaattestid
