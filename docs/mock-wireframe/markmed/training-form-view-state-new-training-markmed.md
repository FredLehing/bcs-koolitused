# TrainingFormView.vue (state: "new-training") — märkmed

Üks kolmest TrainingFormView oleku märkmete failist (vt ka teisi `training-form-view-state-*-markmed.md` faile). Plaan ja skeemid: `docs/mock-wireframe/loo-mock-vaade/`.

## Vaate märkmed

```text
Roll: Admin
Failinimi: TrainingFormView.vue
Frontend rada: /training-form

Vaatega seotud lisainfo:
state: "new-training" — URL-is query parameetreid pole. Pealkiri "Lisa uus koolitus". Vorm on tühi; tõlke väljad (pealkiri, lühikirjeldus, kirjeldus) täidetakse põhikeeles (et) ja rippmenüüd laaditakse põhikeeles. Lipukesi ja staatuse nuppe pole.
Vaikimisi lektor valitakse "Vali lektor" modalist otsinguga (võib jääda tühjaks). Rahastustüübid on checkboxid, "Tellitav" ja "Esile tõstetud" switchid.
Nupp "Lisa" → POST /api/training (userId localStorage'ist). Backend loob koolituse staatusega "U" koos põhikeele tõlkega. Vastuse trainingId ja trainingTranslationId järgi tehakse router.replace → state "update".
```

## API märkmed — GET /api/languages

```text
API: GET /api/languages

Response (200):
TrainingLanguageDto.java
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
Tagastab kõik kategooriad contentLang keeles (category_translation kaudu). TrainingFormView kutsub avatud tõlke keelega.

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

## API märkmed — POST /api/training

```text
API: POST /api/training

Request body:
TrainingCreateRequestDto.java
{
  "userId": 1,
  "categoryId": 1,
  "trainingLanguageId": 1,
  "locationId": 2,
  "defaultLecturerId": 1,
  "isOrderable": true,
  "isPromoted": false,
  "fundingTypeIds": [
    1,
    ...
  ],
  "title": "Power BI edasijõudnutele",
  "shortDescription": "Andmemudelid, DAX ja interaktiivsed aruanded.",
  "description": "Kursusel ehitatakse Power BI-s andmemudel, kirjutatakse DAX-valemeid ja luuakse interaktiivseid aruandeid."
}

Response (200):
TrainingCreateResponseDto.java
{
  "trainingId": 3,
  "trainingTranslationId": 5
}

API teenuse lisainfo:
Loob training rea (status = "U", määrab backend), training_funding_type read ja et tõlke (language_id = 1) ühes transaktsioonis. defaultLecturerId võib olla null. Sama PRIMARY_KEY_NOT_FOUND muster kehtib ka trainingLanguageId, defaultLecturerId ja fundingTypeIds väljadele.

Veateated:
HTTP: 404
errorCode: PRIMARY_KEY_NOT_FOUND
message: "Ei leidnud primary keyd 'categoryId' väärtusega: 123"

HTTP: 404
errorCode: PRIMARY_KEY_NOT_FOUND
message: "Ei leidnud primary keyd 'locationId' väärtusega: 123"
```
