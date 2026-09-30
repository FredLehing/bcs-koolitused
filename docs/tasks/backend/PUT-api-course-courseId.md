# Toimumiskorra muutmine

**Teenus:** `PUT /api/course/{courseId}`

**Kasutav vaade:** `CourseFormView.vue` (`/course-form?courseId={id}`)

> Mockupi pilt lisatakse hiljem. Seni vt läbimängu `docs/mock-wireframe/loo-mock-vaade/admin-training-courses-view/admin-training-courses-view-labimang.html` ja märkmeid `docs/mock-wireframe/markmed/course-form-view-markmed.md` (JSON-näited).

Eeldab taski `POST-api-training-trainingId-course.md` (sama valideerimine).

## Sisend

`courseId` (path). Request body `CourseUpdateRequestDto` (JSON: `docs/mock-wireframe/markmed/course-form-view-markmed.md`):

| Väli | Tüüp | Kohustuslik | Kirjeldus |
|---|---|---|---|
| `startDate`, `endDate` | LocalDate | jah | `endDate ≥ startDate` |
| `numberOfDays` | Integer | jah, ≥ 1 | frontend pakub tööpäevade arvu, backend ei arvuta |
| `numberOfAcademicHours` | Integer | jah, ≥ 1 | |
| `price` | BigDecimal | jah, ≥ 0 | |
| `lecturerIds` | List<Integer> | jah (võib olla tühi) | järjekord = `sort_order`; uus ID peab olema aktiivne koolitaja |
| `roomId` | Integer | ei | `null` = ruum puudub |
| `status` | String | jah | `U` / `O` / `F` / `X` (`D` keelatud → 400) |
| `notes`, `meetingLink` | String | ei | tühi → `null`; `meetingLink` ≤ 255 |

## Väljund

**Response (200 OK):** tühi. Muudab kõiki välju (ka staatust — eraldi tegevusteenuseid pole); `course_lecturer` read kirjutatakse `lecturerIds` järgi üle; juba seotud kustutatud koolitaja võib jääda. Frontend suunab kalendrisse ("Toimumiskord salvestatud").

## Eesmärk

Admin muudab toimumiskorda (nt tühistab: status `X`, lisab märkme).

## Seotud andmebaasi tabelid

`course`, `course_lecturer`, `room`, `lecturer`.

## Veaolukorrad

| Olukord | Status code | Response body |
|---|---|---|
| `endDate` on varasem kui `startDate` | 403 Forbidden | `{ "message": "Lõppkuupäev ei saa olla varasem kui alguskuupäev", "errorCode": "COURSE_END_BEFORE_START" }` |
| Toimumiskorda pole või see on kustutatud | 404 Not Found | `{ "message": "Ei leidnud primary keyd 'courseId' väärtusega: 123", "errorCode": "PRIMARY_KEY_NOT_FOUND" }` |
| `roomId` / uus `lecturerIds` ID ei leidu | 404 Not Found | `{ "message": "Ei leidnud primary keyd 'roomId' väärtusega: 123", "errorCode": "PRIMARY_KEY_NOT_FOUND" }` / `{ "message": "Ei leidnud primary keyd 'lecturerId' väärtusega: 123", "errorCode": "PRIMARY_KEY_NOT_FOUND" }` |
| Kohustuslik väli puudub / vale väärtus (`@Valid`) | 400 Bad Request | `{ "message": "<väli>: <valideerimise teade>", "errorCode": "INCORRECT_INPUT" }` |
| Ootamatu serveripoolne viga | 500 Internal Server Error | Standardne vea response body (vastavalt projekti globaalsele error handler'ile) |

## Vastuvõtu kriteeriumid

- [ ] Endpoint uuendab välju ja koolitajaid (järjekord)
- [ ] `status = "D"` → 400; `endDate < startDate` → 403
- [ ] Kustutatud / olematu → 404
- [ ] Teenusel on automaattestid
