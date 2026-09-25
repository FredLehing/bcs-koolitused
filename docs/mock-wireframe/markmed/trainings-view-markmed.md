# TrainingsView.vue — märkmed

## Vaate märkmed

```text
Roll: Kõik rollid (külastajale kuvatakse "Logi sisse" ja "Registreeri" nupud, sisselogitud kasutajale peidetud)
Failinimi: HomeView.vue
Frontend rada: /

Vaatega seotud lisainfo:
Nupule "Võta ühendust" vajutades avatakse ApplicationFormView vaade, nupule "Logi sisse" vajutades LoginView vaade ja nupule "Registreeri" vajutades RegisterView vaade (kõik ilma API kutseta).
Sektsioonis "Peagi algavad koolitused" kuvatakse kolm koolitust (GET /api/trainings, limit=3). Kategooriate loend (GET /api/categories) kuvatakse otsingu juures. Otsinguriba viib teisele vaatele, mida selles taskis ei defineerita.
Mõlemad API kutsed käivitatakse vaate avanemisel, mitte kasutaja tegevuse peale.
```

## API märkmed — GET /api/trainings

```text
API: GET /api/trainings

Query parameetrid:
categoryId: Integer — kategooria filter, 0 = kõik
fundingTypeId: Integer — rahastustüübi filter, 0 = kõik
limit: Integer — koolituste arv lehel
page: Integer — lehekülje number, algab 0-st
trainingLang: String — õppekeele filter ("et"/"en")
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
HomeView kutsub: categoryId=0&fundingTypeId=0&limit=3&page=0&trainingLang=et&contentLang=et
totalPages ja totalElements kirjeldavad kogu filtreeritud tulemushulka. fundingTypes võib olla tühi list (training_funding_type kaudu).

Veateated: —
```

## API märkmed — GET /api/categories

```text
API: GET /api/categories

Query parameetrid:
contentLang: String — categoryName keel ("et"/"en") (valikuline)

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
Tagastab kõik süsteemis olevad kategooriad (category_translation kaudu tõlgitud).

Veateated: —
```
