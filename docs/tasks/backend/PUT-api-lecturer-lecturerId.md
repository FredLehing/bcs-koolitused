# Koolitaja ja avatud tõlke muutmine

**Teenus:** `PUT /api/lecturer/{lecturerId}`

**Kasutav vaade:** `LecturerFormView.vue` (`/lecturer-form?lecturerId={id}&lecturerTranslationId={id}`, `state: "update"`)

> Mockupi pilt lisatakse hiljem. Seni vt läbimängu `docs/mock-wireframe/loo-mock-vaade/admin-lecturers-view/admin-lecturers-view-labimang.html` ja märkmeid `docs/mock-wireframe/markmed/lecturer-form-view-state-update-markmed.md`.

Sama muster nagu `PUT-api-training-trainingId.md` (koolitus + avatud tõlge ühes päringus). Eeldab taske `lecturer-db-changes.md`, `lecturer-deleted-status.md` ja pildi kontrolli `POST-api-lecturer.md`-st.

## Sisend

**Path variable:**

| Nimi | Tüüp | Kirjeldus |
|---|---|---|
| `lecturerId` | Integer | Muudetava koolitaja ID |

**Request body:** `LecturerUpdateRequestDto` (ettepanek).

| Väli | Tüüp | Kohustuslik | Kirjeldus |
|---|---|---|---|
| `fullName` | String | jah, kuni 255 | Täisnimi |
| `photo` | String | ei | Pilt Base64 kujul; `null` = pilt eemaldatakse |
| `photoContentType` | String | kui `photo` on antud | `image/png`, `image/jpeg` või `image/webp` |
| `lecturerTranslation` | objekt | jah | Avatud tõlge |
| `lecturerTranslation.lecturerTranslationId` | Integer | jah | Tõlke ID (peab kuuluma sellele koolitajale) |
| `lecturerTranslation.title` | String | jah, kuni 255 | Ametinimetus |
| `lecturerTranslation.shortDescription` | String | jah, kuni 255 | Lühikirjeldus |
| `lecturerTranslation.description` | String (HTML) | jah | Kirjeldus (`@HtmlNotBlank`) |

```json
{
  "fullName": "Rain Tüür",
  "photo": null,
  "photoContentType": null,
  "lecturerTranslation": {
    "lecturerTranslationId": 1,
    "title": "Lektor/konsultant",
    "shortDescription": "Tarkvaraarendus, Java, Spring Boot, REST API, PostgreSQL, Git. Vali Tarkvaraarendus! programm.",
    "description": "<p>Tarkvaraarendus, Java, HTML, CSS, JavaScript, SQL, Git, Spring Boot, REST API, PostgreSQL, JPA (Hibernate), MapStruct, JUnit, Swagger, Gradle, Confluence, Jira. Vali Tarkvaraarendus! programm. Juhendab Java algkursust ja Spring Booti veebiarenduse koolitust.</p>"
  }
}
```

## Väljund

**Response (200 OK):** Tühi vastus (ainult staatuskood 200 OK).

**Ühes transaktsioonis:**

1. `lecturer.full_name` uuendatakse.
2. Pilt — **saadetakse alati praegusel kujul** (frontend saadab `GET /api/lecturer/{lecturerId}` väärtuse tagasi, kui pilti ei muudetud):
   - `photo ≠ null` → `lecturer_photo` lisatakse või asendatakse (`photo`, `content_type`); sama pildi uuesti salvestamine on lubatud;
   - `photo = null` → `lecturer_photo` rida kustutatakse (kui oli).
3. `lecturer_translation` (id = `lecturerTranslationId`) — `title`, `short_description`, `description` (puhastatud `HtmlSanitizer`-iga).

Pildi kontroll sama mis `POST-api-lecturer.md`-s (tüüp, ≤ 2 MB, korrektne Base64). `updated_at` uueneb auditeerimisega kõigis muudetud ridades.

## Eesmärk

Admin muudab `LecturerFormView` muutmise olekus koolitaja nime, pilti ("Vali pilt" / "Eemalda") ja avatud keele tõlget ning vajutab "Salvesta". Eduteade "Salvestatud"; vaade jääb samasse olekusse.

## Seotud andmebaasi tabelid

Vt `docs/database/2_create.sql` ja `lecturer-db-changes.md`: `lecturer`, `lecturer_photo`, `lecturer_translation`.

Näidisandmed: Rain Tüür (1, tõlked 1 ja 2, pildiga) — pildi eemaldamise proov; Merje Vaide (2) — pildi lisamise proov.

## Veaolukorrad

| Olukord | Status code | Response body |
|---|---|---|
| `lecturerId` ei leidu või koolitaja on kustutatud | 404 Not Found | `{ "message": "Ei leidnud primary keyd 'lecturerId' väärtusega: 123", "errorCode": "PRIMARY_KEY_NOT_FOUND" }` |
| `lecturerTranslationId` ei leidu või kuulub teisele koolitajale | 404 Not Found | `{ "message": "Ei leidnud primary keyd 'lecturerTranslationId' väärtusega: 123", "errorCode": "PRIMARY_KEY_NOT_FOUND" }` |
| Pildi tüüp pole lubatud | 403 Forbidden | `{ "message": "Lubatud on ainult PNG, JPEG või WebP pilt", "errorCode": "PHOTO_TYPE_NOT_ALLOWED" }` |
| Pilt on suurem kui 2 MB | 403 Forbidden | `{ "message": "Pilt on liiga suur, lubatud kuni 2 MB", "errorCode": "PHOTO_TOO_LARGE" }` |
| Kohustuslik väli puudub, on liiga pikk, `description` on tühi HTML või `photo` on vigane Base64 | 400 Bad Request | `{ "message": "<väli>: <valideerimise teade>", "errorCode": "INCORRECT_INPUT" }` |
| Ootamatu serveripoolne viga | 500 Internal Server Error | Standardne vea response body (vastavalt projekti globaalsele error handler'ile) |

404 read ja 400 tulenevad olemasolevast mustrist (`getValidActiveLecturerBy`, `RestExceptionHandler`); tõlke omaniku kontroll nagu `PUT /api/training/{trainingId}` puhul.

## Vastuvõtu kriteeriumid

- [ ] Endpoint `PUT /api/lecturer/{lecturerId}` on olemas, tagastab tühja vastuse
- [ ] Nimi ja avatud tõlge uuenevad ühes transaktsioonis
- [ ] `photo ≠ null` → pilt lisatakse või asendatakse; `photo = null` → `lecturer_photo` rida kustutatakse
- [ ] Sama pildi tagasisaatmine ei muuda midagi peale `updated_at`
- [ ] Teise koolitaja tõlke ID → 404 `'lecturerTranslationId'`
- [ ] Kustutatud koolitaja → 404
- [ ] Pildi vead → 403 `PHOTO_TYPE_NOT_ALLOWED` / `PHOTO_TOO_LARGE`; valideerimisvead → 400
- [ ] Teenusel on automaattestid
