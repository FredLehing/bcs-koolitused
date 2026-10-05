# Koolitaja kaardi andmed

**Teenus:** `GET /api/lecturer-summary/{lecturerId}?contentLang={contentLang}`

**Seis:** olemasolev üksiku koolitaja koondteenus jääb alles. `/course` ja
`/training` ei kutsu seda enam kaartide jaoks: `LecturerCard` saab andmed
vaate koondvastuse `lecturers` massiivist. DTO `LecturerSummaryDto` asub
jagatud `controller/common/dto` paketis. Pildid laaditakse eraldi olemasolevast
`GET /api/lecturer/{lecturerId}/photo?v={photoVersion}` teenusest.

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

404: `LecturerService.getValidActiveLecturerBy(lecturerId, "lecturerId")`. Koondteenused filtreerivad kustutatud koolitajad enne kaardiandmete koostamist; kaart ise seda teenust ei kutsu.

## Vastuvõtu kriteeriumid

- [ ] Endpoint `GET /api/lecturer-summary/{lecturerId}` on olemas ja tagastab `LecturerSummaryDto`
- [ ] `title` ja `shortDescription` on `contentLang` keeles, puudumisel põhikeeles
- [ ] Pilt Base64 kujul koos `photoContentType`-iga; pildita koolitajal mõlemad `null`
- [ ] Vastuses pole `description` välja
- [ ] Kustutatud või olematu koolitaja → 404 `PRIMARY_KEY_NOT_FOUND`
- [ ] Teenusel on automaattestid
