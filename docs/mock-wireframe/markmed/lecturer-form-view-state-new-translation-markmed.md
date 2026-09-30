# LecturerFormView.vue (state: "new-translation") — märkmed

Üks kolmest LecturerFormView oleku märkmete failist (vt ka teisi `lecturer-form-view-state-*-markmed.md` faile). Olekud ja tõlkeloogika on samad mis TrainingFormView-l. Otsused, andmebaasi ettepanek ja skeemid: `docs/mock-wireframe/loo-mock-vaade/admin-lecturers-view/admin-lecturers-view-skeemid.md`. Uute teenuste DTO-d on ettepanek.

## Vaate märkmed

```text
Roll: Admin
Failinimi: LecturerFormView.vue
Frontend rada: /lecturer-form?lecturerId={id}&languageId={id}

Vaatega seotud lisainfo:
state: "new-translation" — URL-is lecturerId ja languageId (keel, mille tõlge puudub). Pealkiri "Lisa koolitaja tõlge". Täisnimi ja pilt on kirjutuskaitstud; ametinimetus, lühikirjeldus ja kirjeldus eeltäidetakse salvestatud põhikeele (et) tõlkega, mida admin tõlgib. Välja "Ametinimetus" sildi kõrval on "?" ikoon tooltip'iga: "Ametinimetus kuvatakse koolitaja kaardil nime all. Kirjuta lühidalt, millega koolitaja tegeleb, nt „Tarkvaraarendaja ja Java koolitaja“ või „UX-disainer“. Iga keele jaoks eraldi tõlge." Lühikirjelduse all vihje "Kuvatakse koolitaja kaardil koolituse lehel ja toimumiskorra juures".
"Tee AI tõlge" tõlgib salvestatud põhikeele tõlke (kõik kolm välja) ja täidab ainult vormi; salvestamata muudatuste korral küsitakse enne kinnitust.
"Lisa tõlge" → POST /api/lecturer/{lecturerId}/lecturer-translation. Vastuse järgi router.replace → state "update". Kiirnupp "Koolitajad" → /admin-lecturers.
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
Olemasolev teenus. LecturerFormView kasutab seda põhikeele (isMainLanguage = true) ja tõlkelippude (requiresTranslation = true) leidmiseks ning languageId ↔ languageCode teisendamiseks. Uue koolitaja tõlge on alati põhikeeles.

Veateated: —
```

## API märkmed — GET /api/lecturer/{lecturerId}

```text
API: GET /api/lecturer/{lecturerId}

Response (200):
LecturerDetailDto.java
{
  "lecturerId": 1,
  "fullName": "Mari Tamm",
  "photo": "iVBORw0KGgoAAAANSUhEUgAAACAAAAAg...",
  "photoContentType": "image/png"
}

API teenuse lisainfo:
Koolitaja nimi ja pilt (tabelist lecturer_photo, Base64). photo ja photoContentType on null, kui pilti pole. Frontend kuvab eelvaate kujul data:{photoContentType};base64,{photo} ja saadab sama väärtuse PUT päringuga tagasi, kui pilti ei muudetud. Kustutatud koolitaja (status "D") on nagu olematu: 404 PRIMARY_KEY_NOT_FOUND.

Veateated:
HTTP: 404
errorCode: PRIMARY_KEY_NOT_FOUND
message: "Ei leidnud primary keyd 'lecturerId' väärtusega: 123"
```

## API märkmed — GET /api/lecturer/{lecturerId}/lecturer-translations

```text
API: GET /api/lecturer/{lecturerId}/lecturer-translations

Response (200):
LecturerTranslationItemDto.java
[
  {
    "lecturerTranslationId": 1,
    "languageId": 1,
    "languageCode": "et",
    "isMainLanguage": true
  },
  ...
]

API teenuse lisainfo:
Koolitaja olemasolevad tõlked (sama muster nagu GET /api/training/{trainingId}/training-translations). Frontend võrdleb languageCode väärtusi GET /api/languages tõlkekeeltega: tõlge olemas → värviline lipp, puudub → hall lipp. isMainLanguage = true tõlkega eeltäidetakse uue tõlke vorm. Kustutatud koolitaja (status "D") on nagu olematu: 404 PRIMARY_KEY_NOT_FOUND.

Veateated:
HTTP: 404
errorCode: PRIMARY_KEY_NOT_FOUND
message: "Ei leidnud primary keyd 'lecturerId' väärtusega: 123"
```

