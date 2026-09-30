# Kustutatud koolitaja taastamine

**Teenus:** `PUT /api/lecturer/{lecturerId}/restore`

**Kasutav vaade:** `AdminLecturersView.vue` (`/admin-lecturers`, lüliti "Näita kustutatud")

> Mockupi pilt lisatakse hiljem. Seni vt läbimängu `docs/mock-wireframe/loo-mock-vaade/admin-lecturers-view/admin-lecturers-view-labimang.html` ja märkmeid `docs/mock-wireframe/markmed/admin-lecturers-view-markmed.md`.

Eeldab taski `lecturer-db-changes.md`.

## Sisend

**Path variable:**

| Nimi | Tüüp | Kirjeldus |
|---|---|---|
| `lecturerId` | Integer | Taastatava koolitaja ID (`lecturer.id`) |

Query parameetreid ja request body't pole — **tegevusteenus** (URL-ide kokkuleppe erand, sama muster nagu `PUT /api/training/{trainingId}/restore`): backend teab ise, mis väärtuse panna.

## Väljund

**Response (200 OK):** Tühi vastus (ainult staatuskood 200 OK).

## Eesmärk

Admin lülitab `AdminLecturersView`-s sisse "Näita kustutatud", vajutab kustutatud koolitaja real nupule "Taasta" ja kinnitab modalis. Koolitaja muutub uuesti aktiivseks (`status = "A"`): teda saab jälle koolitustele ja toimumiskordadele valida ning tema kaarti kuvatakse. Vaade laadib seejärel nimekirja uuesti ja näitab eduteadet "Koolitaja taastatud".

## Seotud andmebaasi tabelid

Vt `docs/database/2_create.sql` ja `lecturer-db-changes.md`.

### lecturer

Muudetakse ainult `status` (`"D"` → `"A"`) ja auditeerimise kaudu `updated_at`. Tabeli struktuur: vt `DELETE-api-lecturer-lecturerId.md`.

Näidisandmed: Virve Räni (4) on kustutatud — taastamist saab proovida kohe.

## Veaolukorrad

| Olukord | Status code | Response body |
|---|---|---|
| `lecturerId` ei leidu andmebaasist | 404 Not Found | `{ "message": "Ei leidnud primary keyd 'lecturerId' väärtusega: 123", "errorCode": "PRIMARY_KEY_NOT_FOUND" }` |
| Ootamatu serveripoolne viga | 500 Internal Server Error | Standardne vea response body (vastavalt projekti globaalsele error handler'ile) |

404 vastab olemasolevale mustrile `LecturerService.getValidLecturerBy(lecturerId, "lecturerId")` (leiab ka kustutatud koolitaja). **Aktiivse koolitaja** taastamine tagastab 200 OK ja midagi ei muutu (idempotentne).

## Vastuvõtu kriteeriumid

- [ ] Endpoint `PUT /api/lecturer/{lecturerId}/restore` on olemas, ei võta ega tagasta body't
- [ ] Kustutatud koolitaja → `status = "A"`, `updated_at` uueneb
- [ ] Aktiivne koolitaja → 200 OK, midagi ei muutu
- [ ] Olematu `lecturerId` → 404 `PRIMARY_KEY_NOT_FOUND`
- [ ] Teenusel on automaattestid
