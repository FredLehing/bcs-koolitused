# CourseFormView.vue — märkmed

Toimumiskorra vorm (uus ja muutmine). Otsused ja skeemid: `docs/mock-wireframe/loo-mock-vaade/admin-training-courses-view/admin-training-courses-view-skeemid.md`. Vorm kasutab ka teenuseid `GET /api/admin-training/{trainingId}` ja `DELETE /api/course/{courseId}` — nende märkmed on failis `admin-training-courses-view-markmed.md`. Uute teenuste DTO-d on ettepanek.

## Vaate märkmed

```text
Roll: Admin
Failinimi: CourseFormView.vue
Frontend rada: /course-form?trainingId={id} (uus), /course-form?courseId={id} (muutmine)

Vaatega seotud lisainfo:
trainingId-ga avatuna on pealkiri "Uus toimumiskord": staatus vaikimisi "Mustand", koolitajad = koolituse koolitajad samas järjekorras (GET /api/admin-training/{trainingId} → lecturers); edasi on toimumiskorra koolitajad koolitusest sõltumatud. courseId-ga avatuna "Toimumiskorra muutmine": väljad täidetakse GET /api/course/{courseId} vastusest ja selle trainingId järgi laaditakse koolituse nimi; lisaks on prügikasti ikoon (CourseDeleteButton.vue). Pealkirja all koolituse nimi, kiirnupp "Kalender" → /admin-training-courses?trainingId={id}.
Koolitajad: valitud koolitajate nimekiri (× eemaldab, ↑ ↓ muudab järjekorda), "+ Lisa koolitaja" avab "Vali koolitaja" modali (LecturerSelectModal.vue, GET /api/lecturers, ainult aktiivsed; juba valitud ei pakuta); kuvatakse ainult nimed; ruum rippmenüüst (GET /api/rooms, esimene valik "Ruum puudub"). Staatuse rippmenüüs Mustand / Avatud / Täis / Tühistatud. Kui algus ja lõpp on valitud ja admin pole päevade arvu ise muutnud, täidab vorm selle tööpäevade (E–R) arvuga ("Arvutatud tööpäevadest, saad muuta").
Enne saatmist: algus, lõpp, päevi (≥ 1), akad. tunde (≥ 1), hind (≥ 0) ja staatus on kohustuslikud ("Täida kõik kohustuslikud väljad"), lõpp ei tohi olla enne algust ("Lõppkuupäev ei saa olla varasem kui alguskuupäev") — AlertDanger.vue. "Salvesta" → POST / PUT, seejärel kalendrisse eduteatega ("Toimumiskord lisatud" / "Toimumiskord salvestatud"). "Tagasi" → kalendrisse ilma salvestamata. Kustutatud või olematu toimumiskord/koolitus → üldine veavaade.
```

## API märkmed — GET /api/course/{courseId}

```text
API: GET /api/course/{courseId}

Response (200):
CourseDto.java
{
  "courseId": 4,
  "trainingId": 1,
  "startDate": "2026-10-19",
  "endDate": "2026-10-23",
  "numberOfDays": 5,
  "numberOfAcademicHours": 40,
  "price": 490.00,
  "lecturers": [
    {
      "lecturerId": 8,
      "lecturerName": "Meelis Teern"
    },
    ...
  ],
  "roomId": 2,
  "roomName": "Bremeni",
  "status": "X",
  "notes": "Tühistatud koolitaja haiguse tõttu.",
  "meetingLink": null
}

API teenuse lisainfo:
Toimumiskorra andmed muutmise vormi jaoks. lecturers = toimumiskorra koolitajad (course_lecturer) sort_order järjekorras, võib olla tühi list; roomId, roomName, notes ja meetingLink võivad olla null. roomName tagastatakse ka kustutatud ruumi korral — vorm näitab seda rippmenüüs "Bremeni (kustutatud)", sest GET /api/rooms kustutatud ruumi ei tagasta. trainingId järgi laadib vorm koolituse nime (GET /api/admin-training/{trainingId}). Kustutatud toimumiskord (status "D") = olematu.

Veateated:
HTTP: 404
errorCode: PRIMARY_KEY_NOT_FOUND
message: "Ei leidnud primary keyd 'courseId' väärtusega: 123"
```

## API märkmed — POST /api/training/{trainingId}/course

