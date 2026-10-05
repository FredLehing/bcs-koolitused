# CourseRegistrationView.vue — märkmed

Toimumiskorrale registreerumine (ainult sisseloginud kasutaja, registreerib iseennast). Otsused, andmebaasi ettepanek ja skeemid: `docs/mock-wireframe/loo-mock-vaade/courses-view/courses-view-skeemid.md`. Interaktiivne läbimäng: `course-registration-view-labimang.html`. Uute teenuste DTO-d on ettepanek; olemas: `GET /api/course-summary/{courseId}`.

## Vaate märkmed

```text
Roll: Kasutaja (sisse logitud, roll participant)
Failinimi: CourseRegistrationView.vue
Frontend rada: /course-registration?courseId={id}

Vaatega seotud lisainfo:
Avaneb /course lehe nupust "Registreeru". Sisse logimata kasutaja suunatakse /login?redirect=/course-registration?courseId={id}; pärast sisselogimist (või konto loomist) tullakse siia tagasi. Admin → /not-authorized.
Pealkiri "Registreerumine". Vasakul kaart "Toimumiskord" (GET /api/course-summary/{courseId}): koolitus, toimumisaeg, päevi/tunde, hind, toimumisviis, koolitajad; link "Tagasi toimumiskorra lehele".
Paremal kaart "Osaleja andmed": Eesnimi*, Perekonnanimi*, E-post*, Telefon* — eeltäidetud kasutaja osaleja andmetest (GET /api/user/{userId}/participant; kui osalejat pole, ainult konto e-post); linnuke "Vajan koolitusel sülearvutit"; "Lisainfo" (valikuline, nt arve andmed). Muudetud andmed salvestatakse ka kasutaja profiili.
"Registreeru" → frontendi kontroll (kohustuslikud väljad, e-posti kuju) → POST /api/course/{courseId}/participant → /course?courseId={id} koos eduteatega "Registreerumine õnnestus!". "Tühista" → tagasi /course lehele.
Kui kasutaja on juba registreerunud (GET /api/course/{courseId}/participant-status → "R"), vormi ei näidata: "Oled sellele toimumiskorrale juba registreerunud." Täis toimumiskorral: "Kohad on täis — küsi lisainfot toimumiskorra lehelt." Olematu või mitteavalik toimumiskord → üldine veavaade.
```

## API märkmed — GET /api/course-summary/{courseId}

```text
API: GET /api/course-summary/{courseId}

Query parameetrid:
contentLang: String — koolituse teksti keel (puudumisel põhikeel)

Response (200):
CoursePageDto.java
{
  "courseId": 9,
  "trainingId": 3,
  "trainingTranslationId": 5,
  "isMainLanguageFallback": false,
  "title": "Spring Boot veebiarendus",
  "shortDescription": "REST API-de loomine Spring Booti abil.",
  "description": "<p>…</p>",
  "categoryName": "Programmeerimine",
  "trainingLanguageFlagIconCode": "fi-ee",
  "fundingTypes": [ ... ],
  "startDate": "2026-10-12",
  "endDate": "2026-10-15",
  "isPast": false,
  "numberOfDays": 4,
  "numberOfAcademicHours": 32,
  "price": 560.0,
  "status": "O",
  "isOnSite": true,
  "isOnline": false,
  "lecturers": [ { "lecturerId": 1, "fullName": "Rain Tüür", "title": "Lektor/konsultant", "shortDescription": "Java koolitaja.", "photoVersion": 1784106000 } ],
  "upcomingCourses": [
    { "courseId": 1, "startDate": "2026-10-05", "endDate": "2026-10-09", "status": "O", "isOnSite": true, "isOnline": false },
    { "courseId": 5, "startDate": "2026-11-16", "endDate": "2026-11-20", "status": "F", "isOnSite": false, "isOnline": true }
  ]
}

API teenuse lisainfo:
Leiab ainult toimumiskorra, mille status on "O" või "F" ja koolitus publitseeritud ("P"); muidu 404 (sama message nagu olematu ID korral). Möödunud toimumiskord leitakse (isPast = true). Tekst contentLang keeles, puudumisel põhikeeles (isMainLanguageFallback = true). lecturers course_lecturer sort_order järjekorras (võib olla tühi). upcomingCourses = sama koolituse kõik public_course_summary read (ka päritud toimumiskord ise, kui see on tulevane), alguse järgi — frontend tõstab praeguse esile.

Veateated:
HTTP: 404
errorCode: PRIMARY_KEY_NOT_FOUND
message: "Ei leidnud primary keyd 'courseId' väärtusega: 123"
```

