# LecturerFormView.vue (state: "update") — märkmed

Üks kolmest LecturerFormView oleku märkmete failist (vt ka teisi `lecturer-form-view-state-*-markmed.md` faile). Olekud ja tõlkeloogika on samad mis TrainingFormView-l. Otsused, andmebaasi ettepanek ja skeemid: `docs/mock-wireframe/loo-mock-vaade/admin-lecturers-view/admin-lecturers-view-skeemid.md`. Uute teenuste DTO-d on ettepanek.

## Vaate märkmed

```text
Roll: Admin
Failinimi: LecturerFormView.vue
Frontend rada: /lecturer-form?lecturerId={id}&lecturerTranslationId={id}

Vaatega seotud lisainfo:
state: "update" — URL-is lecturerId ja lecturerTranslationId. Pealkiri "Muuda koolitajat". Kaardid "Koolitaja andmed" (nimi, pilt) ja "Tõlge ({keel})" (ametinimetus, lühikirjeldus, kirjeldus) on täidetud. Välja "Ametinimetus" sildi kõrval on "?" ikoon tooltip'iga: "Ametinimetus kuvatakse koolitaja kaardil nime all. Kirjuta lühidalt, millega koolitaja tegeleb, nt „Lektor/konsultant“ või „Projektijuht/lektor“. Iga keele jaoks eraldi tõlge." Lühikirjelduse all vihje "Kuvatakse koolitaja kaardil koolituse lehel ja toimumiskorra juures". Iga router.replace järel laaditakse andmed uuesti.
Lipukesed: tõlge olemas → värviline lipp (klikk avab selle tõlke), puudub → hall lipp (klikk → state "new-translation").
"Salvesta" → PUT /api/lecturer/{lecturerId} (nimi, pilt ja avatud tõlge ühes transaktsioonis; photo = uus pilt või null = ei muudeta, isPhotoRemoved = eemalda), eduteade "Salvestatud". "Tee AI tõlge" on ainult mitte-põhikeele tõlkel: täidab vaid vormi, salvestamata muudatuste korral küsib enne kinnitust. Kiirnupp "Koolitajad" → /admin-lecturers. Kustutatud koolitaja → üldine veavaade.
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
  "fullName": "Rain Tüür",
  "photoVersion": 1784095200
}

API teenuse lisainfo:
Koolitaja nimi ja pildi versioon. Pilti ei tagastata — photoVersion = lecturer_photo.updated_at epoch-sekundites või null, kui pilti pole; eelvaade tuleb pilditeenusest /api/lecturer/{lecturerId}/photo?v={photoVersion}. Kustutatud koolitaja (status "D") on nagu olematu: 404 PRIMARY_KEY_NOT_FOUND.

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
  "title": "Lektor/konsultant",
  "shortDescription": "Tarkvaraarendus, Java, HTML, CSS, JavaScript, SQL, Git, Spring Boot, REST API, PostgreSQL, JPA (Hibernate), MapStruct, JUnit, Swagger, Gradle, Confluence, Jira. Vali Tarkvaraarendus! programm.",
  "description": "<p>Tarkvaraarendus, Java, HTML, CSS, JavaScript, SQL, Git, Spring Boot, REST API, PostgreSQL, JPA (Hibernate), MapStruct, JUnit, Swagger, Gradle, Confluence, Jira. Vali Tarkvaraarendus! programm.</p>"
}

API teenuse lisainfo:
Üks koolitaja tõlge (sama muster nagu GET /api/training-translation/{trainingTranslationId}). state "update" laadib avatud tõlke; state "new-translation" laadib põhikeele tõlke, millega eeltäidetakse tõlke väljad. title = ametinimetus. Kui tõlke koolitaja on kustutatud, on tõlge nagu olematu.

Veateated:
HTTP: 404
errorCode: PRIMARY_KEY_NOT_FOUND
message: "Ei leidnud primary keyd 'lecturerTranslationId' väärtusega: 123"
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

## API märkmed — PUT /api/lecturer/{lecturerId}

```text
API: PUT /api/lecturer/{lecturerId}

