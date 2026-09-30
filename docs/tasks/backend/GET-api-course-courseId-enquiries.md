# Toimumiskorra huvilised (admin)

**Teenus:** `GET /api/course/{courseId}/enquiries`

**Kasutav vaade:** `AdminCourseView.vue`, tabel "Huvilised"

> Mockupi pilt lisatakse hiljem. Seni vt läbimängu `docs/mock-wireframe/loo-mock-vaade/courses-view/admin-all-courses-view-labimang.html` ja märkmeid `docs/mock-wireframe/markmed/admin-course-view-markmed.md` (API märge on sama sisuga).

Eeldab taski `courses-calendar-db-changes.md`.

## Sisend

**Path variable:** `courseId` (Integer). Tõlgitavaid välju pole → `contentLang`-i pole.

## Väljund

**Response (200 OK):** `CourseEnquiryDto` list

```json
[
  { "enquiryId": 1, "createdAt": "2026-09-15T05:30:00Z", "fullName": "Anna Saar",
    "email": "anna.saar@example.com", "companyName": null, "status": "U" }
]
```

## Eesmärk

Admin näeb toimumiskorraga seotud päringuid ja avab neid `/admin-enquiry` vaates.

## Seotud andmebaasi tabelid

`enquiry` + `profile`, `course_id = courseId`, uusimad eespool. Kõik staatused (`U`, `H`).

## Veaolukorrad

| Olukord | Status code | Response body |
|---|---|---|
| Olematu courseId | 404 | `PRIMARY_KEY_NOT_FOUND` — "Ei leidnud primary keyd 'courseId' väärtusega: 123" |

## Vastuvõtu kriteeriumid

- [ ] Päringud uusimad eespool, `[]` kui pole
- [ ] Teenusel on automaattestid
