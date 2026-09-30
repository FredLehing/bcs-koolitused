# Toimumiskorra andmed (muutmise vorm)

**Teenus:** `GET /api/course/{courseId}`

**Kasutav vaade:** `CourseFormView.vue` (`/course-form?courseId={id}`)

> Mockupi pilt lisatakse hiljem. Seni vt läbimängu `docs/mock-wireframe/loo-mock-vaade/admin-training-courses-view/admin-training-courses-view-labimang.html` ja märkmeid `docs/mock-wireframe/markmed/course-form-view-markmed.md` (JSON-näited).

Eeldab taski `course-db-changes.md`.

## Sisend

`courseId` (path, Integer).

## Väljund

**Response (200 OK):** `CourseDto` — `courseId`, `trainingId`, `startDate`, `endDate`, `numberOfDays`, `numberOfAcademicHours`, `price`, `lecturers: [{ lecturerId, lecturerName }]` (`course_lecturer`, `sort_order`), `roomId`, `roomName` (ka kustutatud ruumi nimi — vt `room-deleted-status.md`), `status`, `notes`, `meetingLink` (viimased võivad olla `null`). JSON: `docs/mock-wireframe/markmed/course-form-view-markmed.md`.

## Eesmärk

Muutmise vorm täidetakse; `trainingId` järgi laaditakse koolituse nimi (`GET-api-admin-training-trainingId.md`).

## Seotud andmebaasi tabelid

`course`, `course_lecturer`, `lecturer`.

## Veaolukorrad

| Olukord | Status code | Response body |
|---|---|---|
| Toimumiskorda pole või see on kustutatud (`D`) | 404 Not Found | `{ "message": "Ei leidnud primary keyd 'courseId' väärtusega: 123", "errorCode": "PRIMARY_KEY_NOT_FOUND" }` |
| Ootamatu serveripoolne viga | 500 Internal Server Error | Standardne vea response body (vastavalt projekti globaalsele error handler'ile) |

## Vastuvõtu kriteeriumid

- [ ] Endpoint tagastab `CourseDto` koos koolitajatega järjekorras
- [ ] Kustutatud / olematu → 404
- [ ] Teenusel on automaattestid
