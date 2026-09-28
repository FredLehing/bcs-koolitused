# TrainingFormView.vue (state: "update") — märkmed

Üks kolmest TrainingFormView oleku märkmete failist (vt ka teisi `training-form-view-state-*-markmed.md` faile). Plaan ja skeemid: `docs/mock-wireframe/loo-mock-vaade/`.

## Vaate märkmed

```text
Roll: Admin
Failinimi: TrainingFormView.vue
Frontend rada: /training-form?trainingId={id}&trainingTranslationId={id}

Vaatega seotud lisainfo:
state: "update" — URL-is trainingId ja trainingTranslationId. Pealkiri "Muuda koolitust" koos staatuse märgisega (Mustand / Publitseeritud). Vorm on eeltäidetud; rippmenüüd (kategooriad, rahastustüübid) laaditakse kasutajaliidese keeles (store'i contentLang) ja laaditakse keele vahetamisel navbaris uuesti — vormi sisu jääb alles. Iga router.replace järel laaditakse andmed uuesti.
Lipukesed: tõlge olemas → värviline lipp (klikk avab selle tõlke), tõlge puudub → hall lipp (klikk → state "new-translation").
"Salvesta" → PUT /api/training/{trainingId} (koolituse väljad + avatud tõlge ühes transaktsioonis). "Tee AI tõlge" on ainult mitte-põhikeele tõlkel: täidab vaid vormi, ei salvesta.
status "U" → nupp "Publitseeri", status "P" → nupp "Liiguta mustandisse"; mõlemal kinnituse modal (PUT /api/training/{trainingId}/publish või /unpublish).
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
    "languageName": "Eesti"
  },
  ...
]

API teenuse lisainfo:
Tagastab kõik süsteemi keeled (language tabel). TrainingFormView kasutab neid "Koolituse keel" rippmenüüs ja languageId ↔ languageCode teisendamiseks. languageName ei ole tõlgitud.

Veateated: —
```

## API märkmed — GET /api/locations

```text
API: GET /api/locations

Response (200):
LocationDto.java
[
  {
    "locationId": 1,
    "locationName": "BCS Koolitus"
  },
  ...
]

API teenuse lisainfo:
Tagastab kõik toimumiskohad "Toimumiskoht" rippmenüü jaoks. location.name ei ole tõlgitud, seega contentLang parameetrit pole.

Veateated: —
```

## API märkmed — GET /api/training/{trainingId}

```text
API: GET /api/training/{trainingId}

Response (200):
TrainingDto.java
{
  "trainingId": 1,
  "categoryId": 1,
  "trainingLanguageId": 1,
  "locationId": 1,
  "defaultLecturerId": 1,
  "defaultLecturerName": "Mari Tamm",
  "isOrderable": true,
  "isPromoted": true,
  "status": "P",
  "fundingTypeIds": [
    1,
    ...
  ]
}

API teenuse lisainfo:
Koolituse väljad ilma tõlketa. defaultLecturerId ja defaultLecturerName võivad olla null. status: "U" = mustand (unpublished), "P" = publitseeritud.

Veateated:
HTTP: 404
errorCode: PRIMARY_KEY_NOT_FOUND
message: "Ei leidnud primary keyd 'trainingId' väärtusega: 123"
```

## API märkmed — GET /api/training-translation/{trainingTranslationId}

```text
API: GET /api/training-translation/{trainingTranslationId}

Response (200):
TrainingTranslationDto.java
{
  "trainingTranslationId": 1,
  "trainingId": 1,
  "languageId": 1,
  "languageCode": "et",
  "title": "Java algkursus",
  "shortDescription": "Java programmeerimise alused algajatele.",
  "description": "Kursusel õpitakse Java süntaksit, objektorienteeritud programmeerimist ja põhilisi andmestruktuure."
}

API teenuse lisainfo:
Laadib avatud tõlke (URL-i trainingTranslationId). languageCode näitab, mis keeles avatud tõlge on (tõlke vormi pealkiri, AI nupu nähtavus).

Veateated:
HTTP: 404
errorCode: PRIMARY_KEY_NOT_FOUND
message: "Ei leidnud primary keyd 'trainingTranslationId' väärtusega: 123"
```

## API märkmed — GET /api/training/{trainingId}/training-translations

```text
API: GET /api/training/{trainingId}/training-translations

Response (200):
TrainingTranslationItemDto.java
[
  {
    "trainingTranslationId": 1,
    "languageId": 1,
    "languageCode": "et",
    "isMainLanguage": true
  },
  ...
]

API teenuse lisainfo:
Koolituse olemasolevad tõlked. Frontend võrdleb languageCode väärtusi store'i contentLanguages massiiviga: tõlge olemas → värviline lipp, puudub → hall lipp. isMainLanguage tuleb language.is_main_language veerust; põhikeele tõlkega eeltäidetakse uue tõlke vorm.

Veateated:
HTTP: 404
errorCode: PRIMARY_KEY_NOT_FOUND
message: "Ei leidnud primary keyd 'trainingId' väärtusega: 123"
```

## API märkmed — GET /api/categories

```text
API: GET /api/categories

Query parameetrid:
contentLang: String — categoryName keel ("et"/"en"/"ru")

Response (200):
CategoryDto.java
[
  {
    "categoryId": 1,
    "categoryName": "Programmeerimine"
  },
  ...
]

API teenuse lisainfo:
Tagastab kõik kategooriad contentLang keeles (category_translation kaudu). TrainingFormView kutsub kasutajaliidese keelega (store'i contentLang) ja kutsub uuesti, kui keel navbaris vahetub.

Veateated: —
```

