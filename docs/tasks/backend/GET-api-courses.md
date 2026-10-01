# Avalik koolituste kalender

**Teenus:** `GET /api/courses`

**Kasutav vaade:** `CoursesView.vue` (`/courses`)

> Mockupi pilt lisatakse hiljem. Seni vt läbimängu `docs/mock-wireframe/loo-mock-vaade/courses-view/courses-view-labimang.html` ja märkmeid `docs/mock-wireframe/markmed/courses-view-markmed.md` (API märge on sama sisuga).

Eeldab taski `courses-calendar-db-changes.md`. Eeskuju: `GET /api/trainings` (`TrainingSummarySpecifications`, `fundingTypes`).

## Sisend

**Query parameetrid:**

| Nimi | Tüüp | Kirjeldus |
|---|---|---|
| `contentLang` | String | tõlgitud väljade keel |
| `searchText` | String | `""` = kõik; iga sõna `title` või `shortDescription` väljas |
| `categoryId`, `trainingLanguageId`, `fundingTypeId` | Integer | `0` = kõik |
| `attendance` | String | `ONSITE` / `ONLINE` (valikuline) |
| `hideFull` | Boolean | `true` = ilma `F` toimumiskordadeta |
| `startDateFrom`, `startDateTo` | LocalDate | valikulised |
| `page`, `limit` | Integer | |

## Väljund

**Response (200 OK):** `CourseSummaryPageDto` (nimi vali nii, et ei kattuks olemasoleva `CourseSummaryDto`-ga)

```json
{
  "totalPages": 2, "totalElements": 7,
  "courseSummaries": [
    {
      "courseId": 1, "trainingId": 1, "title": "Java algkursus",
      "shortDescription": "Java programmeerimise alused algajatele.", "categoryName": "Programmeerimine",
      "trainingLanguageFlagIconCode": "fi-ee", "startDate": "2026-10-05", "endDate": "2026-10-09",
      "numberOfDays": 5, "numberOfAcademicHours": 40, "price": 490.0, "status": "O",
      "isPromoted": true, "isOnSite": true, "isOnline": false, "lecturerNames": "Rain Tüür, Meelis Teern",
      "fundingTypes": [ { "fundingTypeId": 1, "fundingTypeName": "Töötukassa" } ]
    }
  ]
}
```

Veebilinki **ei** tagastata.

## Eesmärk

Külastaja näeb avalikke tulevasi toimumiskordi, filtreerib ja avab toimumiskorra lehe.

## Seotud andmebaasi tabelid

View `public_course_summary` (publitseeritud koolitus, `O`/`F`, `start_date >= täna`, ainult olemasoleva `contentLang` tõlkega). Järjestus `is_promoted DESC, start_date, course_id`. `fundingTypes` nagu `GET /api/trainings`.

Näidisandmed: `et` → 7 toimumiskorda (esimesed 1, 9, 11 esile tõstetud), `hideFull=true` → 5.

## Veaolukorrad

| Olukord | Status code | Response body |
|---|---|---|
| Ootamatu serveripoolne viga | 500 | Standardne vea response body |

## Vastuvõtu kriteeriumid

- [ ] Mustand/tühistatud/kustutatud/möödunud ja mittepublitseeritud koolituse toimumiskorrad puuduvad
- [ ] Kõik filtrid ja leheküljestus töötavad
- [ ] Teenusel on automaattestid
