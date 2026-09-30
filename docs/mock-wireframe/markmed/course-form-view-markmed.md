# CourseFormView.vue — märkmed

Toimumiskorra vorm (uus ja muutmine). Otsused ja skeemid: `docs/mock-wireframe/loo-mock-vaade/training-courses-view/training-courses-view-skeemid.md`. Vorm kasutab ka teenuseid `GET /api/admin-training/{trainingId}`, `GET /api/lecturer-summary/{lecturerId}` ja `DELETE /api/course/{courseId}` — nende märkmed on failis `training-courses-view-markmed.md`. Uute teenuste DTO-d on ettepanek.

## Vaate märkmed

```text
Roll: Admin
Failinimi: CourseFormView.vue
Frontend rada: /course-form?trainingId={id} (uus), /course-form?courseId={id} (muutmine)

Vaatega seotud lisainfo:
trainingId-ga avatuna on pealkiri "Uus toimumiskord": staatus vaikimisi "Mustand", koolitaja = koolituse vaikimisi koolitaja (GET /api/admin-training/{trainingId}). courseId-ga avatuna "Toimumiskorra muutmine": väljad täidetakse GET /api/course/{courseId} vastusest ja selle trainingId järgi laaditakse koolituse nimi; lisaks on prügikasti ikoon (CourseDeleteButton.vue). Pealkirja all koolituse nimi, kiirnupp "Kalender" → /training-courses?trainingId={id}.
Koolitaja valitakse "Vali koolitaja" modaliga (LecturerSelectModal.vue, GET /api/lecturers, ainult aktiivsed koolitajad); nupu all on valitud koolitaja kaart (LecturerCard.vue: pilt, nimi, ametinimetus, lühikirjeldus — GET /api/lecturer-summary/{lecturerId}), ruum rippmenüüst (GET /api/rooms, esimene valik "Ruum puudub"). Staatuse rippmenüüs Mustand / Avatud / Täis / Tühistatud. Kui algus ja lõpp on valitud ja admin pole päevade arvu ise muutnud, täidab vorm selle tööpäevade (E–R) arvuga ("Arvutatud tööpäevadest, saad muuta").
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
  "lecturerId": 2,
  "lecturerName": "Jaan Kask",
  "roomId": 2,
  "status": "X",
  "notes": "Tühistatud koolitaja haiguse tõttu.",
  "meetingLink": null
}

API teenuse lisainfo:
Toimumiskorra andmed muutmise vormi jaoks. lecturerId/lecturerName, roomId, notes ja meetingLink võivad olla null. trainingId järgi laadib vorm koolituse nime (GET /api/admin-training/{trainingId}). Kustutatud toimumiskord (status "D") = olematu.

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
  "lecturerId": 1,
  "roomId": 1,
  "status": "U",
  "notes": "",
  "meetingLink": ""
}

Response (200): NONE

API teenuse lisainfo:
Lisab koolitusele toimumiskorra; created_by = userId (sisselogitud kasutaja). Kohustuslikud: userId, startDate, endDate, numberOfDays (≥ 1), numberOfAcademicHours (≥ 1), price (≥ 0), status ("U" / "O" / "F" / "X"; muu väärtus → 400). lecturerId ja roomId võivad olla null; tühi notes/meetingLink salvestatakse null-ina. meetingLink max 255 märki. numberOfDays saadab frontend (backend kuupäevadest ei arvuta). Toimumiskorra saab lisada ka mustandis koolitusele, mitte kustutatud koolitusele.

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
  "lecturerId": 2,
  "roomId": 2,
  "status": "X",
  "notes": "Tühistatud koolitaja haiguse tõttu.",
  "meetingLink": ""
}

Response (200): NONE

API teenuse lisainfo:
Muudab toimumiskorra kõiki välju (ka staatust — eraldi tegevusteenuseid pole). Valideerimine sama mis POST puhul; status "D" ei ole lubatud (kustutamiseks DELETE). Uuendab updated_at. Kustutatud toimumiskord (status "D") = olematu.

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
    "roomName": "A101",
    "roomStatus": "VAB"
  },
  ...
]

API teenuse lisainfo:
Kõik ruumid nime järgi. CourseFormView kasutab neid ruumi rippmenüüs (esimene valik "Ruum puudub" = null). roomStatus tähendus (VAB / KIN) on lahtine küsimus — praegu kuvatakse kõik ruumid.

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
    "lecturerName": "Mari Tamm"
  },
  ...
]

API teenuse lisainfo:
Olemasolev teenus; muudatused: lecturerPhoto eemaldatakse DTO-st (pilt on nüüd eraldi tabelis lecturer_photo ja otsingus seda ei loeta) ja tagastatakse ainult aktiivsed koolitajad (lecturer.status = "A"). Tagastab koolitajad, kelle nimi sisaldab otsingusõna (tõstutundetu), nime järgi tähestikuliselt. CourseFormView kasutab seda "Vali koolitaja" modalis (LecturerSelectModal.vue) nagu TrainingFormView.

Veateated: —
```
