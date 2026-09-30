# Koolitaja ühe tõlke andmed

**Teenus:** `GET /api/lecturer-translation/{lecturerTranslationId}`

**Kasutav vaade:** `LecturerFormView.vue` (`state: "update"` — avatud tõlge; `state: "new-translation"` — põhikeele tõlge eeltäitmiseks)

> Mockupi pilt lisatakse hiljem. Seni vt läbimängu `docs/mock-wireframe/loo-mock-vaade/admin-lecturers-view/admin-lecturers-view-labimang.html` ja märkmeid `docs/mock-wireframe/markmed/lecturer-form-view-state-update-markmed.md`.

Sama muster nagu `GET-api-training-translation-trainingTranslationId.md`. Eeldab taske `lecturer-db-changes.md` ja `lecturer-deleted-status.md`.

## Sisend

**Path variable:**

| Nimi | Tüüp | Kirjeldus |
|---|---|---|
| `lecturerTranslationId` | Integer | Tõlke ID (`lecturer_translation.id`) |

Query parameetreid ja request body't pole.

## Väljund

**Response (200 OK):** `LecturerTranslationDto` (ettepanek).

```json
{
  "lecturerTranslationId": 1,
  "lecturerId": 1,
  "languageId": 1,
  "languageCode": "et",
  "title": "Lektor/konsultant",
  "shortDescription": "Tarkvaraarendus, Java, HTML, CSS, JavaScript, SQL, Git, Spring Boot, REST API, PostgreSQL, JPA (Hibernate), MapStruct, JUnit, Swagger, Gradle, Confluence, Jira. Vali Tarkvaraarendus! programm.",
  "description": "<p>Tarkvaraarendus, Java, HTML, CSS, JavaScript, SQL, Git, Spring Boot, REST API, PostgreSQL, JPA (Hibernate), MapStruct, JUnit, Swagger, Gradle, Confluence, Jira. Vali Tarkvaraarendus! programm.</p>"
}
```

- `title` — ametinimetus; `shortDescription` — koolitaja kaardi tekst; `description` — HTML (rich text).
- `description` tagastatakse puhastatud kujul (`HtmlSanitizer`, sama nagu `TrainingTranslationDto.description` — vt `training-description-html-sanitize.md`).

## Eesmärk

Muutmise olekus laaditakse avatud tõlke väljad kaardile "Tõlge ({keel})". Uue tõlke olekus laaditakse põhikeele tõlge (ID `GET /api/lecturer/{lecturerId}/lecturer-translations` vastusest, `isMainLanguage = true`), millega eeltäidetakse ametinimetus, lühikirjeldus ja kirjeldus.

## Seotud andmebaasi tabelid

Vt `docs/database/2_create.sql` ja `lecturer-db-changes.md`: `lecturer_translation`, `language`, `lecturer` (`status`).

Näidisandmed: tõlked 1–15 (`lecturer-db-changes.md`, seed). Tõlge 6 kuulub kustutatud koolitajale (Virve Räni) → 404.

## Veaolukorrad

| Olukord | Status code | Response body |
|---|---|---|
| `lecturerTranslationId` ei leidu või tõlke koolitaja on kustutatud | 404 Not Found | `{ "message": "Ei leidnud primary keyd 'lecturerTranslationId' väärtusega: 123", "errorCode": "PRIMARY_KEY_NOT_FOUND" }` |
| Ootamatu serveripoolne viga | 500 Internal Server Error | Standardne vea response body (vastavalt projekti globaalsele error handler'ile) |

404: uus `getValidLecturerTranslationBy(Integer lecturerTranslationId)` (nt `LecturerTranslationService`), mis kontrollib ka koolitaja aktiivsust — sama reegel nagu kustutatud koolituse tõlkel (`training-deleted-status.md`).

## Vastuvõtu kriteeriumid

- [ ] Endpoint `GET /api/lecturer-translation/{lecturerTranslationId}` on olemas ja tagastab `LecturerTranslationDto`
- [ ] Väljad `title`, `shortDescription`, `description` ja `languageCode` on õiged
- [ ] Olematu tõlge või kustutatud koolitaja tõlge → 404 `PRIMARY_KEY_NOT_FOUND`
- [ ] Teenusel on automaattestid
