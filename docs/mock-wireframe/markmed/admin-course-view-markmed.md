# AdminCourseView.vue — märkmed

Ühe toimumiskorra ülevaade koos osalejate ja huviliste tabelitega (admin). Otsused, andmebaasi ettepanek ja skeemid: `docs/mock-wireframe/loo-mock-vaade/courses-view/courses-view-skeemid.md`. Interaktiivne läbimäng: `admin-all-courses-view-labimang.html`. Uute teenuste DTO-d on ettepanek.

## Vaate märkmed

```text
Roll: Admin
Failinimi: AdminCourseView.vue
Frontend rada: /admin-course?courseId={id}

Vaatega seotud lisainfo:
Pealkiri "Toimumiskord", all koolituse nimi. Kiirnupud "Muuda" (/course-form?courseId), "Koolituse kalender" (/admin-training-courses?trainingId), "Koolituste kalender" (/admin-all-courses).
Kaart "Toimumiskord" (ainult lugemiseks): Koolitus (link /training), Toimumisaeg, Päevi, Akad. tunde, Hind, Koolitajad, Ruum, Veebilink, Staatus (+ "Toimunud"), Esile tõstetud, Märkmed. Avatud/täis toimumiskorral link "Vaata avalikul lehel" → /course?courseId.
Tabel "Osalejad" (frontendi filtrid): otsing nime/e-posti järgi, Tasumine (Kõik/Tasunud/Tasumata), lüliti "Näita ka loobunud". Veerud Nimi | E-post | Telefon | Registreerus | Tasunud | Sülearvuti | Staatus | Märkmed. All "Kokku N osalejat, neist M tasunud" (registreerunud osalejad).
Tabel "Huvilised" (frontendi filtrid): otsing nime/e-posti/ettevõtte järgi, Staatus (Kõik/Uued/Käsitletud). Veerud Saabunud | Nimi | E-post | Ettevõte | Staatus | silm → /admin-enquiry?enquiryId.
Keele vahetusel laaditakse andmed uuesti. Olematu või kustutatud toimumiskord → üldine veavaade.
```

## API märkmed — GET /api/admin-course/{courseId}

```text
API: GET /api/admin-course/{courseId}

Query parameetrid:
contentLang: String — koolituse nime keel

Response (200):
AdminCourseDto.java
{
  "courseId": 1,
  "trainingId": 1,
  "trainingTranslationId": 1,
  "trainingTitle": "Java algkursus",
  "startDate": "2026-10-05",
  "endDate": "2026-10-09",
  "isPast": false,
  "numberOfDays": 5,
  "numberOfAcademicHours": 40,
  "price": 490.0,
  "status": "O",
  "isPromoted": true,
  "lecturerNames": "Rain Tüür, Meelis Teern",
  "roomName": "Assauwe",
  "meetingLink": null,
  "notes": "Kaasa sülearvuti."
}

API teenuse lisainfo:
admin_course_summary rida (course_id + contentLang) + course tabelist number_of_academic_hours, notes, meeting_link, ruumi nimi ja koolitajad (course_lecturer, sort_order). lecturerNames, roomName, meetingLink, notes võivad olla null. Kustutatud toimumiskord (status "D") või kustutatud koolitus → 404.

Veateated:
HTTP: 404
errorCode: PRIMARY_KEY_NOT_FOUND
message: "Ei leidnud primary keyd 'courseId' väärtusega: 123"
```

## API märkmed — GET /api/course/{courseId}/participants

```text
API: GET /api/course/{courseId}/participants

Response (200):
CourseParticipantDto.java
[
  {
    "courseParticipantId": 1,
    "participantName": "Anna Saar",
    "email": "anna.saar@example.com",
    "phone": "+37256789012",
    "registeredAt": "2026-09-10T09:00:00Z",
    "hasPaid": true,
    "requiresLaptop": true,
    "status": "R",
    "notes": "Registreerus veebilehe kaudu."
  },
  ...
]

API teenuse lisainfo:
course_participant + participant (name) + profile (email, phone). Tagastatakse kõik osalejad (ka "C" = loobunud), järjestus registreerumise aja järgi (created_at kasvavalt). status: "R" = registreerunud, "C" = loobunud. Otsing ja filtrid frontendis. Kustutatud toimumiskord → 404.

Veateated:
HTTP: 404
errorCode: PRIMARY_KEY_NOT_FOUND
message: "Ei leidnud primary keyd 'courseId' väärtusega: 123"
```

## API märkmed — GET /api/course/{courseId}/enquiries

```text
API: GET /api/course/{courseId}/enquiries

Response (200):
CourseEnquiryDto.java
[
  {
    "enquiryId": 1,
    "createdAt": "2026-09-15T05:30:00Z",
    "fullName": "Anna Saar",
    "email": "anna.saar@example.com",
    "companyName": null,
    "status": "U"
  }
]

API teenuse lisainfo:
enquiry + profile, course_id = {courseId}; uusimad eespool. Kõik päringud ("U" ja "H"). Tühja nimekirja korral []. Kustutatud toimumiskord → 404.

Veateated:
HTTP: 404
errorCode: PRIMARY_KEY_NOT_FOUND
message: "Ei leidnud primary keyd 'courseId' väärtusega: 123"
```
