# TrainingFormView.vue (state: "new-training") — märkmed

Üks kolmest TrainingFormView oleku märkmete failist (vt ka teisi `training-form-view-state-*-markmed.md` faile). Plaan ja skeemid: `docs/mock-wireframe/loo-mock-vaade/`.

## Vaate märkmed

AI tegevused on selles etapis ühendatud päris POST-teenustega AiTrainingController klassis; vastus on placeholder-objekt. AI päringu ajal on tekstide muutmine, faili vahetamine ja salvestamine keelatud; vea korral säilivad tekstid ja fail. Vaate vahetamisel vana AI tulemust ei rakendata.
```text
Roll: Admin
Failinimi: TrainingFormView.vue
Frontend rada: /training-form

Vaatega seotud lisainfo:
state: "new-training" — URL-is query parameetreid pole. Pealkiri "Lisa uus koolitus". Vorm on tühi; tõlke väljad (pealkiri, lühikirjeldus, kirjeldus) täidetakse põhikeeles (et); rippmenüüd (kategooriad, rahastustüübid) laaditakse kasutajaliidese keeles (store'i contentLang) ja laaditakse keele vahetamisel navbaris uuesti — vormi sisu jääb alles. Lipukesi ja staatuse nuppe pole.
Koolitajad: valitud koolitajate nimekiri (× eemaldab, ↑ ↓ muudab järjekorda, esimene kuvatakse esimesena); "+ Lisa koolitaja" avab "Vali koolitaja" modali otsinguga (juba valitud koolitajaid ei pakuta). Võib jääda tühjaks. Rahastustüübid on checkboxid, "Tellitav" ja "Esile tõstetud" switchid.
Nupp "Lisa" → POST /api/training (userId localStorage'ist). Backend loob koolituse staatusega "U" koos põhikeele tõlkega. Vastuse trainingId ja trainingTranslationId järgi tehakse router.replace → state "update".
Õppekava (PDF): tõlke väljade all valikuline PDF-fail (kuni 10 MB) — "Vali fail…"; valitud fail salvestub koos "Lisa" nupuga (curriculum = Base64, curriculumLabel = sõna "Õppekava" tõlke keeles i18n-ist). Failinime teeb backend: puhastatud pealkiri + sõna, nt power-bi-edasijoudnutele-oppekava.pdf. Kui PDF on valitud, nupp "Täida vorm PDF + AI abiga" saadab salvestamata faili backendile → Gemini AI; vastus täidab pealkirja, lühikirjelduse ja kirjelduse. DB-sse midagi ei salvestata enne "Lisa" vajutamist. Salvestamata tekstide korral küsitakse ülekirjutamiseks kinnitust. Tooltip selgitab voogu.
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
search: String — otsingusõna koolitaja nimest, tühi = kõik

Response (200):
LecturerDto.java
[
  {
    "lecturerId": 1,
    "lecturerName": "Rain Tüür"
  },
  ...
]

API teenuse lisainfo:
Tagastab aktiivsed koolitajad, kelle nimi (lecturer.full_name) sisaldab otsingusõna, tõstutundetult. Kasutatakse "Vali koolitaja" modalis; juba valitud koolitajad jätab frontend nimekirjast välja.

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
  "lecturerIds": [
    1,
    ...
  ],
  "isOrderable": true,
  "isPromoted": false,
  "fundingTypeIds": [
    1,
    ...
  ],
  "title": "Power BI edasijõudnutele",
  "shortDescription": "Andmemudelid, DAX ja interaktiivsed aruanded.",
  "description": "Kursusel ehitatakse Power BI-s andmemudel, kirjutatakse DAX-valemeid ja luuakse interaktiivseid aruandeid.",
  "curriculum": "JVBERi0xLjcKJeLjz9MK...",
  "curriculumLabel": "Õppekava"
}

Response (200):
TrainingCreateResponseDto.java
{
  "trainingId": 3,
  "trainingTranslationId": 5
}

API teenuse lisainfo:
Loob training rea (status = "U", määrab backend), training_funding_type read ja et tõlke (language_id = 1) ühes transaktsioonis. lecturerIds = koolitajate ID-d kuvamise järjekorras (sort_order = positsioon 1, 2, …), võib olla tühi list; backend kirjutab training_lecturer read üle. Uus ID peab olema aktiivne koolitaja; juba seotud kustutatud koolitaja võib jääda. Sama PRIMARY_KEY_NOT_FOUND muster kehtib ka trainingLanguageId, lecturerIds (sõnumis 'lecturerId') ja fundingTypeIds väljadele. Õppekava: curriculum = PDF Base64 (valikuline, null = õppekava pole), curriculumLabel = sõna "Õppekava" tõlke keeles (kohustuslik). Backend kontrollib faili (algus %PDF-, kuni 10 MB) ja lisab training_translation_curriculum rea; failinimi = puhastatud pealkiri + "-" + puhastatud curriculumLabel + ".pdf" (nt power-bi-edasijoudnutele-oppekava.pdf). Vigane Base64 või puuduv curriculumLabel → 400 INCORRECT_INPUT.

Veateated:
HTTP: 404
errorCode: PRIMARY_KEY_NOT_FOUND
message: "Ei leidnud primary keyd 'categoryId' väärtusega: 123"

HTTP: 404
errorCode: PRIMARY_KEY_NOT_FOUND
message: "Ei leidnud primary keyd 'locationId' väärtusega: 123"

HTTP: 403
errorCode: CURRICULUM_TYPE_NOT_ALLOWED
message: "Lubatud on ainult PDF-fail"

HTTP: 403
errorCode: CURRICULUM_TOO_LARGE
message: "Õppekava on liiga suur, lubatud kuni 10 MB"
```


## API märkmed — POST /api/ai-training/pdf (TO BE IMPLEMENTED)

```text
API: POST /api/ai-training/pdf

Request: multipart/form-data
curriculum: kohustuslik PDF-fail (kuni 10 MB)

Response (200):
AiTrainingContentDto.java
{
  "title": "PDF-ist genereeritud pealkiri (TO BE IMPLEMENTED)",
  "shortDescription": "PDF-ist genereeritud lühikirjeldus (TO BE IMPLEMENTED)",
  "description": "PDF-ist genereeritud kirjeldus (TO BE IMPLEMENTED)"
}

API teenuse lisainfo:
TO BE IMPLEMENTED: controller tagastab praegu fikseeritud placeholder-väärtused; DB-d ei loeta ja Geminit ei kutsuta.
Valitud salvestamata PDF saadetakse backendile, kus tulevikus töödeldakse see Gemini AI-ga. Vastus täidab vormi, kuid DB-sse midagi ei salvestata; admin kontrollib tulemust ja vajutab ise "Lisa". Praegu tagastab controller placeholder-väärtused. Sama vastuse DTO on mõeldud ka AI tõlkele.

Veateated: —
```
