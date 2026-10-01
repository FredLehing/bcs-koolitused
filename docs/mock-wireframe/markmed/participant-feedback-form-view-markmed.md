# ParticipantFeedbackFormView.vue — märkmed

Osaleja tagasiside vorm toimunud koolitusele (kaks olekut: `hasFeedback = false` → uus tagasiside, `hasFeedback = true` → olemasoleva vaatamine ja muutmine). Otsused, andmebaasi ettepanek ja skeemid: `docs/mock-wireframe/loo-mock-vaade/participant-feedback-form-view/participant-feedback-form-view-skeemid.md`. Interaktiivne läbimäng: `participant-feedback-form-view-labimang.html`. Teenused ja DTO-d on ettepanek (koodi veel pole). Vaate avamise nupud on vaates `ParticipantCoursesView.vue` (vt `participant-courses-view-markmed.md`).

## Vaate märkmed

```text
Roll: Kasutaja (osaleja); admin → /not-authorized
Failinimi: ParticipantFeedbackFormView.vue
Frontend rada: /participant-feedback-form?courseParticipantId={id}

Vaatega seotud lisainfo:
Avatakse "Minu koolitused" nupust "Anna tagasisidet" / "Vaata tagasisidet". Vasakul ProfileMenu.vue (aktiivne "Minu koolitused"), kaardi ülaosas link "← Tagasi minu koolituste juurde" (/participant-courses). Sisselogimata → /login?redirect={rada koos parameetriga}; courseParticipantId puudub → ErrorView. Vaate avamisel ja keele vahetusel GET .../feedback?contentLang={UI keel}; 403 FEEDBACK_NOT_ALLOWED / 404 REGISTRATION_NOT_FOUND → punane teade backendi tekstiga, vormi pole.

Päis: "Tagasiside", koolituse nimi · toimumisaeg dd/MM/yyyy – dd/MM/yyyy; olemasoleval tagasisidel "Esitatud dd/MM/yyyy" ja muudetul "Muudetud dd/MM/yyyy" (updatedAt kuupäev ≠ createdAt kuupäev). Staatust (N/U/H) osalejale ei näidata. Vihje "Hinda iga väidet skaalal 1–10 (1 = ei nõustu üldse, 10 = nõustun täielikult)".

Iga kriteerium (FeedbackCriteriaItem.vue): title + (?) → hover/fookus tooltip description; raadionupud 1–10 (kohustuslik); link "+ Lisa kommentaar" / "− Peida kommentaar" avab/peidab textarea (3 rida, placeholder "Täpsusta soovi korral oma hinnangut", loendur "0 / 10000"). Peitmine teksti ei kustuta. Olemasoleva kommentaariga kast on kohe lahti; lugemisrežiimis kommentaarita kriteeriumil linki pole.

hasFeedback = false → väljad kohe täidetavad, nupp "Lisa tagasiside" → POST. hasFeedback = true → väljad disabled, nupp "Muuda" → väljad muudetavaks, nupud "Salvesta" (PUT) ja "Tühista" (laaditud väärtused tagasi, lugemisrežiim). Enne saatmist: hinne puudub → "Hinda kõiki kriteeriume" (InlineAlerts.vue nuppude kõrval), hindamata kriteeriumid punase äärisega, päringut ei tehta; tühi kommentaar saadetakse null-ina. Edu → "Tagasiside salvestatud", GET uuesti, lugemisrežiim. 403 FEEDBACK_ALREADY_EXISTS / FEEDBACK_CRITERIA_CHANGED, 404 FEEDBACK_NOT_FOUND → backendi teade ja GET uuesti.
```

## API märkmed — GET /api/user/{userId}/registration/{courseParticipantId}/feedback

