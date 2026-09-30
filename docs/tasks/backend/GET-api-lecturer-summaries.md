# Avalik koolitajate nimekiri (kaardid)

**Teenus:** `GET /api/lecturer-summaries?contentLang={contentLang}`

**Kasutav vaade:** `LecturersView.vue` (`/lecturers`)

> Mockupi pilt lisatakse hiljem. Seni vt läbimängu `docs/mock-wireframe/loo-mock-vaade/lecturers-view/lecturers-view-labimang.html` ja märkmeid `docs/mock-wireframe/markmed/lecturers-view-markmed.md` (JSON-näide).

Eeldab taske `lecturer-db-changes.md`, `GET-api-lecturer-lecturerId-photo.md` (photoVersion).

## Sisend

| Nimi | Tüüp | Kohustuslik | Kirjeldus |
|---|---|---|---|
| `contentLang` | String | jah | Ameti ja lühikirjelduse keel (`"et"` / `"en"`) |

## Väljund

**Response (200 OK):** `List<LecturerSummaryDto>` — sama DTO nagu `GET-api-lecturer-summary-lecturerId.md` (`lecturerId`, `fullName`, `title`, `shortDescription`, `photoVersion`). Ainult aktiivsed (`status = 'A'`), sorteeritud `fullName` järgi. `title` / `shortDescription` `contentLang` keeles, puudumisel põhikeeles. Pilti ega `description`-it ei tagastata. Tühi list, kui koolitajaid pole.

## Eesmärk

Avalik leht "Meie koolitajad" (navbari põhimenüü link): kaardid pildi, nime, ameti ja lühikirjeldusega; kogu kaart viib detailvaatesse.

## Seotud andmebaasi tabelid

`lecturer`, `lecturer_translation`, `lecturer_photo` (ainult `updated_at`), `language`. Näidisandmed: 8 aktiivset koolitajat (Virve Räni on kustutatud).

## Veaolukorrad

| Olukord | Status code | Response body |
|---|---|---|
| Ootamatu serveripoolne viga | 500 Internal Server Error | Standardne vea response body (vastavalt projekti globaalsele error handler'ile) |

Tundmatu `contentLang` → põhikeele tekstid (sama reegel nagu lecturer-summary).

## Vastuvõtu kriteeriumid

- [ ] Endpoint tagastab aktiivsed koolitajad nime järgi
- [ ] Tekstid `contentLang` keeles, puudumisel põhikeeles; `photoVersion` õige või `null`
- [ ] Pilte ega kirjeldust vastuses pole
- [ ] Teenusel on automaattestid
