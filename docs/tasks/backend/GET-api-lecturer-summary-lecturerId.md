# Koolitaja kaardi andmed

**Teenus:** `GET /api/lecturer-summary/{lecturerId}?contentLang={contentLang}`

**Kasutav komponent:** `LecturerCard.vue` — vaates `TrainingView.vue` (`/training`, parem veerg "Koolitajad"; iga koolituse koolitaja kohta üks kaart). Admini kalender ja toimumiskorra vorm näitavad koolitajaid ainult nimedena.

> Mockupi pilt lisatakse hiljem. `/training` läbimängu veel pole; kaardi välimus: `admin-lecturers-view-skeemid.md` (jaotis 10 "LecturerCard"), pildilahendus `lecturers-view-skeemid.md`.

Eeldab taske `lecturer-db-changes.md` ja `lecturer-deleted-status.md`.

> **Uuendus (2026-09-30):** pilte ei tagastata Base64-na — `photo` / `photoContentType` asenduvad väljaga **`photoVersion`** (`lecturer_photo.updated_at` epoch-sekundites, `null` = pilti pole); pilt tuleb `GET-api-lecturer-lecturerId-photo.md` teenusest. Allpool olevad Base64-näited on vananenud. Sama DTO kasutab `GET-api-lecturer-summaries.md`. Teenust kasutab ainult `LecturerCard` koolituse lehel (`/training`).

## Sisend

**Path variable:**

| Nimi | Tüüp | Kirjeldus |
|---|---|---|
| `lecturerId` | Integer | Koolitaja ID (`lecturer.id`) |

**Query parameeter:**

| Nimi | Tüüp | Kohustuslik | Kirjeldus |
|---|---|---|---|
| `contentLang` | String | jah | Ametinimetuse ja lühikirjelduse keel (`"et"` / `"en"`) |

Request body't pole.

## Väljund

**Response (200 OK):** `LecturerSummaryDto` (ettepanek; nimeloogika nagu `TrainingSummaryDto`).

```json
{
  "lecturerId": 1,
  "fullName": "Rain Tüür",
  "title": "Lektor/konsultant",
  "shortDescription": "Tarkvaraarendus, Java, HTML, CSS, JavaScript, SQL, Git, Spring Boot, REST API, PostgreSQL, JPA (Hibernate), MapStruct, JUnit, Swagger, Gradle, Confluence, Jira. Vali Tarkvaraarendus! programm.",
  "photo": "iVBORw0KGgoAAAANSUhEUgAAACAAAAAgCAIAAAD8GO2jAAAAaElEQVR42mO48/w3TRHDqAWjFsCQfv49NERNCzBNJ94OBrJNJ9IOBkpMJ8aOUQsGvQU0T0X0yAf0yMmjpekAW0DDZEpMLiNoDQOFRhO0hoFapuOyg+4WUGI6VjvoawHlpmPaMWoBQQsAjEBZdEobAXMAAAAASUVORK5CYII=",
  "photoContentType": "image/png"
}
```

- `title`, `shortDescription` — `contentLang` keele tõlkest, selle puudumisel põhikeele (`language.is_main_language`) tõlkest (nt Kersti Laidvee `contentLang=en` → eestikeelne).
- `photo` (Base64) ja `photoContentType` — tabelist `lecturer_photo`; `null`, kui pilti pole (kaardil kohatäite ikoon).
- `description` (pikk kirjeldus) **ei** kuulu vastusesse.
- Koostatakse ilma view'ta: aktiivne `lecturer` + `lecturer_translation` + `lecturer_photo`.

## Eesmärk

Koolitaja kaart näitab koolituse lehel, koolituse kalendris ja toimumiskorra vormil koolitaja pilti, nime, ametinimetust ja lühikirjeldust. Komponent laeb andmed ise `lecturerId` järgi ning uuesti keele või `lecturerId` muutumisel. Pilt loetakse ainult sellest teenusest — `GET /api/admin-training/{trainingId}` pilti ei tagasta.

## Seotud andmebaasi tabelid

Vt `docs/database/2_create.sql` ja `lecturer-db-changes.md`: `lecturer`, `lecturer_translation`, `lecturer_photo`, `language`.

Näidisandmed: Rain Tüür (1) — pildiga, et + en; Merje Vaide (2) — pildita, et + en; Kersti Laidvee (3) — pildita, ainult et; Virve Räni (4) — kustutatud → 404.

## Veaolukorrad

| Olukord | Status code | Response body |
|---|---|---|
| `lecturerId` ei leidu või koolitaja on kustutatud | 404 Not Found | `{ "message": "Ei leidnud primary keyd 'lecturerId' väärtusega: 123", "errorCode": "PRIMARY_KEY_NOT_FOUND" }` |
| Ootamatu serveripoolne viga | 500 Internal Server Error | Standardne vea response body (vastavalt projekti globaalsele error handler'ile) |

404: `LecturerService.getValidActiveLecturerBy(lecturerId, "lecturerId")`. `LecturerCard` 404 korral kaarti ei kuva (üldisele veavaatele **ei** suunata).

## Vastuvõtu kriteeriumid

- [ ] Endpoint `GET /api/lecturer-summary/{lecturerId}` on olemas ja tagastab `LecturerSummaryDto`
- [ ] `title` ja `shortDescription` on `contentLang` keeles, puudumisel põhikeeles
- [ ] Pilt Base64 kujul koos `photoContentType`-iga; pildita koolitajal mõlemad `null`
- [ ] Vastuses pole `description` välja
- [ ] Kustutatud või olematu koolitaja → 404 `PRIMARY_KEY_NOT_FOUND`
- [ ] Teenusel on automaattestid
