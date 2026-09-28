# TrainingFormView.vue — tööde järjekord

Järjekord: **ühe oleku (`state: "new-training"`) backend teenused → kogu vaate frontend task → ülejäänud backend teenused**.

Nii saab esimese etapi järel juba päris backendi vastu uue koolituse lisamist testida, vaate ülejäänud olekud (`update`, `new-translation`) ehitatakse valmis ja ühendatakse päris teenustega jooksvalt, kui 3. etapi teenused valmivad.

Allikad: märkmed `docs/mock-wireframe/markmed/training-form-view-state-*-markmed.md`, skeemid `training-form-view-skeemid.md`, läbimäng `training-form-view-labimang.html`.

Taskifailide nimed järgivad olemasolevat kokkulepet (`docs/tasks/backend/<METOOD>-api-<tee>.md`, path variable ilma loogeliste sulgudeta).

---

## 0. Eeltöö

| # | Töö | Märkus |
|---|---|---|
| 0.1 | Andmebaas lokaalselt uuesti luua (`1_reset` → `2_create` → `3_import`) | `training.status varchar(1)`, `language.is_main_language`, uued asukohad |
| 0.2 | `TrainingStatus` enum (`UNPUBLISHED("U")`, `PUBLISHED("P")`) | `ApiStatus` "D" tähendab kustutatud — ära kasuta seda koolituse juures |

---

## 1. Etapp — `state: "new-training"` backend teenused

Järjekord lihtsamast keerulisemani. 1.1–1.4 on lihtsad nimekirjad ja neid saab teha paralleelselt; `/api/categories`, `/api/funding-types` ja `/api/languages` kasutab ka TrainingsView.

| # | Teenus | Taskifail | Keerukus | Märkus |
|---|---|---|---|---|
| 1.1 | `GET /api/languages` | `GET-api-languages.md` | lihtne | `LanguageRepository` ja `LanguageMapper` on olemas; vastus `SystemLanguageDto` |
| 1.2 | `GET /api/categories?contentLang=` | `GET-api-categories.md` | lihtne | `category_translation` kaudu |
| 1.3 | `GET /api/funding-types?contentLang=` | `GET-api-funding-types.md` | lihtne | `FundingTypeDto` on olemas |
| 1.4 | `GET /api/locations` | `GET-api-locations.md` | lihtne | tõlkimata, `contentLang` puudub |
| 1.5 | `GET /api/lecturers?search=` | `GET-api-lecturers.md` | keskmine | otsing `full_name` järgi, tõstutundetu |
| 1.6 | `POST /api/training` | `POST-api-training.md` | keerukas | loob `training` (status `U`) + `training_funding_type` read + põhikeele tõlke ühes transaktsioonis; `PRIMARY_KEY_NOT_FOUND` kontrollid |

**Etapi tulemus:** uue koolituse saab päris andmebaasi lisada (Swaggerist testitav).

---

## 2. Etapp — kogu vaate frontend task

| # | Töö | Taskifail | Märkus |
|---|---|---|---|
| 2.1 | `TrainingFormView.vue` kõik kolm olekut | `docs/tasks/frontend/training-form-view.md` | vt allpool |

Taskis kirjeldatakse terve vaade:
- `state` tuletamine URL-ist (`new-training` / `update` / `new-translation`) ja `$route.query` jälgija, mis laadib andmed iga `router.replace`-i järel uuesti;
- store'i `contentLanguages` (`isMainLanguage`), lipukesed, "Vali lektor" modal, kinnituse modal, AI tõlke nupp koos tooltip'iga;
- iga oleku päringud (märkmete failidest).

**Testitavus selles etapis:** olek `new-training` töötab päris backendiga. Olekute `update` ja `new-translation` UI ehitatakse valmis, aga päringud hakkavad tööle alles 3. etapi teenustega — seni saab kasutada märkmete JSON näidiseid.

---

## 3. Etapp — ülejäänud backend teenused

Järjekord tuleb sõltuvustest: kõigepealt lugemine (vaade avaneb olekus `update`), siis salvestamine, siis olek `new-translation`, lõpuks AI (väline sõltuvus).

| # | Teenus | Taskifail | Olek | Keerukus | Märkus |
|---|---|---|---|---|---|
| 3.1 | `GET /api/training/{trainingId}` | `GET-api-training-trainingId.md` | update, new-translation | keskmine | `TrainingDto` koos `fundingTypeIds` ja `defaultLecturerName` |
| 3.2 | `GET /api/training/{trainingId}/training-translations` | `GET-api-training-trainingId-training-translations.md` | update, new-translation | lihtne | lipukesed; `isMainLanguage` |
| 3.3 | `GET /api/training-translation/{trainingTranslationId}` | `GET-api-training-translation-trainingTranslationId.md` | update, new-translation | lihtne | → pärast 3.1–3.3 avaneb olek `update` päris andmetega |
| 3.4 | `PUT /api/training/{trainingId}` | `PUT-api-training-trainingId.md` | update | keerukas | koolituse väljad + avatud tõlge ühes transaktsioonis; `training_funding_type` üle kirjutamine |
| 3.5 | `PUT /api/training/{trainingId}/publish` | `PUT-api-training-trainingId-publish.md` | update, new-translation | lihtne | tegevusteenus, body puudub |
| 3.6 | `PUT /api/training/{trainingId}/unpublish` | `PUT-api-training-trainingId-unpublish.md` | update, new-translation | lihtne | sama mis 3.5, teine staatus — sobib teha koos |
| 3.7 | `POST /api/training/{trainingId}/training-translation` | `POST-api-training-trainingId-training-translation.md` | new-translation | keskmine | → pärast seda töötab olek `new-translation` (v.a AI) |
| 3.8 | `GET /api/training/{trainingId}/ai-translation?languageId=` | `GET-api-training-trainingId-ai-translation.md` | update, new-translation | keerukas | väline AI teenus, uued veakoodid (403/404/503), 503 käsitlus `RestExceptionHandler`-isse; `Cache-Control: no-store` |

**Etapi tulemus:** kõik kolm olekut töötavad päris backendiga.

---

## Lahtised küsimused enne taskide loomist

- Taskide loomise skillid (`skill-loo-backend-task`, `skill-loo-frontend-task`) eeldavad mockupi PDF-i lehekülge. TrainingFormView lehed tulevad Balsamiqi hiljem — kuni selleni saab taskid koostada märkmete failide põhjal.
- "Ettepanek" märgisega DTO-d ja veakoodid (vt märkmed) tuleb enne vastava taski loomist kinnitada.
