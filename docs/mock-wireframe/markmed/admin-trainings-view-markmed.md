# AdminTrainingsView.vue — märkmed

Admini koolituste tabel. Otsused, andmebaasi view ettepanek ja skeemid: `docs/mock-wireframe/loo-mock-vaade/admin-trainings-view/admin-trainings-view-skeemid.md`. Uute teenuste (`GET /api/admin-trainings`, `GET /api/training-titles`, `DELETE /api/training/{trainingId}`, `PUT /api/training/{trainingId}/restore`) DTO-d on ettepanek.

## Vaate märkmed

```text
Roll: Admin
Failinimi: AdminTrainingsView.vue
Frontend rada: /admin-trainings

Vaatega seotud lisainfo:
Ülal vahelehed (AdminTabs.vue): Koolituste päringud | Registreerumised | Koolitused | Koolituste kalender | Koolitajad | Koolitusruumid | Kontod — samad lingid ja järjekord mis menüüs "Admin", selle vaate vaheleht on aktiivne. Kitsal ekraanil on vahelehed ühel keritaval real, aktiivne keritakse keskele.
Tabeli rida = üks koolitus; vaikimisi ainult aktiivsed (status "U" ja "P"), kustutatud ("D") näeb staatuse filtriga "Kustutatud". Nimi ja kategooria kuvatakse kasutajaliidese keeles (store'i contentLang); kui selles keeles tõlget pole, kuvatakse põhikeele oma. Keele vahetamisel navbaris laaditakse tabel, kategooriad, rahastustüübid ja nimede ettepanekud uuesti; otsing, filtrid, sorteerimine ja leht jäävad alles.
Otsinguväli otsib ainult koolituse nimest; trükkimise ajal pakub datalist nimesid (GET /api/training-titles). Otsing käivitub nupu "Otsi" või Enteri peale. Nupu kõrval on küsimärgi ikoon, mille tooltip selgitab: otsitakse nimest, iga sõna peab esinema, käändeid ei kohandata; nimede valikus on ainult aktiivsed koolitused — kustutatud koolituse leidmiseks vali filtrites Staatus "Kustutatud".
Kaart "Otsingu filtrid" on vaikimisi peidus (link "▾ Ava otsingu filtrid" / "▴ Peida otsingu filtrid"). Filtrid rakenduvad nupuga "Filtreeri" ja jäävad kehtima ka peidetud kaardiga (märk "N filtrit aktiivne"); "Tühjenda filtrid" taastab vaikimisi väärtused.
Veeru pealkirja klõps sorteerib backendis (ka Staatus: Mustand → Publitseeritud → Kustutatud ja Tõlked: puuduvad eespool): uus veerg → kasvav, sama veerg uuesti → suund vahetub. Vaikimisi Lisatud kahanevalt. Otsing, filtrid ja sorteerimine alustavad alati lehelt 0; leheküljestus (PaginationNav.vue) 10 rida lehel.
Vaade avaneb navbari menüüst "Admin" → "Koolitused". Pealkiri "Koolitused". Pealkirja real paremal on nupp "Lisa uus koolitus" → /training-form.
Kuupäevad kuvatakse kujul 30/09/2026. "Vaata" → /training?trainingId={id}&trainingTranslationId={id}, "Muuda" → /training-form?trainingId={id}&trainingTranslationId={id}. Staatuse nupp (TrainingStatusButton.vue: "Publitseeri" / "Liiguta mustandisse" / "Taasta") ja kustutamise ikoon (TrainingDeleteButton.vue) küsivad kinnitust modaliga, teevad API kutse ise ja annavad vaatele sündmusega teada; vaade laadib tabeli uuesti. Kustutatud real on ainult nupp "Taasta" (Vaata/Muuda/Kustuta on peidus).
```

## API märkmed — GET /api/admin-trainings

```text
API: GET /api/admin-trainings

Query parameetrid:
contentLang: String — nime ja kategooria keel ("et"/"en")
searchText: String — otsingutekst, "" = kõik; sõnad eraldatakse tühikute kohalt, iga sõna peab esinema (contains, tõstutundetu) title väljas
categoryId: Integer — kategooria filter, 0 = kõik
trainingLanguageId: Integer — õppekeele filter, 0 = kõik
fundingTypeId: Integer — rahastustüübi filter, 0 = kõik
status: String — "U" = mustand, "P" = publitseeritud, "D" = kustutatud (valikuline, puudub = "U" ja "P")
isOrderable: Boolean — tellitav jah/ei (valikuline)
isPromoted: Boolean — esile tõstetud jah/ei (valikuline)
hasAllTranslations: Boolean — true = kõik tõlked olemas, false = mõni tõlge puudub (valikuline)
sortBy: String — "createdAt" / "updatedAt" / "title" / "categoryName" / "trainingLanguageCode" / "status" / "hasAllTranslations"
sortDirection: String — "asc" / "desc"
page: Integer — lehekülje number, algab 0-st
limit: Integer — ridade arv lehel

Response (200):
AdminTrainingSummaryDto.java
{
  "totalPages": 2,
  "totalElements": 13,
  "adminTrainingSummaries": [
    {
      "trainingId": 13,
      "trainingTranslationId": 23,
      "title": "Tehisaru töövahendid arendajale",
      "categoryId": 1,
      "categoryName": "Programmeerimine",
      "trainingLanguageCode": "et",
      "trainingLanguageFlagIconCode": "fi-ee",
      "status": "P",
      "isOrderable": true,
      "isPromoted": true,
      "createdAt": "2026-09-25T12:10:00Z",
      "updatedAt": "2026-09-26T06:00:00Z",
      "hasAllTranslations": false,
      "missingTranslationLanguageCodes": [
        "en",
        ...
      ],
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
Andmed tulevad view'st admin_training_summary. Ilma status parameetrita tagastatakse ainult aktiivsed ("U" ja "P"). Kui koolitusel pole contentLang tõlget, on title, categoryName ja trainingTranslationId põhikeele omad. Valikulise parameetri puudumine = filtrit ei rakendata. Tulemus sorteeritakse sortBy/sortDirection järgi, lisaks alati trainingId järgi; tundmatu sortBy korral kasutatakse createdAt. sortBy "status" kasutab töövoo järjekorda (view veerg status_order: U → P → D), "hasAllTranslations" kasvavalt = puuduvad tõlked eespool. updatedAt = hiliseim training ja selle tõlgete updated_at. missingTranslationLanguageCodes = tõlkekeeled (requiresTranslation = true), milles tõlge puudub; tühi list, kui kõik on olemas. fundingTypes võib olla tühi list.

Veateated: —
```

