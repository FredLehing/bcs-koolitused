# ParticipantCoursesView.vue — märkmed

"Minu koolitused" — sisseloginud osaleja registreerumised (plokid "Tulevased" ja "Toimunud"), loobumine ja tagasiside nupud. Vaade ja teenused on koodis olemas (`profile-view`); uus on tagasiside nupp ja `MyRegistrationDto` väljad `canGiveFeedback`, `hasFeedback`. Otsused: `docs/mock-wireframe/loo-mock-vaade/profile-view/profile-view-skeemid.md` ja `docs/mock-wireframe/loo-mock-vaade/participant-feedback-form-view/participant-feedback-form-view-skeemid.md`. Läbimängud: `profile-view-labimang.html` (loobumine) ja `participant-feedback-form-view-labimang.html` (tagasiside nupud).

## Vaate märkmed

```text
Roll: Kasutaja (osaleja); admin → /not-authorized
Failinimi: ParticipantCoursesView.vue
Frontend rada: /participant-courses

Vaatega seotud lisainfo:
Avaneb navbari "👤 ▾" (Minu profiil) menüüst ja vasakult ProfileMenu.vue-st. Sisselogimata → /login?redirect=/participant-courses. GET /api/user/{userId}/registrations?contentLang={UI keel} (ka keele vahetusel). Plokk "Tulevased" (lõpp täna või hiljem, alguse järgi kasvavalt; tühjana "Tulevasi koolitusi pole.") ja "Toimunud" (uusimad eespool; ainult kui on). Rida (ParticipantRegistrationItem.vue): koolituse nimi (avatud/täis toimumiskorral link → /course?courseId={id}), toimumisaeg dd/MM/yyyy – dd/MM/yyyy · vorm (Kohapeal / Veebis / Kohapeal + Veebis), märgised Registreerunud / Loobunud, "Tühistatud", Tasutud / Tasumata.

Nupp "Loobu" (canCancel) → ConfirmModal "Kas soovid koolitusest „{koolitus}“ loobuda?" → PUT .../cancel → "Oled koolitusest loobunud", nimekiri uuesti; 403 CANCEL_NOT_ALLOWED / 404 → backendi teade. Tühi nimekiri: "Sa pole veel ühelegi koolitusele registreerunud." + link "Vaata koolitusi" → /courses.

Uus: kui canGiveFeedback, on rea paremas servas (samas kohas kui "Loobu") nupp "Anna tagasisidet" (hasFeedback = false) või "Vaata tagasisidet" (hasFeedback = true) → /participant-feedback-form?courseParticipantId={id}. Viimasel koolituspäeval on rida veel "Tulevased" plokis, nupp kuvatakse ka seal.
```

## API märkmed — GET /api/user/{userId}/registrations

```text
API: GET /api/user/{userId}/registrations

Query parameetrid:
contentLang: String — koolituse nime keel ("et"/"en")

Response (200):
MyRegistrationDto.java
[
  {
    "courseParticipantId": 2,
    "courseId": 3,
    "trainingTitle": "Java algkursus",
    "startDate": "2026-09-07",
    "endDate": "2026-09-11",
    "isOnSite": true,
    "isOnline": false,
    "courseStatus": "O",
    "status": "R",
    "hasPaid": true,
    "isPast": true,
    "canCancel": false,
    "canGiveFeedback": true,
    "hasFeedback": false
  },
  ...
]

API teenuse lisainfo:
Olemasolev teenus, uued väljad canGiveFeedback ja hasFeedback. Kasutaja osaleja kõik registreerumised (ka loobunud), kustutatud toimumiskorrad (D) välja, järjestus start_date kasvavalt; osaleja puudumisel tühi list. isPast = end_date < täna. canCancel = status "R", start_date > täna, toimumiskord pole "X" ega "D". canGiveFeedback = status "R", end_date <= täna, toimumiskord pole "X" ega "D" (tasumine ei loe). hasFeedback = registreerumisel on feedback rida.

Veateated:
HTTP: 404
errorCode: PRIMARY_KEY_NOT_FOUND
message: "Ei leidnud primary keyd 'userId' väärtusega: 123"
```

## API märkmed — PUT /api/user/{userId}/registration/{courseParticipantId}/cancel

```text
API: PUT /api/user/{userId}/registration/{courseParticipantId}/cancel

Response (200): NONE

API teenuse lisainfo:
Olemasolev teenus, muudatust pole. course_participant.status = "C". Registreerumine peab kuuluma kasutaja osalejale. Lubatud ainult kui status "R", toimumiskord pole alanud ega tühistatud/kustutatud (sama reegel mis canCancel).

Veateated:
HTTP: 404
errorCode: PRIMARY_KEY_NOT_FOUND
message: "Ei leidnud primary keyd 'userId' väärtusega: 123"

HTTP: 404
errorCode: PRIMARY_KEY_NOT_FOUND
message: "Ei leidnud primary keyd 'courseParticipantId' väärtusega: 123"

HTTP: 404
errorCode: REGISTRATION_NOT_FOUND
message: "Registreerumist ei leitud"

HTTP: 403
errorCode: CANCEL_NOT_ALLOWED
message: "Sellest registreerumisest ei saa enam loobuda"
```