## API märkmed — GET /api/course/{courseId}/participant-status

```text
API: GET /api/course/{courseId}/participant-status

Query parameetrid:
userId: Integer — sisseloginud kasutaja (sessionStorage)

Response (200):
CourseParticipantStatusDto.java
{
  "status": "R"
}

API teenuse lisainfo:
Kasutaja oma osaleja (participant.user_id = userId) course_participant rea staatus: "R" = registreerunud, "C" = loobunud, null = pole registreerunud (ka siis, kui kasutajal osalejat veel pole). Kutsutakse ainult sisseloginud kasutaja korral.

Veateated:
HTTP: 404
errorCode: PRIMARY_KEY_NOT_FOUND
message: "Ei leidnud primary keyd 'courseId' väärtusega: 123" (sama 'userId' kohta)
```

## API märkmed — GET /api/user/{userId}/participant

```text
API: GET /api/user/{userId}/participant

Response (200):
MyParticipantDto.java
{
  "participantId": 1,
  "firstName": "Anna",
  "lastName": "Saar",
  "email": "anna.saar@example.com",
  "phone": "+37256789012"
}

API teenuse lisainfo:
Kasutaja oma osaleja (participant.user_id = userId) ja selle profiil. Kui osalejat veel pole (nt admini loodud konto), tagastatakse participantId = null, nimed ja telefon tühjad ning email = user.email.

Veateated:
HTTP: 404
errorCode: PRIMARY_KEY_NOT_FOUND
message: "Ei leidnud primary keyd 'userId' väärtusega: 123"
```

## API märkmed — POST /api/course/{courseId}/participant

```text
API: POST /api/course/{courseId}/participant

Request body:
CourseRegistrationRequest.java
{
  "userId": 2,
  "firstName": "Anna",
  "lastName": "Saar",
  "email": "anna.saar@example.com",
  "phone": "+37256789012",
  "requiresLaptop": true,
  "notes": "Arve ettevõttele: OÜ Näidis"
}

Response (200): NONE

API teenuse lisainfo:
Kasutaja registreerib iseennast. Backend leiab kasutaja osaleja (participant.user_id = userId); kui seda pole, loob profile + participant (name = eesnimi + perekonnanimi). Olemasoleva osaleja profiil ja nimi uuendatakse vormi andmetega.
course_participant: uus rida (status "R", has_paid = false, requires_laptop, notes — tühi → ""); kui samal osalejal on sellele toimumiskorrale "C" (loobunud) rida, muudetakse see tagasi "R"-iks.
Toimumiskord peab olema publitseeritud koolituse "O" toimumiskord ja algama täna või hiljem.

Veateated:
HTTP: 400 — valideerimise viga (@NotBlank, @Email, @Size)
HTTP: 403
errorCode: COURSE_FULL
message: "Toimumiskord on täis"
HTTP: 403
errorCode: ALREADY_REGISTERED
message: "Oled sellele toimumiskorrale juba registreerunud"
HTTP: 403
errorCode: REGISTRATION_CLOSED
message: "Registreerumine on lõppenud" (toimumiskord on alanud või möödunud)
HTTP: 404
errorCode: PRIMARY_KEY_NOT_FOUND
message: "Ei leidnud primary keyd 'courseId' väärtusega: 123" (ka mustand, tühistatud, kustutatud)
HTTP: 404
errorCode: PRIMARY_KEY_NOT_FOUND
message: "Ei leidnud primary keyd 'userId' väärtusega: 123"
```


## Tagasitee — täiendatud navigatsioon

Vaade võtab vastu valikulise `returnTo` query parameetri ja kuvab lingi „← Tagasi“ (`BackLink.vue`). Avamislingid annavad kaasa lähtevaate täieliku URL-i. Tagasilingi puuduv, väline, tundmatu või iseendale osutav siht asendatakse vaate varusihtkohaga. Oleku- ja tõlkevahetus ei kaota tagasiteed. Eraldi nimega nimekirja-/kalendrinupud säilitavad oma sihtkoha. Täpne [kaardistus ja varusihtkohad](../../tasks/frontend/return-to-navigation.md) ning [skeemid](../loo-mock-vaade/return-to-navigation-skeemid.md).