## API märkmed — GET /api/lecturer-translation/{lecturerTranslationId}

```text
API: GET /api/lecturer-translation/{lecturerTranslationId}

Response (200):
LecturerTranslationDto.java
{
  "lecturerTranslationId": 1,
  "lecturerId": 1,
  "languageId": 1,
  "languageCode": "et",
  "title": "Tarkvaraarendaja ja Java koolitaja",
  "shortDescription": "Üle 10 aasta kogemust tarkvaraarenduse koolitajana.",
  "description": "<p>Mari on töötanud tarkvaraarendajana panganduses ja telekommunikatsioonis ning koolitab Java ja Spring Booti teemadel alates 2015. aastast.</p>"
}

API teenuse lisainfo:
Üks koolitaja tõlge (sama muster nagu GET /api/training-translation/{trainingTranslationId}). state "update" laadib avatud tõlke; state "new-translation" laadib põhikeele tõlke, millega eeltäidetakse tõlke väljad. title = ametinimetus. Kui tõlke koolitaja on kustutatud, on tõlge nagu olematu.

Veateated:
HTTP: 404
errorCode: PRIMARY_KEY_NOT_FOUND
message: "Ei leidnud primary keyd 'lecturerTranslationId' väärtusega: 123"
```

## API märkmed — GET /api/lecturer/{lecturerId}/ai-translation

```text
API: GET /api/lecturer/{lecturerId}/ai-translation

Query parameetrid:
languageId: Integer — sihtkeel, kuhu tõlgitakse

Response (200):
LecturerAiTranslationDto.java
{
  "title": "UX designer",
  "shortDescription": "Teaches user-centred design and Figma.",
  "description": "<p>Kadri has designed web and mobile apps for start-ups and coaches design teams in running user research.</p>"
}

API teenuse lisainfo:
Tõlgib alati andmebaasi salvestatud põhikeele tõlke (kõik kolm välja) AI abil sihtkeelde — vormi sisu ei kasutata. Andmebaasi midagi ei salvestata; tulemus täidab ainult vormi. description HTML-märgendid säilitatakse. Vastusel päis Cache-Control: no-store. Sama loogika nagu GET /api/training/{trainingId}/ai-translation; frontend võib alustada mock-vastusega. Kustutatud koolitaja (status "D") on nagu olematu: 404 PRIMARY_KEY_NOT_FOUND.

Veateated:
HTTP: 404
errorCode: PRIMARY_KEY_NOT_FOUND
message: "Ei leidnud primary keyd 'lecturerId' väärtusega: 123"

HTTP: 404
errorCode: PRIMARY_KEY_NOT_FOUND
message: "Ei leidnud primary keyd 'languageId' väärtusega: 123"

HTTP: 403
errorCode: MAIN_LANGUAGE_NOT_TRANSLATABLE
message: "Põhikeelde ei saa AI tõlget teha"

HTTP: 503
errorCode: AI_SERVICE_UNAVAILABLE
message: "AI tõlketeenus ei ole hetkel kättesaadav"
```

## API märkmed — POST /api/lecturer/{lecturerId}/lecturer-translation

```text
API: POST /api/lecturer/{lecturerId}/lecturer-translation

Request body:
LecturerTranslationCreateRequestDto.java
{
  "languageId": 2,
  "title": "UX designer",
  "shortDescription": "Teaches user-centred design and Figma.",
  "description": "<p>Kadri has designed web and mobile apps for start-ups and coaches design teams in running user research.</p>"
}

Response (200):
LecturerTranslationCreateResponseDto.java
{
  "lecturerTranslationId": 8
}

API teenuse lisainfo:
Lisab koolitajale uue keele tõlke. Ühes keeles saab koolitajal olla ainult üks tõlge (lecturer_translation_uq). Kõik kolm välja on kohustuslikud (title ja shortDescription kuni 255). Vastuse järgi teeb frontend router.replace → state "update". Kustutatud koolitaja (status "D") on nagu olematu: 404 PRIMARY_KEY_NOT_FOUND.

Veateated:
HTTP: 404
errorCode: PRIMARY_KEY_NOT_FOUND
message: "Ei leidnud primary keyd 'lecturerId' väärtusega: 123"

HTTP: 404
errorCode: PRIMARY_KEY_NOT_FOUND
message: "Ei leidnud primary keyd 'languageId' väärtusega: 123"

HTTP: 403
errorCode: TRANSLATION_EXISTS
message: "Selles keeles tõlge on juba olemas"
```
