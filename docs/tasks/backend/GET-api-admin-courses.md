# Kõigi koolituste toimumiskorrad (admin)

**Teenus:** `GET /api/admin-courses`

**Kasutav vaade:** `AdminAllCoursesView.vue` (`/admin-all-courses`)

> Mockupi pilt lisatakse hiljem. Seni vt läbimängu `docs/mock-wireframe/loo-mock-vaade/courses-view/admin-all-courses-view-labimang.html` ja märkmeid `docs/mock-wireframe/markmed/admin-all-courses-view-markmed.md` (API märge on sama sisuga).

Eeldab taski `courses-calendar-db-changes.md`. Eeskuju: `GET /api/admin-trainings` (`AdminTrainingSummarySpecifications`, leheküljestus, `sortBy`).

## Sisend

**Query parameetrid** (valikulise puudumine = filtrit ei rakendata):

| Nimi | Tüüp | Kirjeldus |
|---|---|---|
| `contentLang` | String | koolituse nime keel |
| `searchText` | String | sõnad koolituse nimest (iga sõna peab esinema, tõstutundetu) |
| `categoryId`, `trainingLanguageId` | Integer | |
| `status` | String | `U` / `O` / `F` / `X`; puudub = kõik peale `D` |
| `attendance` | String | `ONSITE` (`is_on_site`) / `ONLINE` (`has_meeting_link`) |
| `isPromoted` | Boolean | |
| `startDateFrom`, `startDateTo` | LocalDate | algus vahemikus |
| `includePast` | Boolean | vaikimisi `false` → `is_past = false` |
| `sortBy` | String | `startDate`, `trainingTitle`, `price`, `status` (`status_order`), `participantCount`, `enquiryCount` |
| `sortDirection` | String | `ASC` / `DESC` |
| `page`, `limit` | Integer | |

Soovitus: parameetrid ühte `AdminCourseFilterDto`-sse (`@ModelAttribute`), nagu `AdminTrainingFilterDto`.

## Väljund

**Response (200 OK):** `AdminCourseSummaryDto`

```json
{
  "totalPages": 1,
  "totalElements": 10,
  "adminCourseSummaries": [
    {
      "courseId": 1, "trainingId": 1, "trainingTitle": "Java algkursus",
      "startDate": "2026-10-05", "endDate": "2026-10-09", "isPast": false,
      "numberOfDays": 5, "price": 490.0, "status": "O", "isPromoted": true,
      "hasMeetingLink": false, "participantCount": 3, "paidCount": 2, "enquiryCount": 1
    }
  ]
}
```

## Eesmärk

Admin näeb kõigi koolituste toimumiskordi ühes tabelis koos osalejate, tasunute ja huviliste arvuga.

## Seotud andmebaasi tabelid

View `admin_course_summary` (`content_language_code = contentLang`, alati `status <> 'D'` ja `training_status <> 'D'`). Vaikimisi järjestus `is_past, days_from_today, course_id`; `sortBy` korral see veerg + `course_id`.

Näidisandmed (täna = 01/10/2026): vaikimisi 10 toimumiskorda, `includePast=true` → 12.

## Veaolukorrad

| Olukord | Status code | Response body |
|---|---|---|
| Ootamatu serveripoolne viga | 500 | Standardne vea response body |

## Vastuvõtu kriteeriumid

- [ ] Kõik filtrid, sorteerimine ja leheküljestus töötavad
- [ ] `participantCount`/`paidCount` ainult `R` osalejad, `enquiryCount` kõik päringud
- [ ] Teenusel on automaattestid
