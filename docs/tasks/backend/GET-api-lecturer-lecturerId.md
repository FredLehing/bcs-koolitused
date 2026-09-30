# Koolitaja andmed (nimi ja pilt) vormi jaoks

**Teenus:** `GET /api/lecturer/{lecturerId}`

**Kasutav vaade:** `LecturerFormView.vue` (`/lecturer-form?lecturerId={id}&lecturerTranslationId={id}`, `state: "update"`; `/lecturer-form?lecturerId={id}&languageId={id}`, `state: "new-translation"`)

> Mockupi pilt lisatakse hiljem. Seni vt läbimängu `docs/mock-wireframe/loo-mock-vaade/admin-lecturers-view/admin-lecturers-view-labimang.html` ja märkmeid `docs/mock-wireframe/markmed/lecturer-form-view-state-update-markmed.md`.

Eeldab taske `lecturer-db-changes.md` ja `lecturer-deleted-status.md` (`getValidActiveLecturerBy`).

> **Uuendus (2026-09-30):** pilte ei tagastata Base64-na — `photo` / `photoContentType` asenduvad väljaga **`photoVersion`** (`lecturer_photo.updated_at` epoch-sekundites, `null` = pilti pole); pilt tuleb `GET-api-lecturer-lecturerId-photo.md` teenusest. Allpool olevad Base64-näited on vananenud. Näide: `{ "lecturerId": 1, "fullName": "Rain Tüür", "photoVersion": 1784095200 }`.

## Sisend

**Path variable:**

| Nimi | Tüüp | Kirjeldus |
|---|---|---|
| `lecturerId` | Integer | Koolitaja ID (`lecturer.id`) |

Query parameetreid ja request body't pole.

## Väljund

**Response (200 OK):** `LecturerDetailDto` (ettepanek; nimi erineb olemasolevast nimekirja `LecturerDto`-st).

```json
{
  "lecturerId": 1,
  "fullName": "Rain Tüür",
  "photo": "iVBORw0KGgoAAAANSUhEUgAAACAAAAAgCAIAAAD8GO2jAAAAaElEQVR42mO48/w3TRHDqAWjFsCQfv49NERNCzBNJ94OBrJNJ9IOBkpMJ8aOUQsGvQU0T0X0yAf0yMmjpekAW0DDZEpMLiNoDQOFRhO0hoFapuOyg+4WUGI6VjvoawHlpmPaMWoBQQsAjEBZdEobAXMAAAAASUVORK5CYII=",
  "photoContentType": "image/png"
}
```

Pildita koolitaja (nt Merje Vaide):

```json
{
  "lecturerId": 2,
  "fullName": "Merje Vaide",
  "photo": null,
  "photoContentType": null
}
```

- `photo` — `lecturer_photo.photo` **Base64** kujul (`Base64.getEncoder().encodeToString(bytes)`, mitte `StringBytesConverter`); `null`, kui `lecturer_photo` rida puudub.
- `photoContentType` — `lecturer_photo.content_type`; `null`, kui pilti pole.
- Frontend kuvab eelvaate kujul `data:{photoContentType};base64,{photo}` ja saadab sama väärtuse `PUT /api/lecturer/{lecturerId}`-ga tagasi, kui pilti ei muudetud.
- Tõlkeid see teenus ei tagasta (vt `GET-api-lecturer-translation-lecturerTranslationId.md`).

## Eesmärk

`LecturerFormView` muutmise ja uue tõlke olekus laaditakse kaardi "Koolitaja andmed" väljad: täisnimi ja pildi eelvaade. Uue tõlke olekus on need kirjutuskaitstud. Koolituse vormi analoog on `GET /api/training/{trainingId}`.

## Seotud andmebaasi tabelid

Vt `docs/database/2_create.sql` ja `lecturer-db-changes.md`: `lecturer` (`full_name`, `status`), `lecturer_photo` (`photo`, `content_type`).

Näidisandmed: Rain Tüür (1) — pildiga (PNG); Merje Vaide (2), Kersti Laidvee (3) — pildita; Virve Räni (4) — kustutatud → 404.

## Veaolukorrad

| Olukord | Status code | Response body |
|---|---|---|
| `lecturerId` ei leidu või koolitaja on kustutatud (`status = 'D'`) | 404 Not Found | `{ "message": "Ei leidnud primary keyd 'lecturerId' väärtusega: 123", "errorCode": "PRIMARY_KEY_NOT_FOUND" }` |
| Ootamatu serveripoolne viga | 500 Internal Server Error | Standardne vea response body (vastavalt projekti globaalsele error handler'ile) |

404 vastab mustrile `LecturerService.getValidActiveLecturerBy(lecturerId, "lecturerId")`. Frontend suunab 404 korral üldisele veavaatele.

## Vastuvõtu kriteeriumid

- [ ] Endpoint `GET /api/lecturer/{lecturerId}` on olemas ja tagastab `LecturerDetailDto`
- [ ] Pildiga koolitaja: `photo` on korrektne Base64, `photoContentType` = `"image/png"`
- [ ] Pildita koolitaja: `photo` ja `photoContentType` on `null`
- [ ] Kustutatud või olematu koolitaja → 404 `PRIMARY_KEY_NOT_FOUND`
- [ ] Teenusel on automaattestid
