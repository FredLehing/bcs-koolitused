# TrainingFormView.vue (state: "update") — märkmed

Üks kolmest TrainingFormView oleku märkmete failist (vt ka teisi `training-form-view-state-*-markmed.md` faile). Plaan ja skeemid: `docs/mock-wireframe/loo-mock-vaade/`.

## Vaate märkmed

AI tegevused on selles etapis ühendatud päris POST-teenustega AiTrainingController klassis; vastus on placeholder-objekt. AI päringu ajal on tekstide muutmine, faili vahetamine ja salvestamine keelatud; vea korral säilivad tekstid ja fail. Vaate vahetamisel vana AI tulemust ei rakendata.
```text
Roll: Admin
Failinimi: TrainingFormView.vue
Frontend rada: /training-form?trainingId={id}&trainingTranslationId={id}

Vaatega seotud lisainfo:
state: "update" — URL-is trainingId ja trainingTranslationId. Pealkiri "Muuda koolitust" koos staatuse märgisega (Mustand / Publitseeritud). Vorm on eeltäidetud; rippmenüüd (kategooriad, rahastustüübid) laaditakse kasutajaliidese keeles (store'i contentLang) ja laaditakse keele vahetamisel navbaris uuesti — vormi sisu jääb alles. Iga router.replace järel laaditakse andmed uuesti.
Lipukesed: tõlge olemas → värviline lipp (klikk avab selle tõlke), tõlge puudub → hall lipp (klikk → state "new-translation").
"Salvesta" → PUT /api/training/{trainingId} (koolituse väljad + avatud tõlge ühes transaktsioonis). "Tee AI tõlge" on ainult mitte-põhikeele tõlkel: täidab vaid vormi, ei salvesta.
status "U" → nupp "Publitseeri", status "P" → nupp "Liiguta mustandisse"; mõlemal kinnituse modal (PUT /api/training/{trainingId}/publish või /unpublish).
Õppekava (PDF): olemas → failinimi lingina (klikk = allalaadimine) + suurus, nupud "Vaheta" ja "Eemalda" (kinnitust ei küsi — jõustub salvestamisel, "Tühista" võtab tagasi); puudub → "Vali fail…" (PDF, kuni 10 MB). "Salvesta" saadab curriculum / isCurriculumRemoved / curriculumLabel (sõna "Õppekava" avatud tõlke keeles, mitte kasutajaliidese keeles); backend arvutab failinime uuesti ka siis, kui muutus ainult pealkiri. Pärast salvestamist laaditakse tõlge uuesti (curriculumFileName). Nupp "Täida vorm PDF + AI abiga" kasutab uut vormis valitud faili, selle puudumisel salvestatud õppekava. AI vastus täidab vormi, DB-sse midagi ei kirjutata enne "Salvesta" vajutamist. Salvestamata tekstide korral küsitakse ülekirjutamiseks kinnitust. Tooltip selgitab, millist PDF-i kasutatakse.
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
  "description": "Kursusel õpitakse Java süntaksit, objektorienteeritud programmeerimist ja põhilisi andmestruktuure.",
  "curriculumFileName": "java-algkursus-oppekava.pdf",
  "curriculumFileSize": 846213
}

API teenuse lisainfo:
Laadib avatud tõlke (URL-i trainingTranslationId). languageCode näitab, mis keeles avatud tõlge on (tõlke vormi pealkiri, AI nupu nähtavus). Kui tõlke koolitus on kustutatud (status "D"), on tõlge nagu olematu: 404 PRIMARY_KEY_NOT_FOUND. curriculumFileName = õppekava failinimi (nt java-algkursus-oppekava.pdf), curriculumFileSize = suurus baitides; mõlemad null, kui õppekava pole. Faili ennast ei tagastata (GET /api/training-translation/{trainingTranslationId}/curriculum).

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
Koolituse olemasolevad tõlked. Frontend võrdleb languageCode väärtusi GET /api/languages tõlkekeeltega (requiresTranslation = true): tõlge olemas → värviline lipp, puudub → hall lipp. isMainLanguage tuleb language.is_main_language veerust; põhikeele tõlkega eeltäidetakse uue tõlke vorm. Kustutatud koolitus (status "D") on nagu olematu: 404 PRIMARY_KEY_NOT_FOUND.

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

## API märkmed — PUT /api/training/{trainingId}

```text
API: PUT /api/training/{trainingId}

Request body:
TrainingUpdateRequestDto.java
{
  "categoryId": 1,
  "trainingLanguageId": 1,
  "locationId": 1,
  "lecturerIds": [
    1,
    ...
  ],
  "isOrderable": true,
  "isPromoted": true,
  "fundingTypeIds": [
    1,
    ...
  ],
  "trainingTranslationId": 1,
  "title": "Java algkursus",
  "shortDescription": "Java programmeerimise alused algajatele.",
  "description": "Kursusel õpitakse Java süntaksit, objektorienteeritud programmeerimist ja põhilisi andmestruktuure.",
  "curriculum": null,
  "isCurriculumRemoved": false,
  "curriculumLabel": "Õppekava"
}

Response (200): NONE

