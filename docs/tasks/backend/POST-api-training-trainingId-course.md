# Uue toimumiskorra lisamine

**Teenus:** `POST /api/training/{trainingId}/course`

**Kasutav vaade:** `CourseFormView.vue` (`/course-form?trainingId={id}`)

> Mockupi pilt lisatakse hiljem. Seni vt läbimängu `docs/mock-wireframe/loo-mock-vaade/admin-training-courses-view/admin-training-courses-view-labimang.html` ja märkmeid `docs/mock-wireframe/markmed/course-form-view-markmed.md` (JSON-näited).

Eeldab taske `course-db-changes.md`, `GET-api-rooms.md` (ruumid).

## Sisend

`trainingId` (path). Request body `CourseCreateRequestDto` (JSON: `docs/mock-wireframe/markmed/course-form-view-markmed.md`):

| Väli | Tüüp | Kohustuslik | Kirjeldus |
|---|---|---|---|
| `userId` | Integer | jah | sisselogitud kasutaja → `created_by` |
| `startDate`, `endDate` | LocalDate | jah | `endDate ≥ startDate` |
| `numberOfDays` | Integer | jah, ≥ 1 | frontend pakub tööpäevade arvu, backend ei arvuta |
| `numberOfAcademicHours` | Integer | jah, ≥ 1 | |
| `price` | BigDecimal | jah, ≥ 0 | |
| `lecturerIds` | List<Integer> | jah (võib olla tühi) | järjekord = `sort_order`; uus ID peab olema aktiivne koolitaja |
| `roomId` | Integer | ei | `null` = ruum puudub |
| `status` | String | jah | `U` / `O` / `F` / `X` (`D` keelatud → 400) |
| `notes`, `meetingLink` | String | ei | tühi → `null`; `meetingLink` ≤ 255 |

## Väljund

**Response (200 OK):** tühi. Luuakse `course` ja `course_lecturer` read (üks transaktsioon). Frontend suunab kalendrisse eduteatega "Toimumiskord lisatud". Toimumiskorra saab lisada ka mustandis koolitusele.

## Eesmärk

Admin lisab kalendrist ("+ Lisa toimuv koolitus") koolitusele toimumiskorra; vorm on eeltäidetud koolituse koolitajatega.

## Seotud andmebaasi tabelid

`course`, `course_lecturer`, `room`, `lecturer`, `training`.

## Veaolukorrad

| Olukord | Status code | Response body |
|---|---|---|
| `endDate` on varasem kui `startDate` | 403 Forbidden | `{ "message": "Lõppkuupäev ei saa olla varasem kui alguskuupäev", "errorCode": "COURSE_END_BEFORE_START" }` |
| Koolitust pole või see on kustutatud | 404 Not Found | `{ "message": "Ei leidnud primary keyd 'trainingId' väärtusega: 123", "errorCode": "PRIMARY_KEY_NOT_FOUND" }` |
| `roomId` ei leidu | 404 Not Found | `{ "message": "Ei leidnud primary keyd 'roomId' väärtusega: 123", "errorCode": "PRIMARY_KEY_NOT_FOUND" }` |
| Uus `lecturerIds` ID ei leidu / on kustutatud | 404 Not Found | `{ "message": "Ei leidnud primary keyd 'lecturerId' väärtusega: 123", "errorCode": "PRIMARY_KEY_NOT_FOUND" }` |
| `userId` ei leidu | 404 Not Found | `{ "message": "Ei leidnud primary keyd 'userId' väärtusega: 123", "errorCode": "PRIMARY_KEY_NOT_FOUND" }` |
| Kohustuslik väli puudub / vale väärtus (`@Valid`) | 400 Bad Request | `{ "message": "<väli>: <valideerimise teade>", "errorCode": "INCORRECT_INPUT" }` |
| Ootamatu serveripoolne viga | 500 Internal Server Error | Standardne vea response body (vastavalt projekti globaalsele error handler'ile) |

Uus `Error.COURSE_END_BEFORE_START("Lõppkuupäev ei saa olla varasem kui alguskuupäev")` (`ForbiddenException`).

## Vastuvõtu kriteeriumid

- [ ] Endpoint loob `course` ja `course_lecturer` read ühes transaktsioonis
- [ ] Valideerimine (kohustuslikud, ≥ 1, ≥ 0, status U/O/F/X, `endDate ≥ startDate`)
- [ ] Veaolukorrad nagu tabelis
- [ ] Teenusel on automaattestid
