# AdminAllCoursesView.vue — märkmed

Kõigi koolituste toimumiskorrad ühes tabelis ("Koolituste kalender", admin). Otsused, andmebaasi ettepanek ja skeemid: `docs/mock-wireframe/loo-mock-vaade/courses-view/courses-view-skeemid.md`. Interaktiivne läbimäng: `admin-all-courses-view-labimang.html`. Uute teenuste DTO-d on ettepanek; olemas: `GET /api/training-titles`, `GET /api/categories`, `GET /api/languages`, `DELETE /api/course/{courseId}`.

## Vaate märkmed

```text
Roll: Admin
Failinimi: AdminAllCoursesView.vue
Frontend rada: /admin-all-courses

Vaatega seotud lisainfo:
Ülal vahelehed (AdminTabs.vue): Koolituste päringud | Registreerumised | Koolitused | Koolituste kalender | Koolitajad | Koolitusruumid — samad lingid ja järjekord mis menüüs "Admin", selle vaate vaheleht on aktiivne. Kitsal ekraanil on vahelehed ühel keritaval real, aktiivne keritakse keskele.
Avaneb navbari menüüst "Admin" → "Koolituste kalender". Kõigi koolituste toimumiskorrad ühes tabelis: Algus | Päevi | Koolitus | Hind | Staatus | Osalejad | Tasunud | Veebilink | Huvilisi | Tegevused. Koolituse nimi on kasutajaliidese keeles (puudumisel põhikeeles) ja viib koolituse kalendrisse /admin-training-courses?trainingId={id}; esile tõstetud toimumiskorral täht ☆. Tasunud = "tasunud / osalejad" (ainult registreerunud osalejad), kõik tasunud → roheline, 0 osalejat → "—". Huvilisi = kõik toimumiskorraga seotud päringud. Möödunud rida on tuhmim märgisega "Toimunud".
Vaikimisi ainult tulevased toimumiskorrad; lüliti "Näita ka möödunud" → includePast=true. Kustutatud toimumiskordi ja kustutatud koolituste toimumiskordi ei kuvata.
Otsing koolituse nimest ("Otsi"/Enter, iga sõna peab esinema, ettepanekud GET /api/training-titles). Kaart "Otsingu filtrid" (vaikimisi peidus): Periood alates–kuni, Kategooria, Koolituse keel, Staatus, Toimumisviis, Esile tõstetud; rakenduvad nupuga "Filtreeri", märk "N filtrit aktiivne", "Tühjenda filtrid".
Sorteerimine backendis: Algus, Koolitus, Hind, Staatus, Osalejad, Huvilisi (1. klõps kasvav, järgmine vahetab suunda). Vaikimisi tulevased lähimast, siis möödunud hiliseimast. Leheküljestus 10 rida, "Kokku N toimumiskorda". Iga otsing, filter, sorteerimine ja lüliti alustab lehelt 0.
Tegevused (sama järjekord nagu /admin-trainings): silm → /admin-course?courseId={id}, pliiats → /course-form?courseId={id}, kalender → koolituse kalender /admin-training-courses?trainingId={id}, prügikast → CourseDeleteButton (kinnitus, DELETE /api/course/{courseId}, tabel laaditakse uuesti).
```

## API märkmed — GET /api/admin-courses

```text
API: GET /api/admin-courses

Query parameetrid:
contentLang: String — koolituse nime keel ("et"/"en")
searchText: String — sõnad koolituse nimest (valikuline)
categoryId: Integer (valikuline)
trainingLanguageId: Integer (valikuline)
status: String — "U"/"O"/"F"/"X" (valikuline, puudub = kõik peale "D")
attendance: String — "ONSITE"/"ONLINE" (valikuline)
isPromoted: Boolean (valikuline)
startDateFrom, startDateTo: LocalDate — algus vahemikus (valikulised)
includePast: Boolean — true = ka möödunud (vaikimisi false)
sortBy: String — startDate/trainingTitle/price/status/participantCount/enquiryCount (valikuline)
sortDirection: String — ASC/DESC (valikuline)
page: Integer, limit: Integer

Response (200):
AdminCourseSummaryDto.java
{
  "totalPages": 1,
  "totalElements": 10,
  "adminCourseSummaries": [
    {
      "courseId": 1,
      "trainingId": 1,
      "trainingTitle": "Java algkursus",
      "startDate": "2026-10-05",
      "endDate": "2026-10-09",
      "isPast": false,
      "numberOfDays": 5,
      "price": 490.0,
      "status": "O",
      "isPromoted": true,
      "hasMeetingLink": false,
      "participantCount": 3,
      "paidCount": 2,
      "enquiryCount": 1
    },
    ...
  ]
}

API teenuse lisainfo:
Andmed tulevad view'st admin_course_summary (content_language_code = contentLang). Alati välistatakse status = "D" ja kustutatud koolituse toimumiskorrad (training_status = "D"). includePast=false → is_past = false. attendance ONSITE → is_on_site, ONLINE → has_meeting_link (hübriid sobib mõlemaga). searchText: iga sõna peab esinema koolituse nimes (contains, tõstutundetu) nagu GET /api/admin-trainings. participantCount ja paidCount loevad ainult "R" osalejaid; enquiryCount kõiki päringuid. Vaikimisi järjestus is_past, days_from_today; sortBy korral see veerg; alati lisaks courseId.

Veateated: —
```

## API märkmed — GET /api/training-titles

```text
API: GET /api/training-titles

Query parameetrid:
contentLang: String — nimede keel ("et"/"en")

Response (200):
TrainingTitleDto.java
[
  {
    "trainingId": 1,
    "title": "Java algkursus"
  },
  ...
]

API teenuse lisainfo:
Kõigi aktiivsete (status ≠ "D") koolituste nimed contentLang keeles, puuduva tõlke korral põhikeeles (sama view admin_training_summary). Sorteeritud title järgi. AdminVaade kasutab neid otsinguvälja datalist'is ettepanekuteks.

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

## API märkmed — DELETE /api/course/{courseId}

```text
API: DELETE /api/course/{courseId}

Response (200): NONE

API teenuse lisainfo:
Soft delete: määrab course.status = "D" (CourseStatus.DELETED) ja uuendab updated_at. Osalejaid ei kustutata. Kustutatud toimumiskord kaob kalendrist; taastamist praegu pole. Kustutada saab ka osalejatega ja möödunud toimumiskorda (frontend hoiatab osalejate korral). Frontendis teeb kutse CourseDeleteButton.vue (kalendri tabel ja CourseFormView muutmise olek). Juba kustutatud toimumiskorra korral midagi ei muutu.

Veateated:
HTTP: 404
errorCode: PRIMARY_KEY_NOT_FOUND
message: "Ei leidnud primary keyd 'courseId' väärtusega: 123"
```
