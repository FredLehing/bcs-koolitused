# TrainingFormView.vue (state: "new-translation") — märkmed

Üks kolmest TrainingFormView oleku märkmete failist (vt ka teisi `training-form-view-state-*-markmed.md` faile). Plaan ja skeemid: `docs/mock-wireframe/loo-mock-vaade/`.

## Vaate märkmed

```text
Roll: Admin
Failinimi: TrainingFormView.vue
Frontend rada: /training-form?trainingId={id}&languageId={id}

Vaatega seotud lisainfo:
state: "new-translation" — URL-is trainingId ja languageId (keel, mille tõlge puudub). Pealkiri "Lisa koolituse tõlge". Koolituse väljad on kirjutuskaitstud; rippmenüüd (kategooriad, rahastustüübid) laaditakse kasutajaliidese keeles (store'i contentLang) ja laaditakse keele vahetamisel navbaris uuesti — vormi sisu jääb alles.
Tõlke väljad eeltäidetakse salvestatud põhikeele (et) tõlkega, mida admin tõlgib. "Tee AI tõlge" tõlgib salvestatud põhikeele teksti ja täidab ainult vormi; salvestamata muudatuste korral küsitakse enne kinnitust.
"Lisa tõlge" → POST /api/training/{trainingId}/training-translation. Vastuse trainingTranslationId järgi tehakse router.replace → state "update".
Staatuse nupp ("Publitseeri" / "Liiguta mustandisse") nagu state "update" puhul.
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
Tagastab kõik süsteemi keeled (language tabel). TrainingFormView kasutab neid "Koolituse keel" rippmenüüs ja languageId ↔ languageCode teisendamiseks. languageName ei ole tõlgitud. requiresTranslation = false tähendab õppekeelt, millesse koolituse sisu ei tõlgita (nt ru) — see keel on "Koolituse keel" valikutes, aga mitte tõlkelippudes. flagIconCode on flag-icons CSS klass (nt "fi-ee"). Tõlkelipud = keeled, mille requiresTranslation = true; põhikeel (mainLanguageCode) = keel, mille isMainLanguage = true — mõlemad arvutatakse selle vastuse põhjal, frontendis kõvasti kirjas ei ole.

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
  "lecturers": [
    {
      "lecturerId": 1,
      "lecturerName": "Rain Tüür"
    },
    ...
  ],
  "isOrderable": true,
  "isPromoted": true,
  "status": "P",
  "fundingTypeIds": [
    1,
    ...
  ]
}

API teenuse lisainfo:
Koolituse väljad ilma tõlketa. lecturers = koolituse koolitajad (tabel training_lecturer) sort_order järjekorras; tühi list, kui koolitajaid pole. Kustutatud koolitaja jääb seotuks ja on nimekirjas edasi. status: "U" = mustand (unpublished), "P" = publitseeritud. Kustutatud koolitus (status "D") on nagu olematu: 404 PRIMARY_KEY_NOT_FOUND.

Veateated:
HTTP: 404
errorCode: PRIMARY_KEY_NOT_FOUND
message: "Ei leidnud primary keyd 'trainingId' väärtusega: 123"
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
Koolituse olemasolevad tõlked. Frontend võrdleb languageCode väärtusi GET /api/languages tõlkekeeltega (requiresTranslation = true): tõlge olemas → värviline lipp, puudub → hall lipp. isMainLanguage tuleb language.is_main_language veerust; põhikeele tõlkega eeltäidetakse uue tõlke vorm. Kustutatud koolitus (status "D") on nagu olematu: 404 PRIMARY_KEY_NOT_FOUND.

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
Laadib koolituse salvestatud põhikeele tõlke (training-translations vastusest isMainLanguage = true), millega eeltäidetakse uue tõlke väljad. Vastuse languageCode on põhikeel (et). Kui tõlke koolitus on kustutatud (status "D"), on tõlge nagu olematu: 404 PRIMARY_KEY_NOT_FOUND.

Veateated:
HTTP: 404
errorCode: PRIMARY_KEY_NOT_FOUND
message: "Ei leidnud primary keyd 'trainingTranslationId' väärtusega: 123"
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
Tõlgib alati andmebaasi salvestatud põhikeele (language.is_main_language = true) tõlke AI abil sihtkeelde — vormi sisu ei kasutata. Andmebaasi midagi ei salvestata. description HTML-märgendid säilitatakse. Vastusel päis Cache-Control: no-store. Kustutatud koolitus (status "D") on nagu olematu: 404 PRIMARY_KEY_NOT_FOUND.

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

## API märkmed — POST /api/training/{trainingId}/training-translation

```text
API: POST /api/training/{trainingId}/training-translation

Request body:
TrainingTranslationCreateRequestDto.java
{
  "languageId": 2,
  "title": "Power BI for Advanced Users",
  "shortDescription": "Data models, DAX and interactive reports.",
  "description": "The course builds a data model in Power BI, writes DAX formulas and creates interactive reports."
}

Response (200):
TrainingTranslationCreateResponseDto.java
{
  "trainingTranslationId": 6
}

API teenuse lisainfo:
Lisab koolitusele uue keele tõlke. Ühes keeles saab koolitusel olla ainult üks tõlge (training_translation_uq). Kustutatud koolitus (status "D") on nagu olematu: 404 PRIMARY_KEY_NOT_FOUND.

Veateated:
HTTP: 404
errorCode: PRIMARY_KEY_NOT_FOUND
message: "Ei leidnud primary keyd 'trainingId' väärtusega: 123"

HTTP: 404
errorCode: PRIMARY_KEY_NOT_FOUND
message: "Ei leidnud primary keyd 'languageId' väärtusega: 123"

HTTP: 403
errorCode: TRANSLATION_EXISTS
message: "Selles keeles tõlge on juba olemas"
```

## API märkmed — PUT /api/training/{trainingId}/publish

```text
API: PUT /api/training/{trainingId}/publish

Response (200): NONE

API teenuse lisainfo:
Tegevusteenus (erand URL-ide kokkuleppest): määrab training.status = "P" (publitseeritud). Request body puudub. Juba publitseeritud koolituse korral midagi ei muutu. Frontend kutsub pärast kinnituse modalit. Kustutatud koolituse (status "D") korral backend keeldub.

Veateated:
HTTP: 404
errorCode: PRIMARY_KEY_NOT_FOUND
message: "Ei leidnud primary keyd 'trainingId' väärtusega: 123"

HTTP: 403
errorCode: TRAINING_DELETED
message: "Kustutatud koolituse staatust ei saa muuta, taasta see enne"
```

## API märkmed — PUT /api/training/{trainingId}/unpublish

```text
API: PUT /api/training/{trainingId}/unpublish

Response (200): NONE

API teenuse lisainfo:
Tegevusteenus (erand URL-ide kokkuleppest): määrab training.status = "U" (mustand). Request body puudub. Juba mustandis koolituse korral midagi ei muutu. Frontend kutsub pärast kinnituse modalit. Kustutatud koolituse (status "D") korral backend keeldub.

Veateated:
HTTP: 404
errorCode: PRIMARY_KEY_NOT_FOUND
message: "Ei leidnud primary keyd 'trainingId' väärtusega: 123"

HTTP: 403
errorCode: TRAINING_DELETED
message: "Kustutatud koolituse staatust ei saa muuta, taasta see enne"
```