Request body:
LecturerUpdateRequestDto.java
{
  "fullName": "Rain Tüür",
  "photo": null,
  "photoContentType": null,
  "isPhotoRemoved": true,
  "lecturerTranslation": {
    "lecturerTranslationId": 1,
    "title": "Lektor/konsultant",
    "shortDescription": "Tarkvaraarendus, Java, Spring Boot, REST API, PostgreSQL, Git. Vali Tarkvaraarendus! programm.",
    "description": "<p>Tarkvaraarendus, Java, HTML, CSS, JavaScript, SQL, Git, Spring Boot, REST API, PostgreSQL, JPA (Hibernate), MapStruct, JUnit, Swagger, Gradle, Confluence, Jira. Vali Tarkvaraarendus! programm. Juhendab Java algkursust ja Spring Booti veebiarenduse koolitust.</p>"
  }
}

Response (200): NONE

API teenuse lisainfo:
Muudab ühes transaktsioonis koolitaja nime, pilti ja avatud tõlget (sama muster nagu PUT /api/training/{trainingId}). Pilt: photo ≠ null → uus pilt, normaliseeritakse ja lecturer_photo lisatakse või asendatakse; photo = null → pilti ei muudeta; isPhotoRemoved = true → lecturer_photo rida kustutatakse (photo peab siis olema null). Salvestatud pilti tagasi ei saadeta, seega seda ei kodeerita uuesti. Valideerimine sama mis POST puhul. lecturerTranslation peab kuuluma sellele koolitajale. Uuendab updated_at. Kustutatud koolitaja (status "D") on nagu olematu: 404 PRIMARY_KEY_NOT_FOUND.

Veateated:
HTTP: 404
errorCode: PRIMARY_KEY_NOT_FOUND
message: "Ei leidnud primary keyd 'lecturerId' väärtusega: 123"

HTTP: 404
errorCode: PRIMARY_KEY_NOT_FOUND
message: "Ei leidnud primary keyd 'lecturerTranslationId' väärtusega: 123"

HTTP: 403
errorCode: PHOTO_TYPE_NOT_ALLOWED
message: "Lubatud on ainult PNG, JPEG või WebP pilt"

HTTP: 403
errorCode: PHOTO_TOO_LARGE
message: "Pilt on liiga suur, lubatud kuni 2 MB"
```

## API märkmed — GET /api/lecturer/{lecturerId}/ai-translation

```text
API: GET /api/lecturer/{lecturerId}/ai-translation

Query parameetrid:
languageId: Integer — sihtkeel, kuhu tõlgitakse

Response (200):
LecturerAiTranslationDto.java
{
  "title": "Lecturer/consultant",
  "shortDescription": "Adobe Photoshop, Illustrator, InDesign, Acrobat, Canva, Figma, digital marketing, e-learning design, Office applications.",
  "description": "<p>Adobe Photoshop, Illustrator, InDesign, Acrobat, Canva, Figma, digital marketing, e-learning design, Office applications.</p>"
}

API teenuse lisainfo:
Tõlgib alati andmebaasi salvestatud põhikeele tõlke (kõik kolm välja) AI abil sihtkeelde — vormi sisu ei kasutata. Andmebaasi midagi ei salvestata; tulemus täidab ainult vormi. description HTML-märgendid säilitatakse. Vastusel päis Cache-Control: no-store. Sama loogika nagu POST /api/ai-training/translation/{trainingId}; frontend võib alustada mock-vastusega. Kustutatud koolitaja (status "D") on nagu olematu: 404 PRIMARY_KEY_NOT_FOUND.

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


## Tagasitee — täiendatud navigatsioon

Vaade võtab vastu valikulise `returnTo` query parameetri ja kuvab lingi „← Tagasi“ (`BackLink.vue`). Avamislingid annavad kaasa lähtevaate täieliku URL-i. Tagasilingi puuduv, väline, tundmatu või iseendale osutav siht asendatakse vaate varusihtkohaga. Oleku- ja tõlkevahetus ei kaota tagasiteed. Eraldi nimega nimekirja-/kalendrinupud säilitavad oma sihtkoha. Täpne [kaardistus ja varusihtkohad](../../tasks/frontend/return-to-navigation.md) ning [skeemid](../loo-mock-vaade/return-to-navigation-skeemid.md).
