# TrainingView.vue — koolituse detail

## Vaate märkmed

```text
Roll: Kõik rollid (admin näeb muutmise ikooni)
Failinimi: TrainingView.vue
Frontend rada: /training?trainingId={id}&trainingTranslationId={id}

Vaatega seotud lisainfo:
trainingTranslationId on valikuline kindla tõlke eelvaade. Pealkirja järel olev admini muutmise ikoon avab vormi mõlema ID ja returnTo väärtusega.
Koolituse info all kuvatakse ainult valitud tõlke PDF-i kaart; varuteksti korral teise keele faili ei pakuta.
Tagasi taastab returnTo või /trainings; koolitaja lingid annavad kaasa lähte-URL-i. „Järgmised koolitused“ lingid avavad /course ilma returnTo-ta.
Puuduv koolitus annab veateate; tühjad järgmised koolitused annavad selgitava teksti.
```

Koolituse info all on „Õppekava (PDF)“ kaart ainult valitud tõlke faili
olemasolul. Failinimi on allalaadimislink, kõrval suurus MB-des. Link kasutab
`training-form` kaudu salvestatud faili. Teise keele faili ei kasutata varuna.

Paremal on „Koolituse andmed“ (õppekeel, kategooria, rahastus, asukoht,
tellitavus), koolitajate kaardid, sama koolituse „Järgmised koolitused“
(kuupäevad, toimumisviis ja vajadusel Täis märgis) ning üldkalendri link.
Koolitajakaart saab kõik tekstid ja foto versiooni koondvastuse lecturers massiivist; eraldi JSON-päringuid kaart ei tee. Kustutatud koolitajad jäetakse backendis välja.
Koolitajate kaart puudub tühja nimekirja korral; tulevaste koolituste kaart
kuvab sel juhul selgitava teate.

Tagasilink taastab `returnTo`, selle puudumisel `/trainings`. Edasised
koolitaja lingid lisavad kogu lähte-URL-i. „Järgmised koolitused“ kaardi
lingid avavad `/course?courseId={id}` ilma `returnTo` parameetrita; toimumiskorra
tagasilingi varusiht on `/courses`. Keelevahetus säilitab
tagasitee, eemaldab konkreetse tõlke eelvaate ID ja laadib andmed uuesti.
Hilinenud vastused ei asenda uuema keele/koolituse andmeid.

## API märkmed — koolituse detail

```text
API: GET /api/training-summary/{trainingId}

Query parameetrid:
contentLang: String — kuvatava sisu keel
trainingTranslationId: Integer — konkreetse tõlke eelvaade (valikuline)

Response (200):
TrainingPageDto.java
{
  "trainingId": 1,
  "trainingTranslationId": 1,
  "isMainLanguageFallback": false,
  "title": "Java algkursus",
  "shortDescription": "Java programmeerimise alused algajatele.",
  "description": "<p>Koolituse kirjeldus.</p>",
  "categoryName": "Programmeerimine",
  "trainingLanguageCode": "et",
  "trainingLanguageFlagIconCode": "ee",
  "locationName": "BCS Koolitus",
  "isOnline": false,
  "isOrderable": true,
  "isPromoted": true,
  "curriculumFileName": "Java-algkursus_oppekava_et.pdf",
  "curriculumFileSize": 419430,
  "fundingTypes": [{"fundingTypeId": 1, "fundingTypeName": "Töötukassa"},
    ...
  ],
  "lecturers": [{"lecturerId": 1, "fullName": "Rain Tüür", "title": "Lektor/konsultant", "shortDescription": "Java koolitaja.", "photoVersion": 1784106000},
    ...
  ],
  "upcomingCourses": [{"courseId": 1, "startDate": "2026-10-05", "endDate": "2026-10-09", "status": "O", "isOnSite": true, "isOnline": false},
    ...
  ]
}

API teenuse lisainfo:
Tõlke valik: koolitusele kuuluv antud tõlge → contentLang → põhikeel. Varuteksti puhul PDF-i väljad null.
Koondpäring ei loe faili baite. upcomingCourses: avalik koolitus P, toimumiskord O/F ja algus vähemalt täna.

Veateated:
HTTP: 404
errorCode: PRIMARY_KEY_NOT_FOUND
message: "Ei leidnud primary keyd 'trainingId' väärtusega: 10"

HTTP: 400
Vigane ID tüüp või puuduv contentLang — Springi sisendiviga.
```

## API märkmed — PDF-i allalaadimine

```text
API: GET /api/training-translation/{trainingTranslationId}/curriculum

Response (200):
application/pdf — failibaidid (DTO puudub)

API teenuse lisainfo:
Olemasolev teenus: Content-Disposition attachment ja serveri failinimi. Link kasutab ainult valitud tõlke ID-d.

Veateated:
HTTP: 404
errorCode: PRIMARY_KEY_NOT_FOUND
message: "Ei leidnud primary keyd 'trainingTranslationId' väärtusega: 11"
```

## API märkmed — koolitaja foto

```text
API: GET /api/lecturer/{lecturerId}/photo

Query parameetrid:
v: Long — photoVersion cache-busting URL-is (valikuline)

Response (200):
image/jpeg — pildibaidid (DTO puudub)

API teenuse lisainfo:
Olemasolev LecturerAvatar: ainult photoVersion olemasolul; muidu kohatäide.

Veateated:
HTTP: 404
errorCode: PRIMARY_KEY_NOT_FOUND
message: "Ei leidnud primary keyd 'lecturerId' väärtusega: 1"
```

[Täpne koondteenuse leping](../../tasks/backend/GET-api-training-summary-trainingId.md),
[interaktiivne mock](../loo-mock-vaade/training-view/training-view-labimang.html),
[paigutus ja vooskeemid](../loo-mock-vaade/training-view/training-view-skeemid.md).