```text
API: GET /api/user/{userId}/registration/{courseParticipantId}/feedback

Query parameetrid:
contentLang: String — kriteeriumide ja koolituse nime keel ("et"/"en")

Response (200):
ParticipantFeedbackDto.java
{
  "courseParticipantId": 10,
  "trainingTitle": "Java algkursus",
  "startDate": "2026-06-08",
  "endDate": "2026-06-12",
  "hasFeedback": true,
  "createdAt": "2026-06-12T16:40:00",
  "updatedAt": "2026-06-12T16:40:00",
  "criteria": [
    {
      "feedbackCriteriaId": 1,
      "title": "Koolitus vastas ootustele",
      "description": "Koolituse sisu, tase ja maht vastasid koolituse kirjelduse põhjal tekkinud ootustele.",
      "score": 9,
      "feedbackText": null
    },
    ...
  ]
}

API teenuse lisainfo:
Üks päring mõlemale olekule. hasFeedback = false → criteria = aktiivsed (A) kriteeriumid; score, feedbackText, createdAt, updatedAt on null. hasFeedback = true → criteria = aktiivsed + kustutatud (D), millele on vastatud; hiljem lisatud kriteeriumi score = null. Järjestus sequence, id. title/description ja trainingTitle contentLang keeles, puudumisel põhikeeles. updatedAt = vastuste MAX(updated_at). Kontrollid: registreerumine kuulub kasutaja osalejale, status "R", end_date <= täna, toimumiskord pole "X" ega "D".

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
errorCode: FEEDBACK_NOT_ALLOWED
message: "Sellele koolitusele ei saa tagasisidet anda"
```

## API märkmed — POST /api/user/{userId}/registration/{courseParticipantId}/feedback

```text
API: POST /api/user/{userId}/registration/{courseParticipantId}/feedback

Request body:
FeedbackRequestDto.java
{
  "answers": [
    {
      "feedbackCriteriaId": 1,
      "score": 9,
      "feedbackText": "Praktilisi näiteid oleks võinud olla rohkem"
    },
    ...
  ]
}

Response (200): NONE

API teenuse lisainfo:
Loob feedback rea (status "N") ja iga vastuse kohta course_participant_feedback rea ühes transaktsioonis. answers* (@NotEmpty), feedbackCriteriaId*, score* (1–10), feedbackText (max 10000, valikuline; tühi → NULL). answers peab sisaldama täpselt kõiki aktiivseid kriteeriume, igaüht üks kord. Tingimused nagu GET-il.

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
errorCode: FEEDBACK_NOT_ALLOWED
message: "Sellele koolitusele ei saa tagasisidet anda"

HTTP: 403
errorCode: FEEDBACK_ALREADY_EXISTS
message: "Tagasiside on juba antud"

HTTP: 403
errorCode: FEEDBACK_CRITERIA_CHANGED
message: "Tagasiside küsimused on vahepeal muutunud, laadi leht uuesti"

HTTP: 400
errorCode: INCORRECT_INPUT
message: "answers[0].score: must be less than or equal to 10"
```

## API märkmed — PUT /api/user/{userId}/registration/{courseParticipantId}/feedback

```text
API: PUT /api/user/{userId}/registration/{courseParticipantId}/feedback

Request body:
FeedbackRequestDto.java
{
  "answers": [
    {
      "feedbackCriteriaId": 1,
      "score": 9,
      "feedbackText": "Praktilisi näiteid oleks võinud olla rohkem"
    },
    ...
  ]
}

Response (200): NONE

API teenuse lisainfo:
Uuendab olemasolevad vastused ja lisab puuduvad (hiljem lisatud kriteerium). answers peab sisaldama täpselt aktiivsed kriteeriumid + kustutatud, millele on vastatud. Staatus "H" → "U"; "N" ja "U" jäävad. Tühi feedbackText → NULL. Tingimused nagu GET-il; muutmisel tähtaega pole.

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
errorCode: FEEDBACK_NOT_ALLOWED
message: "Sellele koolitusele ei saa tagasisidet anda"

HTTP: 404
errorCode: FEEDBACK_NOT_FOUND
message: "Tagasisidet ei leitud"

HTTP: 403
errorCode: FEEDBACK_CRITERIA_CHANGED
message: "Tagasiside küsimused on vahepeal muutunud, laadi leht uuesti"

HTTP: 400
errorCode: INCORRECT_INPUT
message: "answers[0].score: must be less than or equal to 10"
```
