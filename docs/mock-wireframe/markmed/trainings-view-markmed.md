# TrainingsView.vue — märkmed

## Vaate märkmed

```text
Roll: Kõik rollid (sh külastajad, sisselogimist ei nõuta)
Failinimi: TrainingsView.vue
Frontend rada: /trainings

Vaatega seotud lisainfo:
Vaate avanemisel tehakse päringud GET /api/trainings, GET /api/categories, GET /api/funding-types ja GET /api/languages (viimased kolm filtrite valikute jaoks); contentLang väärtus võetakse localStorage'ist (vaikimisi "et"), koolituse keele filter on vaikimisi valimata (trainingLanguageId=0). Koolituse keele, kategooria või rahastuse filtri muutmisel tehakse uus päring vastavate query parameetritega ja page lähtestatakse 0-ks. Eelmine/Järgmine ja leheküljenumbrid muudavad page väärtust.
Koolituse kaardil (TrainingCard.vue) kuvatakse "Tellitav" märgis, kui isOrderable = true, ja täht-ikoon, kui isPromoted = true.
Nupule "Vaata lähemalt" vajutades suunatakse kasutaja TrainingView vaatele (/training?trainingId={id}).
```

## API märkmed — GET /api/trainings

```text
API: GET /api/trainings

Query parameetrid:
categoryId: Integer — kategooria filter, 0 = kõik
fundingTypeId: Integer — rahastustüübi filter, 0 = kõik
limit: Integer — koolituste arv lehel
page: Integer — lehekülje number, algab 0-st
trainingLanguageId: Integer — õppekeele filter, 0 = kõik
contentLang: String — tõlgitud väljade keel ("et"/"en")

Response (200):
TrainingSummaryDto.java
{
  "totalPages": 1,
  "totalElements": 2,
  "trainingSummaries": [
    {
      "trainingId": 1,
      "title": "Java algkursus",
      "shortDescription": "Java programmeerimise alused algajatele.",
      "categoryId": 1,
      "categoryName": "Programmeerimine",
      "isOrderable": true,
      "isPromoted": true,
      "fundingTypes": [
        {
          "fundingTypeId": 1,
          "fundingTypeName": "Töötukassa"
        },
        ...
      ]
    },
    ...
  ]
}

API teenuse lisainfo:
Tulemus on sorteeritud: esile tõstetud koolitused (isPromoted = true) eespool, seejärel title järgi tähestikuliselt.
totalPages ja totalElements kirjeldavad kogu filtreeritud tulemushulka. fundingTypes võib olla tühi list (training_funding_type kaudu).

Veateated: —
```

## API märkmed — GET /api/categories

```text
API: GET /api/categories

Query parameetrid:
contentLang: String — categoryName keel ("et"/"en")

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
Tagastab kõik süsteemis olevad kategooriad contentLang keeles (category_translation kaudu). TrainingsView kasutab neid "Koolituse kategooria" filtri valikutena.

Veateated: —
```

## API märkmed — GET /api/funding-types

```text
API: GET /api/funding-types

Query parameetrid:
contentLang: String — fundingTypeName keel ("et"/"en")

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
Tagastab kõik süsteemis olevad rahastustüübid contentLang keeles (funding_type_translation kaudu). TrainingsView kasutab neid "Rahastus" filtri valikutena. FundingTypeDto on sama, mida kasutab GET /api/trainings vastuse fundingTypes list.

Veateated: —
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
Tagastab kõik süsteemis olevad keeled (language tabel). TrainingsView kasutab neid "Koolituse keel" filtri valikutena (languageId → trainingLanguageId). languageName ei ole tõlgitud, seega contentLang parameetrit pole.

Veateated: —
```
