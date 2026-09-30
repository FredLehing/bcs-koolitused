# TrainingCoursesView.vue — märkmed

Koolituse kalender: koolituse andmed ja toimumiskordade tabel. Otsused, andmebaasi view ettepanek ja skeemid: `docs/mock-wireframe/loo-mock-vaade/training-courses-view/training-courses-view-skeemid.md`. Koolitaja kaardi teenus `GET /api/lecturer-summary/{lecturerId}` on defineeritud `admin-lecturers-view-skeemid.md`-s. Uute teenuste (`GET /api/admin-training/{trainingId}`, `GET /api/training/{trainingId}/courses`, `DELETE /api/course/{courseId}`) DTO-d on ettepanek.

## Vaate märkmed

```text
Roll: Admin
Failinimi: TrainingCoursesView.vue
Frontend rada: /training-courses?trainingId={id}

Vaatega seotud lisainfo:
Avaneb AdminTrainingsView rea ikoonist "Kalender" ja TrainingFormView kiirnupust "Kalender". Ülal koolituse kaart (TrainingSummaryCard.vue): nimi, staatus, kategooria, õppekeele lipp, toimumiskoht, vaikimisi koolitaja kaart (LecturerCard.vue: pilt, nimi, ametinimetus, lühikirjeldus — laeb ise GET /api/lecturer-summary/{lecturerId}), rahastus, sätted ning lingid "Vaata" ja "Muuda"; andmed kasutajaliidese keeles (puuduva tõlke korral põhikeeles), keele vahetusel laaditakse uuesti. Kirjelduse kaart on vaikimisi peidus ("▾ Näita kirjeldust" / "▴ Peida kirjeldus").
Tabelis on toimumiskorrad: tulevased eespool (lähim üleval), lüliti "Näita ka möödunud" lisab möödunud (hiliseim üleval) tuhmimalt ja märgisega "Toimunud" — see tuleneb kuupäevast, mitte staatusest. Kustutatud toimumiskordi ei kuvata. Märkmete ja veebilingi sisu ei näidata, ainult ✓/✗. Kuupäevad kujul 30/09/2026, puuduv koolitaja/ruum "—". Leheküljestust pole, all "Kokku N toimumiskorda". Veerud Algus, Hind, Staatus ja Osalejaid on sorteeritavad ainult frontendis (API kutset ei tehta): 1. klõps kasvav, 2. kahanev, 3. tagasi vaikimisi järjestusse; staatus järjekorras Mustand → Avatud → Täis → Tühistatud.
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
  "defaultLecturerId": 1,
  "defaultLecturerName": "Mari Tamm",
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
Ühe koolituse admini ülevaade (ka mustand). Andmed: view admin_training_summary (training_id + contentLang) + training (location, default_lecturer) + kuvatud tõlke description. Kui contentLang tõlget pole, on title, description, categoryName ja trainingTranslationId põhikeele omad. defaultLecturerId ja defaultLecturerName on null, kui vaikimisi koolitajat pole. Koolitaja pilti, ametinimetust ja lühikirjeldust siin ei tagastata — need tulevad koolitaja kaardile teenusest GET /api/lecturer-summary/{lecturerId} (defaultLecturerId järgi). fundingTypes võib olla tühi list. Kasutavad TrainingCoursesView (koolituse kaart) ja CourseFormView (pealkiri, vaikimisi koolitaja). Kustutatud koolitus (status "D") = olematu.

Veateated:
HTTP: 404
errorCode: PRIMARY_KEY_NOT_FOUND
message: "Ei leidnud primary keyd 'trainingId' väärtusega: 123"
```

## API märkmed — GET /api/lecturer-summary/{lecturerId}

```text
API: GET /api/lecturer-summary/{lecturerId}

Query parameetrid:
contentLang: String — ametinimetuse ja lühikirjelduse keel ("et"/"en")

Response (200):
LecturerSummaryDto.java
{
  "lecturerId": 1,
  "fullName": "Mari Tamm",
  "title": "Tarkvaraarendaja ja Java koolitaja",
  "shortDescription": "Üle 10 aasta kogemust tarkvaraarenduse koolitajana.",
  "photo": "iVBORw0KGgoAAAANSUhEUgAAACAAAAAg...",
  "photoContentType": "image/png"
}

API teenuse lisainfo:
Koolitaja kaardi (LecturerCard.vue) andmed: lecturer + lecturer_translation contentLang keeles (puudumisel põhikeeles) + lecturer_photo. title = ametinimetus. photo ja photoContentType on null, kui pilti pole (kaardil kohatäite ikoon); frontend kuvab pildi kujul data:{photoContentType};base64,{photo}. Kustutatud koolitaja (status "D") on nagu olematu — LecturerCard 404 korral kaarti ei kuvata (üldisele veavaatele ei suunata). Kasutavad TrainingView (/training, parem veerg "Koolitaja"), TrainingCoursesView (vaikimisi koolitaja) ja CourseFormView (valitud koolitaja). Teenus on defineeritud admin-lecturers-view skeemides.

Veateated:
HTTP: 404
errorCode: PRIMARY_KEY_NOT_FOUND
message: "Ei leidnud primary keyd 'lecturerId' väärtusega: 123"
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
    "lecturerName": "Mari Tamm",
    "roomName": "A101",
    "status": "O",
    "isPast": false,
    "participantCount": 1,
    "hasNotes": true,
    "hasMeetingLink": false
  },
  ...
]

API teenuse lisainfo:
Andmed tulevad view'st course_summary. Kustutatud (status "D") toimumiskordi ei tagastata. status: "U" = mustand, "O" = avatud, "F" = täis, "X" = tühistatud. Järjestus: tulevased enne möödunuid, tulevased lähimast, möödunud hiliseimast (is_past, days_from_today, course_id). lecturerName ja roomName on null, kui koolitaja/ruum on valimata. notes ja meetingLink sisu ei tagastata, ainult hasNotes/hasMeetingLink. participantCount = course_participant ridade arv. Tühi list, kui toimumiskordi pole. Kustutatud koolitus (status "D") = olematu.

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