## API märkmed — GET /api/training-titles

```text
API: GET /api/training-titles

Query parameetrid:
contentLang: String — nimede keel ("et"/"en")

Response (200):
TrainingTitleDto.java
[
  {
    "trainingId": 1,
    "title": "Java algkursus"
  },
  ...
]

API teenuse lisainfo:
Kõigi aktiivsete (status ≠ "D") koolituste nimed contentLang keeles, puuduva tõlke korral põhikeeles (sama view admin_training_summary). Sorteeritud title järgi. AdminTrainingsView kasutab neid otsinguvälja datalist'is ettepanekuteks.

Veateated: —
```

## API märkmed — DELETE /api/training/{trainingId}

```text
API: DELETE /api/training/{trainingId}

Response (200): NONE

API teenuse lisainfo:
Soft delete: määrab training.status = "D" (TrainingStatus.DELETED) ja uuendab updated_at. Tõlkeid ega rahastustüüpe ei kustutata. Kustutatud koolitus ei ole avalikus nimekirjas ega nimede ettepanekutes; admini tabelis näeb seda filtriga "Kustutatud" ja saab taastada (PUT /api/training/{trainingId}/restore). Kustutada saab nii mustandit kui ka publitseeritud koolitust. Frontendis teeb kutse TrainingDeleteButton.vue.

Veateated:
HTTP: 404
errorCode: PRIMARY_KEY_NOT_FOUND
message: "Ei leidnud primary keyd 'trainingId' väärtusega: 123"
```

## API märkmed — PUT /api/training/{trainingId}/publish

```text
API: PUT /api/training/{trainingId}/publish

Response (200): NONE

API teenuse lisainfo:
Tegevusteenus (erand URL-ide kokkuleppest): määrab training.status = "P" (publitseeritud). Request body puudub. Kutse teeb TrainingStatusButton.vue ("Publitseeri") pärast kinnituse modalit; AdminTrainingsView laadib seejärel tabeli uuesti. Kustutatud koolituse (status "D") korral backend keeldub.

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
Tegevusteenus (erand URL-ide kokkuleppest): määrab training.status = "U" (mustand). Request body puudub. Kutse teeb TrainingStatusButton.vue ("Liiguta mustandisse") pärast kinnituse modalit; AdminTrainingsView laadib seejärel tabeli uuesti. Kustutatud koolituse (status "D") korral backend keeldub.

Veateated:
HTTP: 404
errorCode: PRIMARY_KEY_NOT_FOUND
message: "Ei leidnud primary keyd 'trainingId' väärtusega: 123"

HTTP: 403
errorCode: TRAINING_DELETED
message: "Kustutatud koolituse staatust ei saa muuta, taasta see enne"
```

## API märkmed — PUT /api/training/{trainingId}/restore

```text
API: PUT /api/training/{trainingId}/restore

Response (200): NONE

API teenuse lisainfo:
Tegevusteenus (erand URL-ide kokkuleppest): taastab kustutatud koolituse — määrab training.status = "U" (mustand) ja uuendab updated_at. Publitseerimine on eraldi samm. Request body puudub. Kutse teeb TrainingStatusButton.vue ("Taasta", ainult status "D" korral) pärast kinnituse modalit; AdminTrainingsView laadib seejärel tabeli ja nimede ettepanekud uuesti.

Veateated:
HTTP: 404
errorCode: PRIMARY_KEY_NOT_FOUND
message: "Ei leidnud primary keyd 'trainingId' väärtusega: 123"
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
Tagastab kõik kategooriad contentLang keeles. AdminTrainingsView kasutab neid filtri "Kategooria" valikutena (lisaks "Kõik" = 0) ja laadib keele vahetusel uuesti.

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
Tagastab kõik rahastustüübid contentLang keeles. AdminTrainingsView kasutab neid filtri "Rahastus" valikutena (lisaks "Kõik" = 0) ja laadib keele vahetusel uuesti.

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
    "languageName": "Eesti",
    "isMainLanguage": true,
    "requiresTranslation": true,
    "flagIconCode": "fi-ee"
  },
  ...
]

API teenuse lisainfo:
Tagastab kõik süsteemi keeled. AdminTrainingsView kasutab neid filtri "Koolituse keel" valikutena (languageId → trainingLanguageId, lisaks "Kõik" = 0). languageName ei ole tõlgitud, seega contentLang parameetrit pole.

Veateated: —
```