```text
API: POST /api/training/{trainingId}/course

Request body:
CourseCreateRequestDto.java
{
  "userId": 1,
  "startDate": "2027-01-11",
  "endDate": "2027-01-15",
  "numberOfDays": 5,
  "numberOfAcademicHours": 40,
  "price": 490.00,
  "lecturerIds": [
    1,
    8
  ],
  "roomId": 1,
  "status": "U",
  "notes": "",
  "meetingLink": ""
}

Response (200): NONE

API teenuse lisainfo:
Lisab koolitusele toimumiskorra; created_by = userId (sisselogitud kasutaja). Kohustuslikud: userId, startDate, endDate, numberOfDays (≥ 1), numberOfAcademicHours (≥ 1), price (≥ 0), status ("U" / "O" / "F" / "X"; muu väärtus → 400). lecturerIds = koolitajate ID-d järjekorras (sort_order = positsioon), võib olla tühi list — iga uus ID peab olema aktiivne koolitaja (juba seotud kustutatud koolitaja võib jääda), vastasel juhul 404 'lecturerId'; roomId võib olla null; tühi notes/meetingLink salvestatakse null-ina. meetingLink max 255 märki. numberOfDays saadab frontend (backend kuupäevadest ei arvuta). Toimumiskorra saab lisada ka mustandis koolitusele, mitte kustutatud koolitusele.

Veateated:
HTTP: 403
errorCode: COURSE_END_BEFORE_START
message: "Lõppkuupäev ei saa olla varasem kui alguskuupäev"

HTTP: 404
errorCode: PRIMARY_KEY_NOT_FOUND
message: "Ei leidnud primary keyd 'trainingId' väärtusega: 123"
```

## API märkmed — PUT /api/course/{courseId}

```text
API: PUT /api/course/{courseId}

Request body:
CourseUpdateRequestDto.java
{
  "startDate": "2026-10-19",
  "endDate": "2026-10-23",
  "numberOfDays": 5,
  "numberOfAcademicHours": 40,
  "price": 490.00,
  "lecturerIds": [
    8
  ],
  "roomId": 2,
  "status": "X",
  "notes": "Tühistatud koolitaja haiguse tõttu.",
  "meetingLink": ""
}

Response (200): NONE

API teenuse lisainfo:
Muudab toimumiskorra kõiki välju (ka staatust — eraldi tegevusteenuseid pole). Valideerimine sama mis POST puhul; course_lecturer read kirjutatakse lecturerIds järgi üle; status "D" ei ole lubatud (kustutamiseks DELETE). Uuendab updated_at. Kustutatud toimumiskord (status "D") = olematu.

Veateated:
HTTP: 403
errorCode: COURSE_END_BEFORE_START
message: "Lõppkuupäev ei saa olla varasem kui alguskuupäev"

HTTP: 404
errorCode: PRIMARY_KEY_NOT_FOUND
message: "Ei leidnud primary keyd 'courseId' väärtusega: 123"
```

## API märkmed — GET /api/rooms

```text
API: GET /api/rooms

Response (200):
RoomDto.java
[
  {
    "roomId": 1,
    "roomName": "Assauwe"
  },
  ...
]

API teenuse lisainfo:
Aktiivsed ruumid (room.status = "A") nime järgi. CourseFormView kasutab neid ruumi rippmenüüs (esimene valik "Ruum puudub" = null). Kustutatud ruumi ei tagastata; uueks ruumiks seda valida ei saa (POST/PUT → 404 'roomId'), toimumiskorra praegune kustutatud ruum jääb aga alles.

Veateated: —
```

## API märkmed — GET /api/lecturers (olemas, muutub)

```text
API: GET /api/lecturers

Query parameetrid:
search: String — otsingusõna koolitaja nimest (valikuline, "" = kõik)

Response (200):
LecturerDto.java
[
  {
    "lecturerId": 1,
    "lecturerName": "Rain Tüür"
  },
  ...
]

API teenuse lisainfo:
Olemasolev teenus; muudatused: lecturerPhoto eemaldatakse DTO-st (pilt on nüüd eraldi tabelis lecturer_photo ja otsingus seda ei loeta) ja tagastatakse ainult aktiivsed koolitajad (lecturer.status = "A"). Tagastab koolitajad, kelle nimi sisaldab otsingusõna (tõstutundetu), nime järgi tähestikuliselt. CourseFormView kasutab seda "Vali koolitaja" modalis (LecturerSelectModal.vue) nagu TrainingFormView.

Veateated: —
```
