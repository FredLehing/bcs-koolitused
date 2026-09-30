# AdminTrainingCoursesView.vue — märkmed

Koolituse kalender: koolituse andmed ja toimumiskordade tabel. Otsused, andmebaasi view ettepanek ja skeemid: `docs/mock-wireframe/loo-mock-vaade/admin-training-courses-view/admin-training-courses-view-skeemid.md`. Uute teenuste (`GET /api/admin-training/{trainingId}`, `GET /api/training/{trainingId}/courses`, `DELETE /api/course/{courseId}`) DTO-d on ettepanek.

## Vaate märkmed

```text
Roll: Admin
Failinimi: AdminTrainingCoursesView.vue
Frontend rada: /admin-training-courses?trainingId={id}

Vaatega seotud lisainfo:
Avaneb AdminTrainingsView rea ikoonist "Kalender" ja TrainingFormView kiirnupust "Kalender". Ülal koolituse kaart (TrainingSummaryCard.vue): nimi, staatus, kategooria, õppekeele lipp, toimumiskoht, koolitajad (nimed komadega), rahastus, sätted ning lingid "Vaata" ja "Muuda"; andmed kasutajaliidese keeles (puuduva tõlke korral põhikeeles), keele vahetusel laaditakse uuesti. Kirjelduse kaart on vaikimisi peidus ("▾ Näita kirjeldust" / "▴ Peida kirjeldus").
Tabelis on toimumiskorrad: tulevased eespool (lähim üleval), lüliti "Näita ka möödunud" lisab möödunud (hiliseim üleval) tuhmimalt ja märgisega "Toimunud" — see tuleneb kuupäevast, mitte staatusest. Kustutatud toimumiskordi ei kuvata. Märkmete ja veebilingi sisu ei näidata, ainult ✓/✗. Kuupäevad kujul 30/09/2026, koolitajad komadega (puuduvad koolitajad / ruum "—"). Leheküljestust pole, all "Kokku N toimumiskorda". Veerud Algus, Hind, Staatus ja Osalejaid on sorteeritavad ainult frontendis (API kutset ei tehta): 1. klõps kasvav, 2. kahanev, 3. tagasi vaikimisi järjestusse; staatus järjekorras Mustand → Avatud → Täis → Tühistatud.
Nupp "+ Lisa toimuv koolitus" → /course-form?trainingId={id}; pliiats → /course-form?courseId={id}; prügikast (CourseDeleteButton.vue) küsib kinnitust, teeb DELETE ise ja vaade laadib tabeli uuesti (osalejate korral hoiatab ja soovitab tühistamist). Kiirnupp "Koolituste haldus" → /admin-trainings. Kustutatud või olematu koolitus → üldine veavaade.
```

## API märkmed — GET /api/admin-training/{trainingId}

```text
API: GET /api/admin-training/{trainingId}

Query parameetrid:
contentLang: String — nimede ja kirjelduse keel ("et"/"en")

Response (200):
AdminTrainingDto.java
{
  "trainingId": 1,
  "trainingTranslationId": 1,
  "title": "Java algkursus",
  "description": "<p>Kursusel õpitakse Java süntaksit, objektorienteeritud programmeerimist ja põhilisi andmestruktuure.</p>",
  "categoryName": "Programmeerimine",
  "trainingLanguageCode": "et",
  "trainingLanguageFlagIconCode": "fi-ee",
  "locationName": "BCS Koolitus",
  "lecturers": [
    {
      "lecturerId": 1,
      "lecturerName": "Rain Tüür"
    },
    ...
  ],
  "status": "P",
  "isOrderable": true,
  "isPromoted": true,
  "fundingTypes": [
    {
      "fundingTypeId": 1,
      "fundingTypeName": "Töötukassa"
    },
    ...
  ]
}

API teenuse lisainfo:
Ühe koolituse admini ülevaade (ka mustand). Andmed: view admin_training_summary (training_id + contentLang) + training (location) + training_lecturer (koolitajad sort_order järjekorras) + kuvatud tõlke description. Kui contentLang tõlget pole, on title, description, categoryName ja trainingTranslationId põhikeele omad. lecturers on tühi list, kui koolitajaid pole. fundingTypes võib olla tühi list. Kasutavad AdminTrainingCoursesView (koolituse kaart) ja CourseFormView (pealkiri; koolitajad uue toimumiskorra eeltäitmiseks). Kustutatud koolitus (status "D") = olematu.

Veateated:
HTTP: 404
errorCode: PRIMARY_KEY_NOT_FOUND
message: "Ei leidnud primary keyd 'trainingId' väärtusega: 123"
```

## API märkmed — GET /api/training/{trainingId}/courses

```text
API: GET /api/training/{trainingId}/courses

Query parameetrid:
includePast: Boolean — true = ka möödunud toimumiskorrad (end_date < täna); valikuline, vaikimisi false

Response (200):
CourseSummaryDto.java
[
  {
    "courseId": 1,
    "startDate": "2026-10-05",
    "endDate": "2026-10-09",
    "numberOfDays": 5,
    "numberOfAcademicHours": 40,
    "price": 490.00,
    "lecturerNames": "Rain Tüür, Meelis Teern",
    "roomName": "Assauwe",
    "status": "O",
    "isPast": false,
    "participantCount": 1,
    "hasNotes": true,
    "hasMeetingLink": false
  },
  ...
]

API teenuse lisainfo:
Andmed tulevad view'st course_summary. Kustutatud (status "D") toimumiskordi ei tagastata. status: "U" = mustand, "O" = avatud, "F" = täis, "X" = tühistatud. Järjestus: tulevased enne möödunuid, tulevased lähimast, möödunud hiliseimast (is_past, days_from_today, course_id). lecturerNames = koolitajate nimed komadega sort_order järjekorras (course_lecturer); lecturerNames ja roomName on null, kui koolitajad / ruum on valimata. notes ja meetingLink sisu ei tagastata, ainult hasNotes/hasMeetingLink. participantCount = course_participant ridade arv. Tühi list, kui toimumiskordi pole. Kustutatud koolitus (status "D") = olematu.

Veateated:
HTTP: 404
errorCode: PRIMARY_KEY_NOT_FOUND
message: "Ei leidnud primary keyd 'trainingId' väärtusega: 123"
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
