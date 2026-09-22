# HomeView.vue

## Vaate märkmed

```text
Roll: Kõik rollid (külastajale kuvatakse "Logi sisse" ja "Registreeri" nupud, sisselogitud kasutajale peidetud)
Failinimi: HomeView.vue
Frontend rada: /

Vaatega seotud lisainfo:
Nupule "Võta ühendust" vajutades avatakse ApplicationFormView vaade, nupule "Logi sisse" vajutades LoginView vaade ja nupule "Registreeri" vajutades RegisterView vaade (kõik ilma API kutseta).
Sektsioonis "Peagi algavad koolitused" kuvatakse kolm lähiajal algavat koolitust (GET /api/trainings), koolituse kaardil kuvatakse ka alguskuupäev. Kategooriate loend (GET /api/categories) kuvatakse otsingu juures. Otsinguriba viib teisele vaatele, mida selles taskis ei defineerita.
Mõlemad API kutsed käivitatakse vaate avanemisel, mitte kasutaja tegevuse peale.
```

## API märkmed — GET /api/trainings

```text
API: GET /api/trainings?categoryId={categoryId}&fundingTypeId={fundingTypeId}&limit={limit}&page={page}&sort={sort}&trainingLang={trainingLang}&translationLang={translationLang}

Query parameetrid:
categoryId — valikuline, filtreerib koolitused kategooria järgi
fundingTypeId — valikuline, filtreerib koolitused rahastustüübi järgi (training_funding_type kaudu)
limit — valikuline (integer), lehekülje suurus (HomeView kutsub limit=3-ga)
page — valikuline (integer), lehekülje number, alates 0-st
sort — valikuline (String: "ASC" või "DESC"), sortimissuund koolituse alguskuupäeva (course.start_date) järgi — HomeView kasutab "ASC"
trainingLang — valikuline (String, nt "et"/"en"), filtreerib koolitused õppekeele järgi (training.training_language_id)
translationLang — valikuline (String, nt "et"/"en"), määrab tõlgitud väljade (title/shortDescription/categoryName/fundingTypeName) keele

TrainingSummaryPageDto.java
Response (200):
{
  "page": 0,
  "totalPages": 1,
  "totalElements": 2,
  "trainingSummaries": [
    {
      "trainingId": 1,
      "title": "Java algkursus",
      "shortDescription": "Java programmeerimise alused algajatele.",
      "categoryId": 1,
      "categoryName": "Programmeerimine",
      "startDate": "2026-10-05",
      "isOrderOnly": false,
      "isPromoted": true,
      "fundingTypes": [
        {
          "fundingTypeId": 1,
          "fundingTypeName": "Töötukassa"
        }
      ]
    }
  ]
}

API teenuse lisainfo:
startDate pärineb koolitusele lähimalt eelseisvalt course kirjelt ja võib olla null, kui koolitusel pole ühtegi eelseisvat kursust (nt order_only koolitus) — sortimisreeglit null väärtuste jaoks praegu ei defineerita. isOrderOnly pärineb training.is_order_only veerust, isPromoted training.is_promoted veerust. fundingTypes on koolitusele määratud rahastustüüpide loend (training_funding_type kaudu) — koolitusel võib olla null, üks või mitu rahastustüüpi. trainingSummaries on tagastatud koolituste leheküljeosa, totalElements ja totalPages kirjeldavad kogu tulemushulka (lehitsemise jaoks).

Veateated: —
```

## API märkmed — GET /api/categories

```text
API: GET /api/categories?translationLang={translationLang}

Query parameetrid:
translationLang — valikuline (String, nt "et"/"en"), määrab tagastatud categoryName välja keele

CategoryDto.java
Response (200):
[
  {
    "categoryId": 1,
    "categoryName": "Programmeerimine"
  }
]

API teenuse lisainfo:
Tagastab kõik süsteemis olevad kategooriad (category_translation kaudu tõlgitud), mida HomeView kuvab otsingu juures.

Veateated: —
```
