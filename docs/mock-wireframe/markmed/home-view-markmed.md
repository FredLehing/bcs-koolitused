# HomeView.vue — märkmed

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
API: GET /api/trainings?categoryId={categoryId}&fundingTypeId={fundingTypeId}&limit={limit}&page={page}&trainingLang={trainingLang}&contentLang={contentLang}

TrainingSummaryDto.java
Response (200):
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
      "isOrderOnly": false,
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
Kõik query parameetrid on valikulised: categoryId, fundingTypeId ja trainingLang ("et"/"en") filtreerivad, limit ja page (algab 0-st) leheküljestavad, contentLang ("et"/"en") määrab tõlgitud väljade (title, shortDescription, categoryName, fundingTypeName) keele. HomeView kutsub limit=3-ga.
totalPages ja totalElements kirjeldavad kogu filtreeritud tulemushulka. fundingTypes võib olla tühi list (training_funding_type kaudu).

Veateated: —
```

## API märkmed — GET /api/categories

```text
API: GET /api/categories?contentLang={contentLang}

CategoryDto.java
Response (200):
[
  {
    "categoryId": 1,
    "categoryName": "Programmeerimine"
  },
  ...
]

API teenuse lisainfo:
Tagastab kõik süsteemis olevad kategooriad (category_translation kaudu tõlgitud). contentLang ("et"/"en") on valikuline ja määrab categoryName keele.

Veateated: —
```