API teenuse lisainfo:
Salvestab koolituse väljad ja trainingTranslationId tõlke ühes transaktsioonis. training_funding_type read kirjutatakse fundingTypeIds järgi üle, training_lecturer read lecturerIds järgi (lecturerIds = koolitajate ID-d kuvamise järjekorras (sort_order = positsioon 1, 2, …), võib olla tühi list; backend kirjutab training_lecturer read üle. Uus ID peab olema aktiivne koolitaja; juba seotud kustutatud koolitaja võib jääda.) Kustutatud koolitus (status "D") on nagu olematu: 404 PRIMARY_KEY_NOT_FOUND. Õppekava: curriculum ≠ null → uus fail (kontroll nagu POST puhul), training_translation_curriculum rida lisatakse või asendatakse; isCurriculumRemoved = true → rida kustutatakse (curriculum peab siis olema null); muidu, kui õppekava on olemas, arvutatakse ainult failinimi uuesti (pealkiri võis muutuda). curriculumLabel = sõna "Õppekava" avatud tõlke keeles (kohustuslik). Vigane Base64, puuduv curriculumLabel või isCurriculumRemoved koos curriculum'iga → 400 INCORRECT_INPUT.

Veateated:
HTTP: 404
errorCode: PRIMARY_KEY_NOT_FOUND
message: "Ei leidnud primary keyd 'trainingId' väärtusega: 123"

HTTP: 404
errorCode: PRIMARY_KEY_NOT_FOUND
message: "Ei leidnud primary keyd 'trainingTranslationId' väärtusega: 123"

HTTP: 403
errorCode: CURRICULUM_TYPE_NOT_ALLOWED
message: "Lubatud on ainult PDF-fail"

HTTP: 403
errorCode: CURRICULUM_TOO_LARGE
message: "Õppekava on liiga suur, lubatud kuni 10 MB"
```

## API märkmed — GET /api/training-translation/{trainingTranslationId}/curriculum

```text
API: GET /api/training-translation/{trainingTranslationId}/curriculum

Response (200): PDF-fail
Content-Type: application/pdf
Content-Disposition: attachment; filename="java-algkursus-oppekava.pdf"
Cache-Control: no-cache

API teenuse lisainfo:
Tagastab tõlke õppekava faili (training_translation_curriculum.file), failinimi = file_name. Vormis klikk õppekava failinimel; hiljem ka avalikus vaates /training. Backendis autentimist pole, seega on fail kättesaadav nii mustandi kui publitseeritud koolitusel. Kui õppekava pole või tõlke koolitus on kustutatud (status "D") → 404 PRIMARY_KEY_NOT_FOUND. no-cache, sest fail võib sama URL-i all vahetuda.

Veateated:
HTTP: 404
errorCode: PRIMARY_KEY_NOT_FOUND
message: "Ei leidnud primary keyd 'trainingTranslationId' väärtusega: 123"
```

## API märkmed — POST /api/ai-training/translation/{trainingId}

```text
API: POST /api/ai-training/translation/{trainingId}

Query parameetrid:
languageId: Integer — sihtkeel, kuhu tõlgitakse

Response (200):
AiTrainingContentDto.java
{
  "title": "AI-ga tõlgitud pealkiri (TO BE IMPLEMENTED)",
  "shortDescription": "AI-ga tõlgitud lühikirjeldus (TO BE IMPLEMENTED)",
  "description": "AI-ga tõlgitud kirjeldus (TO BE IMPLEMENTED)"
}

API teenuse lisainfo:
TO BE IMPLEMENTED: AiTrainingController tagastab praegu fikseeritud placeholder-objekti. Andmebaasi lugemist, Gemini kutset ega ärivigade kontrolli veel ei tehta. Järgnev andmevoog kirjeldab hilisemat AI teostust; planeeritud ärivead on backend taskis.
Tõlgib alati andmebaasi salvestatud põhikeele (language.is_main_language = true) tõlke AI abil sihtkeelde — vormi sisu ei kasutata. Andmebaasi midagi ei salvestata. description HTML-märgendid säilitatakse. Vastusel päis Cache-Control: no-store. Kustutatud koolitus (status "D") on nagu olematu: 404 PRIMARY_KEY_NOT_FOUND.

Veateated: —
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


## API märkmed — POST /api/ai-training/pdf/{trainingTranslationId} (TO BE IMPLEMENTED)

```text
API: POST /api/ai-training/pdf/{trainingTranslationId}

Path parameeter:
trainingTranslationId: Integer — avatud tõlke ID

Request: multipart/form-data
curriculum: valikuline uus PDF-fail (kuni 10 MB). Kui osa puudub, kasutatakse tõlke salvestatud õppekava.

Response (200):
AiTrainingContentDto.java
{
  "title": "PDF-ist genereeritud pealkiri (TO BE IMPLEMENTED)",
  "shortDescription": "PDF-ist genereeritud lühikirjeldus (TO BE IMPLEMENTED)",
  "description": "PDF-ist genereeritud kirjeldus (TO BE IMPLEMENTED)"
}

API teenuse lisainfo:
TO BE IMPLEMENTED: controller tagastab praegu fikseeritud placeholder-väärtused; DB-d ei loeta ja Geminit ei kutsuta.
Päringus olev uus PDF on eelistatud; selle puudumisel loetakse salvestatud õppekava. Tulevikus saadetakse PDF Gemini AI-le. DB-sse midagi ei salvestata; vastus täidab vormi ja admin salvestab selle eraldi "Salvesta" nupuga. Praegune controller tagastab placeholder-väärtused.

Veateated: —
```


## Tagasitee — täiendatud navigatsioon

Vaade võtab vastu valikulise `returnTo` query parameetri ja kuvab lingi „← Tagasi“ (`BackLink.vue`). Avamislingid annavad kaasa lähtevaate täieliku URL-i. Tagasilingi puuduv, väline, tundmatu või iseendale osutav siht asendatakse vaate varusihtkohaga. Oleku- ja tõlkevahetus ei kaota tagasiteed. Eraldi nimega nimekirja-/kalendrinupud säilitavad oma sihtkoha. Täpne [kaardistus ja varusihtkohad](../../tasks/frontend/return-to-navigation.md) ning [skeemid](../loo-mock-vaade/return-to-navigation-skeemid.md).
