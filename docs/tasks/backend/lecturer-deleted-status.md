# Kustutatud koolitaja peitmine olemasolevates teenustes

**Teenused (olemasolevad, muudetakse):**

- `GET /api/lecturers`
- `POST /api/training`
- `PUT /api/training/{trainingId}`

**Kasutavad vaated:** `TrainingFormView.vue` (`/training-form`, "Vali koolitaja" modal)

Taust: koolitajal tuleb soft delete (`lecturer.status = 'D'`, vt `lecturer-db-changes.md` ja `DELETE-api-lecturer-lecturerId.md`). Otsused: `docs/mock-wireframe/loo-mock-vaade/admin-lecturers-view/admin-lecturers-view-skeemid.md`, jaotis "Kustutatud koolitaja teistes teenustes".

## Sisend

Teenuste sisendid ei muutu.

## Väljund

Õnnestunud vastused ei muutu. Muutub, **milliseid koolitajaid teenused leiavad**:

1. **`GET /api/lecturers`** ("Vali koolitaja" modal) tagastab ainult aktiivsed koolitajad (`status = 'A'`). `LecturerRepository.findLecturersBy` päringusse lisatakse staatuse tingimus.
2. **`POST /api/training` ja `PUT /api/training/{trainingId}`**: kustutatud koolitajat ei saa `defaultLecturerId`-ks valida → `404 PRIMARY_KEY_NOT_FOUND` (sama sõnum nagu olematu ID korral).
   - `LecturerService`-isse lisatakse olemasoleva `getValidLecturerBy(Integer lecturerId, String fieldName)` kõrvale `getValidActiveLecturerBy(Integer lecturerId, String fieldName)`, mis leiab ainult aktiivse koolitaja. `TrainingService.handleSetDefaultLecturer` kasutab seda.
   - **`getValidLecturerBy` jääb alles** — seda kasutavad `DELETE /api/lecturer/{lecturerId}` ja `PUT /api/lecturer/{lecturerId}/restore`, mis peavad leidma ka kustutatud koolitaja.
3. **Olemasolevad seosed jäävad alles.** Kui koolitus viitab juba kustutatud koolitajale (`default_lecturer_id`), tagastab `GET /api/training/{trainingId}` tema nime edasi. Vorm saadab `PUT`-iga sama `defaultLecturerId` tagasi — kui see aktiivsuse kontrolli tõttu ebaõnnestuks, ei saaks koolituse muid välju enam salvestada. Seetõttu kontrollib `PUT` aktiivsust ainult siis, kui `defaultLecturerId` **muutub** (erineb koolituse praegusest vaikimisi koolitajast).

Uued koolitaja teenused (`GET/PUT /api/lecturer/{lecturerId}`, tõlked, AI tõlge, `lecturer-summary`) kasutavad aktiivse koolitaja meetodit juba oma taskides. `course` teenuste (`lecturerId`) reegel lisatakse kalendri taskidesse.

## Eesmärk

Kustutatud koolitajat ei tohi saada uutele koolitustele valida ega "Vali koolitaja" modalis näha. Varasemad koolitused, kus ta oli vaikimisi koolitaja, jäävad muutmata ja neid saab edasi muuta.

## Seotud andmebaasi tabelid

Vt `docs/database/2_create.sql` ja `lecturer-db-changes.md`: `lecturer` (`status`), `training` (`default_lecturer_id`).

Näidisandmed: Virve Räni (4) on kustutatud (`D`); ükski koolitus temale ei viita.

## Veaolukorrad

| Olukord | Status code | Response body |
|---|---|---|
| `POST /api/training`: `defaultLecturerId` on kustutatud koolitaja | 404 Not Found | `{ "message": "Ei leidnud primary keyd 'defaultLecturerId' väärtusega: 4", "errorCode": "PRIMARY_KEY_NOT_FOUND" }` |
| `PUT /api/training/{trainingId}`: `defaultLecturerId` **muudetakse** kustutatud koolitajaks | 404 Not Found | sama |
| Ootamatu serveripoolne viga | 500 Internal Server Error | Standardne vea response body (vastavalt projekti globaalsele error handler'ile) |

404 tuleneb olemasolevast mustrist (`PrimaryKeyNotFoundException`), mitte mockupist.

## Vastuvõtu kriteeriumid

- [ ] `GET /api/lecturers` ei tagasta kustutatud koolitajaid (Virve Räni puudub)
- [ ] `LecturerService.getValidActiveLecturerBy` on olemas; `getValidLecturerBy` jääb alles
- [ ] `POST /api/training` kustutatud `defaultLecturerId`-ga → 404
- [ ] `PUT /api/training/{trainingId}` uue kustutatud `defaultLecturerId`-ga → 404; sama (juba seotud) kustutatud koolitajaga salvestamine õnnestub
- [ ] Teenustel on automaattestid
