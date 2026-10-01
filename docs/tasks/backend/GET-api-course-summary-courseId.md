# Avalik toimumiskorra leht

**Teenus:** `GET /api/course-summary/{courseId}`

**Kasutav vaade:** `CourseView.vue` (`/course?courseId={id}`), ka `CourseRegistrationView.vue` (kokkuvõte)

> Mockupi pilt lisatakse hiljem. Seni vt läbimängu `docs/mock-wireframe/loo-mock-vaade/courses-view/courses-view-labimang.html` ja märkmeid `docs/mock-wireframe/markmed/course-view-markmed.md` (API märge on sama sisuga).

Eeldab taski `courses-calendar-db-changes.md`.

## Sisend

**Path variable:** `courseId` (Integer). **Query:** `contentLang` (String).

## Väljund

**Response (200 OK):** `CoursePageDto`

```json
{
  "courseId": 9, "trainingId": 3, "trainingTranslationId": 5, "isMainLanguageFallback": false,
  "title": "Spring Boot veebiarendus", "shortDescription": "REST API-de loomine Spring Booti abil.", "description": "<p>…</p>",
  "categoryName": "Programmeerimine", "trainingLanguageFlagIconCode": "fi-ee",
  "fundingTypes": [ { "fundingTypeId": 1, "fundingTypeName": "Töötukassa" } ],
  "startDate": "2026-10-12", "endDate": "2026-10-15", "isPast": false,
  "numberOfDays": 4, "numberOfAcademicHours": 32, "price": 560.0, "status": "O",
  "isOnSite": true, "isOnline": false,
  "lecturers": [ { "lecturerId": 1, "lecturerName": "Rain Tüür" } ],
  "upcomingCourses": [
    { "courseId": 9, "startDate": "2026-10-12", "endDate": "2026-10-15", "status": "O", "isOnSite": true, "isOnline": false }
  ]
}
```

## Eesmärk

Toimumiskorra leht: koolituse tekst, toimumiskorra andmed, koolitajad ja sama koolituse toimumiskordade lingid.

## Seotud andmebaasi tabelid

`course` + `training` + `training_translation` (valitud keel, puudumisel põhikeel → `isMainLanguageFallback = true`) + `category_translation`, `training_funding_type`, `course_lecturer` (`sort_order`). `upcomingCourses` = sama koolituse kõik `public_course_summary` read (`content_language_code = contentLang`), alguse järgi (praegune kaasa arvatud, kui tulevane).

## Veaolukorrad

| Olukord | Status code | Response body |
|---|---|---|
| Olematu courseId | 404 | `PRIMARY_KEY_NOT_FOUND` — "Ei leidnud primary keyd 'courseId' väärtusega: 123" |
| Toimumiskord pole `O`/`F` või koolitus pole `P` | 404 | sama mis olematu |

Möödunud `O`/`F` toimumiskord leitakse (`isPast = true`).

## Vastuvõtu kriteeriumid

- [ ] Mitteavalik → 404, möödunud avalik → 200
- [ ] Põhikeele varuvariant + `isMainLanguageFallback`
- [ ] Teenusel on automaattestid
