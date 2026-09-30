# Koolitaja kustutamine (soft delete)

**Teenus:** `DELETE /api/lecturer/{lecturerId}`

**Kasutav vaade:** `AdminLecturersView.vue` (`/admin-lecturers`)

> Mockupi pilt lisatakse hiljem. Seni vt läbimängu `docs/mock-wireframe/loo-mock-vaade/admin-lecturers-view/admin-lecturers-view-labimang.html` ja märkmeid `docs/mock-wireframe/markmed/admin-lecturers-view-markmed.md`.

Eeldab taski `lecturer-db-changes.md` (`lecturer.status`, `LecturerStatus`).

## Sisend

**Path variable:**

| Nimi | Tüüp | Kirjeldus |
|---|---|---|
| `lecturerId` | Integer | Kustutatava koolitaja ID (`lecturer.id`) |

Query parameetreid ja request body't pole.

## Väljund

**Response (200 OK):** Tühi vastus (ainult staatuskood 200 OK).

## Eesmärk

Admin kustutab `AdminLecturersView` tabelis koolitaja prügikasti ikooniga (komponent `LecturerDeleteButton.vue`, kinnituse modal "Kas soovid koolitaja „Kersti Laidvee“ kustutada?"). Kustutamine on **soft delete**: `lecturer.status` muutub väärtuseks `"D"`. Kustutatud koolitajat ei pakuta "Vali koolitaja" modalis ja tema kaarti (`LecturerCard`) ei kuvata (vt `lecturer-deleted-status.md`); nimekirjas näeb teda lülitiga "Näita kustutatud" ja saab taastada (`PUT-api-lecturer-lecturerId-restore.md`).

Koolitajat, kellel on **tulevasi toimumiskordi**, kustutada ei saa — enne tuleb neile teine koolitaja valida. Frontend keelab sel juhul prügikasti (`upcomingCourseCount > 0`), backend kontrollib sama. Vaikimisi koolitaja roll koolitustel kustutamist ei keela.

## Seotud andmebaasi tabelid

Vt `docs/database/2_create.sql` ja `lecturer-db-changes.md`.

### lecturer

Muudetakse ainult `status` (ja auditeerimise kaudu `updated_at`). Tõlkeid, pilti ega seoseid (`training.default_lecturer_id`, `course.lecturer_id`) ei muudeta.

```sql
CREATE TABLE lecturer
(
    id         serial       NOT NULL,
    full_name  varchar(255) NOT NULL,
    status     varchar(1)   NOT NULL,
    created_at timestamp    NOT NULL,
    updated_at timestamp    NOT NULL,
    created_by int          NOT NULL,
    CONSTRAINT lecturer_pk PRIMARY KEY (id)
);
```

### course (ainult lugemine)

Tulevaste toimumiskordade kontroll: `course.lecturer_id = :lecturerId AND course.end_date >= current_date AND course.status NOT IN ('D', 'X')` (nt repositooriumi `existsBy…` / JPQL `count`). `course` entity on koodis olemas (`persistance/course/`); päring sobib nt `CourseRepository`-sse.

Näidisandmed: Kersti Laidvee (3) — tulevasi toimumiskordi pole → kustutamine õnnestub. Rain Tüür (1) — tulevane toimumiskord (course 1, 05/10/2026) → 403. Virve Räni (4) on juba kustutatud.

## Veaolukorrad

| Olukord | Status code | Response body |
|---|---|---|
| `lecturerId` ei leidu andmebaasist | 404 Not Found | `{ "message": "Ei leidnud primary keyd 'lecturerId' väärtusega: 123", "errorCode": "PRIMARY_KEY_NOT_FOUND" }` |
| Koolitajal on tulevasi toimumiskordi | 403 Forbidden | `{ "message": "Koolitajal on tulevasi toimumiskordi, vali neile enne teine koolitaja", "errorCode": "LECTURER_HAS_UPCOMING_COURSES" }` |
| Ootamatu serveripoolne viga | 500 Internal Server Error | Standardne vea response body (vastavalt projekti globaalsele error handler'ile) |

404 vastab olemasolevale mustrile `LecturerService.getValidLecturerBy(lecturerId, "lecturerId")` (leiab ka kustutatud koolitaja). 403: `ForbiddenException` + uus `Error.LECTURER_HAS_UPCOMING_COURSES("Koolitajal on tulevasi toimumiskordi, vali neile enne teine koolitaja")`.

**Juba kustutatud koolitaja** uuesti kustutamine tagastab 200 OK ja midagi ei muutu (idempotentne).

## Vastuvõtu kriteeriumid

- [ ] Endpoint `DELETE /api/lecturer/{lecturerId}` on olemas, ei võta ega tagasta body't
- [ ] Õnnestunud päring määrab `lecturer.status = "D"`, `updated_at` uueneb (auditeerimine)
- [ ] Tõlked, pilt ja seosed jäävad alles
- [ ] Tulevaste toimumiskordadega koolitaja → 403 `LECTURER_HAS_UPCOMING_COURSES`; tühistatud (`X`), kustutatud (`D`) ja möödunud toimumiskorrad kustutamist ei takista
- [ ] Juba kustutatud koolitaja → 200 OK, midagi ei muutu
- [ ] Olematu `lecturerId` → 404 `PRIMARY_KEY_NOT_FOUND`
- [ ] `Error` enumis on `LECTURER_HAS_UPCOMING_COURSES`
- [ ] Teenusel on automaattestid
