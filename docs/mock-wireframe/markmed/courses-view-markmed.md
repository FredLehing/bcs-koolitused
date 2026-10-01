# CoursesView.vue — märkmed

Avalik koolituste kalender: toimumiskordade kaardid, otsing ja filtrid. Otsused, andmebaasi ettepanek ja skeemid: `docs/mock-wireframe/loo-mock-vaade/courses-view/courses-view-skeemid.md`. Interaktiivne läbimäng: `courses-view-labimang.html`. Uute teenuste DTO-d on ettepanek; olemas: `GET /api/categories`, `GET /api/funding-types`, `GET /api/languages`.

## Vaate märkmed

```text
Roll: Kõik rollid (sh külastajad, sisselogimist ei nõuta)
Failinimi: CoursesView.vue
Frontend rada: /courses

Vaatega seotud lisainfo:
Avaneb navbari menüüst "Koolitused" → "Koolituste kalender" (menüüs ka "Meie koolitused" → /trainings). Pealkiri "Koolituste kalender". Näidatakse publitseeritud koolituste avatud ja täis toimumiskordi, mis algavad täna või hiljem.
Vasakul filtrid (rakenduvad kohe, page = 0): Periood alates–kuni, Toimumisviis (Kõik/Kohapeal/Veebis), lüliti "Peida täis", Koolituse keel, Koolituse kategooria, Rahastus; link "Tühjenda filtrid", kui mõni filter on valitud. Valikud: GET /api/languages, GET /api/categories, GET /api/funding-types.
Otsing nagu /trainings (pealkiri ja lühikirjeldus, "Otsi"/Enter, × tühjendab, tulemuste rida ja "Tühista otsing").
Kaart CourseCard.vue: kuupäevaplokk (nt "05.–09. okt 2026", "5 päeva · 40 t"), pealkiri, lühikirjeldus, kategooria, rahastus, koolitajad, märgised Kohapeal/Veebis, õppekeele lipp, hind, märgis "Täis" ja nupp "Vaata lähemalt" → /course?courseId={id}. Esile tõstetud: täht ja kollakas taust; need on eespool, edasi alguse järgi. Adminile pliiats → /course-form?courseId={id}.
Leheküljestus 5 kaarti lehel (PaginationNav). Keele vahetusel laaditakse uuesti (inglise keeles ainult en tõlkega koolituste toimumiskorrad).
```

## API märkmed — GET /api/courses

```text
API: GET /api/courses

Query parameetrid:
contentLang: String — tõlgitud väljade keel ("et"/"en")
searchText: String — "" = kõik; iga sõna peab esinema title või shortDescription väljas
categoryId: Integer — 0 = kõik
trainingLanguageId: Integer — 0 = kõik
fundingTypeId: Integer — 0 = kõik
attendance: String — "ONSITE"/"ONLINE" (valikuline, puudub = kõik)
hideFull: Boolean — true = ilma "F" toimumiskordadeta
startDateFrom, startDateTo: LocalDate — algus vahemikus (valikulised)
page: Integer, limit: Integer

Response (200):
CourseSummaryDto.java
{
  "totalPages": 2,
  "totalElements": 7,
  "courseSummaries": [
    {
      "courseId": 1,
      "trainingId": 1,
      "title": "Java algkursus",
      "shortDescription": "Java programmeerimise alused algajatele.",
      "categoryName": "Programmeerimine",
      "trainingLanguageFlagIconCode": "fi-ee",
      "startDate": "2026-10-05",
      "endDate": "2026-10-09",
      "numberOfDays": 5,
      "numberOfAcademicHours": 40,
      "price": 490.0,
      "status": "O",
      "isPromoted": true,
      "isOnSite": true,
      "isOnline": false,
      "lecturerNames": "Rain Tüür, Meelis Teern",
      "fundingTypes": [ { "fundingTypeId": 1, "fundingTypeName": "Töötukassa" } ]
    },
    ...
  ]
}

API teenuse lisainfo:
View public_course_summary (content_language_code = contentLang): ainult publitseeritud koolituse ("P") "O"/"F" toimumiskorrad, start_date >= täna; ainult koolitused, millel on contentLang tõlge. Järjestus: isPromoted kahanevalt, startDate kasvavalt, courseId. attendance ONSITE → isOnSite, ONLINE → isOnline (hübriid sobib mõlemaga). Veebilinki ei tagastata. lecturerNames võib olla null, fundingTypes tühi list.

Veateated: —
```

## API märkmed — GET /api/categories

```text
API: GET /api/categories

Query parameetrid:
contentLang: String — categoryName keel ("et"/"en")

Response (200):
CategoryDto.java
[
  {
    "categoryId": 1,
    "categoryName": "Programmeerimine"
  },
  ...
]

API teenuse lisainfo:
Tagastab kõik süsteemis olevad kategooriad contentLang keeles (category_translation kaudu). Vaade kasutab neid "Koolituse kategooria" filtri valikutena.

Veateated: —
```

## API märkmed — GET /api/funding-types

```text
API: GET /api/funding-types

Query parameetrid:
contentLang: String — fundingTypeName keel ("et"/"en")

Response (200):
FundingTypeDto.java
[
  {
    "fundingTypeId": 1,
    "fundingTypeName": "Töötukassa"
  },
  ...
]

API teenuse lisainfo:
Tagastab kõik süsteemis olevad rahastustüübid contentLang keeles (funding_type_translation kaudu). Vaade kasutab neid "Rahastus" filtri valikutena. FundingTypeDto on sama, mida kasutab GET /api/trainings vastuse fundingTypes list.

Veateated: —
```

## API märkmed — GET /api/languages

```text
API: GET /api/languages

Response (200):
SystemLanguageDto.java
[
  {
    "languageId": 1,
    "languageCode": "et",
    "languageName": "Eesti",
    "isMainLanguage": true,
    "requiresTranslation": true,
    "flagIconCode": "fi-ee"
  },
  ...
]

API teenuse lisainfo:
Tagastab kõik süsteemis olevad keeled (language tabel). Vaade kasutab neid "Koolituse keel" filtri valikutena (languageId → trainingLanguageId). languageName ei ole tõlgitud, seega contentLang parameetrit pole. requiresTranslation = false tähendab õppekeelt, millesse koolituse sisu ei tõlgita (nt ru) — see keel on "Koolituse keel" valikutes, aga mitte tõlkelippudes. flagIconCode on flag-icons CSS klass (nt "fi-ee").

Veateated: —
```
