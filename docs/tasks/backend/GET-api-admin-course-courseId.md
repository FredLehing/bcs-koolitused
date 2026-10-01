# Toimumiskorra ülevaade (admin)

**Teenus:** `GET /api/admin-course/{courseId}`

**Kasutav vaade:** `AdminCourseView.vue` (`/admin-course?courseId={id}`)

> Mockupi pilt lisatakse hiljem. Seni vt läbimängu `docs/mock-wireframe/loo-mock-vaade/courses-view/admin-all-courses-view-labimang.html` ja märkmeid `docs/mock-wireframe/markmed/admin-course-view-markmed.md` (API märge on sama sisuga).

Eeldab taski `courses-calendar-db-changes.md`.

## Sisend

**Path variable:** `courseId` (Integer). **Query:** `contentLang` (String).

## Väljund

**Response (200 OK):** `AdminCourseDto`

```json
{
  "courseId": 1, "trainingId": 1, "trainingTranslationId": 1, "trainingTitle": "Java algkursus",
  "startDate": "2026-10-05", "endDate": "2026-10-09", "isPast": false,
  "numberOfDays": 5, "numberOfAcademicHours": 40, "price": 490.0, "status": "O", "isPromoted": true,
  "lecturerNames": "Rain Tüür, Meelis Teern", "roomName": "Assauwe", "meetingLink": null, "notes": "Kaasa sülearvuti."
}
```

`lecturerNames`, `roomName`, `meetingLink`, `notes` võivad olla `null`.

## Eesmärk

Ühe toimumiskorra kõik andmed ainult lugemiseks (sama info mis `/course-form`-is) + link koolituse lehele.

## Seotud andmebaasi tabelid

`admin_course_summary` rida (`course_id` + `contentLang`), lisaks `course` (`number_of_academic_hours`, `notes`, `meeting_link`), `room`, `course_lecturer` + `lecturer` (`sort_order`).

## Veaolukorrad

| Olukord | Status code | Response body |
|---|---|---|
| Olematu courseId | 404 | `PRIMARY_KEY_NOT_FOUND` — "Ei leidnud primary keyd 'courseId' väärtusega: 123" |
| Kustutatud toimumiskord või koolitus | 404 | sama mis olematu |

## Vastuvõtu kriteeriumid

- [ ] Vastus nagu ülal; nimed `contentLang` keeles, puudumisel põhikeeles
- [ ] Kustutatud → 404
- [ ] Teenusel on automaattestid