## API märkmed — GET /api/funding-types

```text
API: GET /api/funding-types

Query parameetrid:
contentLang: String — fundingTypeName keel ("et"/"en"/"ru")

Response (200):
FundingTypeDto.java
[
  {
    "fundingTypeId": 1,
    "fundingTypeName": "Töötukassa"
  },
  ...
]

API teenuse lisainfo:
Tagastab kõik rahastustüübid contentLang keeles (funding_type_translation kaudu). TrainingFormView kuvab need checkboxidena.

Veateated: —
```

## API märkmed — GET /api/lecturers

```text
API: GET /api/lecturers

Query parameetrid:
search: String — otsingusõna lektori nimest, tühi = kõik

Response (200):
LecturerDto.java
[
  {
    "lecturerId": 1,
    "lecturerName": "Mari Tamm",
    "lecturerPhoto": "BASE64-image-data"
  },
  ...
]

API teenuse lisainfo:
Tagastab lektorid, kelle nimi (lecturer.full_name) sisaldab otsingusõna, tõstutundetult. Kasutatakse "Vali lektor" modalis.

Veateated: —
```

## API märkmed — PUT /api/training/{trainingId}

```text
API: PUT /api/training/{trainingId}

Request body:
TrainingUpdateRequestDto.java
{
  "categoryId": 1,
  "trainingLanguageId": 1,
  "locationId": 1,
  "defaultLecturerId": 1,
  "isOrderable": true,
  "isPromoted": true,
  "fundingTypeIds": [
    1,
    ...
  ],
  "trainingTranslationId": 1,
  "title": "Java algkursus",
  "shortDescription": "Java programmeerimise alused algajatele.",
  "description": "Kursusel õpitakse Java süntaksit, objektorienteeritud programmeerimist ja põhilisi andmestruktuure."
}

Response (200): NONE

API teenuse lisainfo:
Salvestab koolituse väljad ja trainingTranslationId tõlke ühes transaktsioonis. training_funding_type read kirjutatakse fundingTypeIds järgi üle.

Veateated:
HTTP: 404
errorCode: PRIMARY_KEY_NOT_FOUND
message: "Ei leidnud primary keyd 'trainingId' väärtusega: 123"

HTTP: 404
errorCode: PRIMARY_KEY_NOT_FOUND
message: "Ei leidnud primary keyd 'trainingTranslationId' väärtusega: 123"
```

## API märkmed — GET /api/training/{trainingId}/ai-translation

```text
API: GET /api/training/{trainingId}/ai-translation

Query parameetrid:
languageId: Integer — sihtkeel, kuhu tõlgitakse

Response (200):
AiTranslationDto.java
{
  "title": "Power BI for Advanced Users",
  "shortDescription": "Data models, DAX and interactive reports.",
  "description": "The course builds a data model in Power BI, writes DAX formulas and creates interactive reports."
}

API teenuse lisainfo:
Tõlgib alati andmebaasi salvestatud põhikeele (language.is_main_language = true) tõlke AI abil sihtkeelde — vormi sisu ei kasutata. Andmebaasi midagi ei salvestata. description HTML-märgendid säilitatakse. Vastusel päis Cache-Control: no-store.

Veateated:
HTTP: 404
errorCode: PRIMARY_KEY_NOT_FOUND
message: "Ei leidnud primary keyd 'trainingId' väärtusega: 123"

HTTP: 404
errorCode: PRIMARY_KEY_NOT_FOUND
message: "Ei leidnud primary keyd 'languageId' väärtusega: 123"

HTTP: 403
errorCode: MAIN_LANGUAGE_NOT_TRANSLATABLE
message: "Põhikeelde ei saa AI tõlget teha"

HTTP: 404
errorCode: MAIN_TRANSLATION_NOT_FOUND
message: "Koolitusel puudub põhikeele tõlge"

HTTP: 503
errorCode: AI_SERVICE_UNAVAILABLE
message: "AI tõlketeenus ei ole hetkel kättesaadav"
```

## API märkmed — PUT /api/training/{trainingId}/publish

```text
API: PUT /api/training/{trainingId}/publish

Response (200): NONE

API teenuse lisainfo:
Tegevusteenus (erand URL-ide kokkuleppest): määrab training.status = "P" (publitseeritud). Request body puudub. Juba publitseeritud koolituse korral midagi ei muutu. Frontend kutsub pärast kinnituse modalit.

Veateated:
HTTP: 404
errorCode: PRIMARY_KEY_NOT_FOUND
message: "Ei leidnud primary keyd 'trainingId' väärtusega: 123"
```

## API märkmed — PUT /api/training/{trainingId}/unpublish

```text
API: PUT /api/training/{trainingId}/unpublish

Response (200): NONE

API teenuse lisainfo:
Tegevusteenus (erand URL-ide kokkuleppest): määrab training.status = "U" (mustand). Request body puudub. Juba mustandis koolituse korral midagi ei muutu. Frontend kutsub pärast kinnituse modalit.

Veateated:
HTTP: 404
errorCode: PRIMARY_KEY_NOT_FOUND
message: "Ei leidnud primary keyd 'trainingId' väärtusega: 123"
```
