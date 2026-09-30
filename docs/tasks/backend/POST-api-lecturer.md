# Uue koolitaja lisamine

**Teenus:** `POST /api/lecturer`

**Kasutav vaade:** `LecturerFormView.vue` (`/lecturer-form`, `state: "new-lecturer"`)

> Mockupi pilt lisatakse hiljem. Seni vt läbimängu `docs/mock-wireframe/loo-mock-vaade/admin-lecturers-view/admin-lecturers-view-labimang.html` ja märkmeid `docs/mock-wireframe/markmed/lecturer-form-view-state-new-lecturer-markmed.md`.

Sama muster nagu `POST-api-training.md` (koolitus luuakse koos põhikeele tõlkega). Eeldab taski `lecturer-db-changes.md`.

> **Uuendus (2026-09-30):** pilt **normaliseeritakse** (ruut, 400×400, JPEG, EXIF eemaldatud; `content_type = 'image/jpeg'`) — ühine abiklass `GET-api-lecturer-lecturerId-photo.md`-s.

## Sisend

**Request body:** `LecturerCreateRequestDto` (ettepanek).

| Väli | Tüüp | Kohustuslik | Kirjeldus |
|---|---|---|---|
| `userId` | Integer | jah | Sisselogitud kasutaja (localStorage'ist) → `lecturer.created_by` |
| `fullName` | String | jah, kuni 255 | Täisnimi (`lecturer.full_name`) |
| `photo` | String | ei | Pilt Base64 kujul või `null` |
| `photoContentType` | String | kui `photo` on antud | `image/png`, `image/jpeg` või `image/webp` |
| `title` | String | jah, kuni 255 | Ametinimetus põhikeeles |
| `shortDescription` | String | jah, kuni 255 | Lühikirjeldus põhikeeles |
| `description` | String (HTML) | jah | Kirjeldus põhikeeles (`@HtmlNotBlank`) |

```json
{
  "userId": 1,
  "fullName": "Kristjan Kuusk",
  "photo": "iVBORw0KGgoAAAANSUhEUgAAACAAAAAgCAIAAAD8GO2jAAAAaElEQVR42mO48/w3TRHDqAWjFsCQfv49NERNCzBNJ94OBrJNJ9IOBkpMJ8aOUQsGvQU0T0X0yAf0yMmjpekAW0DDZEpMLiNoDQOFRhO0hoFapuOyg+4WUGI6VjvoawHlpmPaMWoBQQsAjEBZdEobAXMAAAAASUVORK5CYII=",
  "photoContentType": "image/png",
  "title": "Andmeinsener",
  "shortDescription": "Koolitab SQL-i ja andmeanalüüsi teemadel.",
  "description": "<p>Kristjan on ehitanud andmelaohooneid ja aruandlussüsteeme.</p>"
}
```

## Väljund

**Response (200 OK):** `LecturerCreateResponseDto` (ettepanek).

```json
{
  "lecturerId": 10,
  "lecturerTranslationId": 16
}
```

Näidise ID-d eeldavad seed-andmeid (`lecturer-db-changes.md`: 9 koolitajat, 15 tõlget). Vastuse järgi teeb frontend `router.replace(?lecturerId=10&lecturerTranslationId=16)` → `state: "update"`.

**Ühes transaktsioonis** (`@Transactional`):

1. `lecturer` — `full_name`, `status = "A"` (`LecturerStatus.ACTIVE`), `created_by = userId`.
2. `lecturer_photo` — ainult kui `photo ≠ null`: Base64 dekodeeritakse (`Base64.getDecoder()`), salvestatakse koos `content_type`-iga.
3. `lecturer_translation` — põhikeeles (`LanguageService.getMainLanguage()`): `title`, `short_description`, `description` (puhastatud `HtmlSanitizer`-iga, nagu koolituse kirjeldus).

**Pildi kontroll** (enne salvestamist; sama kontroll `PUT-api-lecturer-lecturerId.md`-s — tee see ühiseks meetodiks):
- `photoContentType` peab olema `image/png`, `image/jpeg` või `image/webp`;
- dekodeeritud pilt ≤ 2 MB (2 × 1024 × 1024 baiti);
- vigane Base64 → 400 `INCORRECT_INPUT`.

Sama nimega koolitajat ei keelata. `created_at` / `updated_at` täidab auditeerimine.

## Eesmärk

Admin lisab navbari menüüst "Admin" → "Lisa uus koolitaja" (või `AdminLecturersView` nupust) uue koolitaja koos põhikeele tõlke ja soovi korral pildiga. Pärast lisamist avaneb vorm muutmise olekus, kust saab lisada teiste keelte tõlked (`POST-api-lecturer-lecturerId-lecturer-translation.md`).

## Seotud andmebaasi tabelid

Vt `docs/database/2_create.sql` ja `lecturer-db-changes.md`: `lecturer`, `lecturer_photo`, `lecturer_translation`, `language` (põhikeel), `"user"` (`created_by`).

## Veaolukorrad

| Olukord | Status code | Response body |
|---|---|---|
| Pildi tüüp pole lubatud | 403 Forbidden | `{ "message": "Lubatud on ainult PNG, JPEG või WebP pilt", "errorCode": "PHOTO_TYPE_NOT_ALLOWED" }` |
| Pilt on suurem kui 2 MB | 403 Forbidden | `{ "message": "Pilt on liiga suur, lubatud kuni 2 MB", "errorCode": "PHOTO_TOO_LARGE" }` |
| `userId` ei leidu andmebaasist | 404 Not Found | `{ "message": "Ei leidnud primary keyd 'userId' väärtusega: 123", "errorCode": "PRIMARY_KEY_NOT_FOUND" }` |
| Kohustuslik väli puudub, on liiga pikk, `description` on tühi HTML või `photo` on vigane Base64 | 400 Bad Request | `{ "message": "<väli>: <valideerimise teade>", "errorCode": "INCORRECT_INPUT" }` |
| Ootamatu serveripoolne viga | 500 Internal Server Error | Standardne vea response body (vastavalt projekti globaalsele error handler'ile) |

Kaks esimest rida on mockupi märkmetest; uued `Error` enumi väärtused `PHOTO_TYPE_NOT_ALLOWED`, `PHOTO_TOO_LARGE` (`ForbiddenException`). 404 ja 400 tulenevad olemasolevast mustrist (`getValid<Entiteet>By`, `RestExceptionHandler.handleMethodArgumentNotValid`).

## Vastuvõtu kriteeriumid

- [ ] Endpoint `POST /api/lecturer` on olemas ja tagastab `lecturerId` ning `lecturerTranslationId`
- [ ] Luuakse `lecturer` (`status = "A"`, `created_by`), põhikeele `lecturer_translation` ja pildi korral `lecturer_photo`
- [ ] Kõik ühes transaktsioonis (vea korral ei jää poolikut koolitajat)
- [ ] `description` puhastatakse `HtmlSanitizer`-iga; tühi HTML → 400
- [ ] Pildita koolitaja: `lecturer_photo` rida ei looda
- [ ] Vale pildi tüüp → 403 `PHOTO_TYPE_NOT_ALLOWED`; üle 2 MB → 403 `PHOTO_TOO_LARGE`
- [ ] Olematu `userId` → 404; puuduvad kohustuslikud väljad → 400 `INCORRECT_INPUT`
- [ ] Teenusel on automaattestid
