# Koolituse toimumiskorrad (kalendri tabel)

**Teenus:** `GET /api/training/{trainingId}/courses?includePast={includePast}`

**Kasutav vaade:** `AdminTrainingCoursesView.vue` (`/admin-training-courses?trainingId={id}`)

> Mockupi pilt lisatakse hiljem. Seni vt läbimängu `docs/mock-wireframe/loo-mock-vaade/admin-training-courses-view/admin-training-courses-view-labimang.html` ja märkmeid `docs/mock-wireframe/markmed/admin-training-courses-view-markmed.md` (JSON-näited).

Eeldab taski `course-db-changes.md`.

## Sisend

| Nimi | Tüüp | Kohustuslik | Kirjeldus |
|---|---|---|---|
| `trainingId` (path) | Integer | jah | Koolituse ID |
| `includePast` (query) | Boolean | ei (vaikimisi `false`) | `true` = ka möödunud (`end_date < täna`) |

## Väljund

**Response (200 OK):** `List<CourseSummaryDto>` — `courseId`, `startDate`, `endDate` (`LocalDate`), `numberOfDays`, `numberOfAcademicHours`, `price`, `lecturerNames` (nimed komadega `sort_order` järjekorras või `null`), `roomName` (või `null`), `status`, `isPast`, `participantCount`, `hasNotes`, `hasMeetingLink`. JSON: `docs/mock-wireframe/markmed/admin-training-courses-view-markmed.md`.
- Kustutatud (`D`) toimumiskordi ei tagastata.
- Järjestus: `is_past`, `days_from_today`, `course_id` (tulevased lähimast, möödunud hiliseimast). Sorteerimine veergude järgi on **ainult frontendis**.
- `notes` / `meetingLink` sisu ei tagastata. Tühi list, kui toimumiskordi pole.

## Eesmärk

Admin näeb koolituse toimumiskordi tabelis; lüliti "Näita ka möödunud".

## Seotud andmebaasi tabelid

View `course_summary` (`course_lecturer`, `lecturer`, `room`, `course_participant`). Näidisandmed: koolitus 1 → 4 tulevast (1, 4, 5, 6), `includePast=true` → 6; koolitus 10 → tühi.

## Veaolukorrad

| Olukord | Status code | Response body |
|---|---|---|
| Koolitust pole või see on kustutatud | 404 Not Found | `{ "message": "Ei leidnud primary keyd 'trainingId' väärtusega: 123", "errorCode": "PRIMARY_KEY_NOT_FOUND" }` |
| Ootamatu serveripoolne viga | 500 Internal Server Error | Standardne vea response body (vastavalt projekti globaalsele error handler'ile) |

## Vastuvõtu kriteeriumid

- [ ] Endpoint tagastab toimumiskorrad õiges järjekorras; `D` välja jäetud
- [ ] `includePast` töötab; `lecturerNames` järjekorras
- [ ] Kustutatud / olematu koolitus → 404
- [ ] Teenusel on automaattestid
