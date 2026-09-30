# Koolitaja pilt (pilditeenus) ja pildi normaliseerimine

**Teenus:** `GET /api/lecturer/{lecturerId}/photo?v={photoVersion}`

**Kasutavad vaated:** `LecturersView.vue`, `LecturerView.vue`, `LecturerCard.vue` (`/training`), `LecturerFormView.vue` (eelvaade) — `<img src>`

> Mockupi pilt lisatakse hiljem. Seni vt läbimängu `docs/mock-wireframe/loo-mock-vaade/lecturers-view/lecturers-view-labimang.html` ja märkmeid `docs/mock-wireframe/markmed/lecturers-view-markmed.md`. Otsused: `docs/mock-wireframe/loo-mock-vaade/lecturers-view/lecturers-view-skeemid.md`, "Pildid".

Eeldab taski `lecturer-db-changes.md`.

## Sisend

| Nimi | Tüüp | Kirjeldus |
|---|---|---|
| `lecturerId` (path) | Integer | Koolitaja ID |
| `v` (query) | Long | `photoVersion` — ainult vahemälu jaoks, backend seda ei kontrolli |

## Väljund

**Response (200 OK):** pildi baidid (mitte JSON), `Content-Type` = `lecturer_photo.content_type` (normaliseeritud pildil `image/jpeg`), päis `Cache-Control: public, max-age=31536000, immutable`. Controller tagastab nt `ResponseEntity<byte[]>`. Avalik teenus.

**`photoVersion`** (muudes DTO-des) = `lecturer_photo.updated_at` epoch-sekundites või `null`, kui pilti pole; frontend koostab URL-i `{API}/lecturer/{lecturerId}/photo?v={photoVersion}`. Pildi vahetamisel muutub versioon → uus URL → vana pilti vahemälust ei näidata.

### Pildi normaliseerimine (ühine abiklass, kasutavad `POST-api-lecturer.md` ja `PUT-api-lecturer-lecturerId.md`)

Üleslaadimisel: kontroll (tüüp PNG / JPEG / WebP, ≤ 2 MB, korrektne Base64) → lõika keskelt ruuduks → vähenda **400×400** px → kodeeri **JPEG**-iks (nt teek Thumbnailator või `ImageIO`). Uuesti kodeerimine eemaldab EXIF-metaandmed (nt GPS). Tulemus ~30–60 KB, `content_type = 'image/jpeg'`. WebP lugemiseks võib `ImageIO` vajada lisapistikut (nt TwelveMonkeys) — kui see on liiga keeruline, jäta WebP lubatud tüüpidest välja ja uuenda veateksti.

## Eesmärk

Pildid laetakse `<img src>`-i kaudu eraldi (brauser paneb vahemällu), mitte Base64-na JSON-i sees — nimekirjad jäävad kergeks.

## Seotud andmebaasi tabelid

`lecturer_photo` (`photo`, `content_type`, `updated_at`), `lecturer` (`status`). Näidisandmed: Rain Tüür (1) — `rain-tuur.jpg`; teistel pilti pole → 404.

## Veaolukorrad

| Olukord | Status code | Response body |
|---|---|---|
| Koolitajat pole, ta on kustutatud või tal pole pilti | 404 Not Found | `{ "message": "Ei leidnud primary keyd 'lecturerId' väärtusega: 123", "errorCode": "PRIMARY_KEY_NOT_FOUND" }` |
| Ootamatu serveripoolne viga | 500 Internal Server Error | Standardne vea response body (vastavalt projekti globaalsele error handler'ile) |

## Vastuvõtu kriteeriumid

- [ ] Endpoint tagastab pildi baidid õige `Content-Type`-i ja `Cache-Control` päisega
- [ ] Pildita / kustutatud / olematu koolitaja → 404
- [ ] Normaliseerimise abiklass (ruut, 400×400, JPEG) on olemas ja testitud
- [ ] Teenusel on automaattestid
