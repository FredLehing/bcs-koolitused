# Süsteemi keelte nimekirja päring

**Teenus:** `GET /api/languages`

**Kasutav vaade:** `TrainingFormView.vue` (`/training-form`), `TrainingsView.vue` (`/trainings`)

![Mockup](../../mock-wireframe/pdf-images/TrainingFormView-state-new-training.png)

## Sisend

Teenusel puuduvad sisendid.

## Väljund

**Response (200 OK):** Kõigi süsteemi keelte nimekiri.

`SystemLanguageDto.java`:

```json
[
  {
    "languageId": 1,
    "languageCode": "et",
    "languageName": "Eesti"
  },
  {
    "languageId": 2,
    "languageCode": "en",
    "languageName": "English"
  }
]
```

Väljade selgitused:

- `languageId` — `language.id`.
- `languageCode` — `language.code` (`et`/`en`).
- `languageName` — `language.name`. **Väli ei ole tõlgitud** (contentLang parameetrit teenusel pole) — tagastatakse alati sama väärtus (nt "Eesti", "English"), sõltumata kasutaja valitud kuvakeelest.

Tulemus on sorteeritud `language.id` järgi kasvavalt (lühikeste valikunimekirjade ühtne reegel).

Tabelis on lisaks veerg `is_main_language` (märgib põhikeele — hetkel `et`), kuid seda vastuses ei kajastata, kuna mockupi `SystemLanguageDto` seda ei sisalda.

## Eesmärk

`TrainingFormView` vaates (roll: Admin) kasutatakse nimekirja "Koolituse keel" rippmenüü täitmiseks ning `languageId` ↔ `languageCode` teisendamiseks. `TrainingsView` vaates (avalik, kõik rollid) kasutatakse sama nimekirja "Koolituse keel" filtri valikute kuvamiseks (`languageId` → `trainingLanguageId`). Mõlemal juhul laaditakse nimekiri vaate avanemisel ja tulemust kasutatakse ainult valikuvõimaluste kuvamiseks, mitte otse koolituste filtreerimiseks (see toimub `GET /api/trainings` `trainingLanguageId` parameetriga).

## Seotud andmebaasi tabelid

Vt `docs/database/2_create.sql`.

### language

Süsteemi keeled. `is_main_language` märgib põhikeele (unikaalsuspiirang tagab, et tõsi on ainult ühel real).

```sql
CREATE TABLE language
(
    id               serial      NOT NULL,
    code             varchar(2)  NOT NULL,
    name             varchar(50) NOT NULL,
    is_main_language boolean     NOT NULL,
    CONSTRAINT language_pk PRIMARY KEY (id),
    CONSTRAINT language_code_uq UNIQUE (code)
);

CREATE UNIQUE INDEX language_main_language_uq ON language (is_main_language) WHERE is_main_language;
```

Näidisandmed (`docs/database/3_import.sql`):

| id | code | name | is_main_language |
|---|---|---|---|
| 1 | et | Eesti | true |
| 2 | en | English | false |

Andmebaasis on hetkel ainult need kaks keelt (mockupi wireframe'il nähtav venekeelne valik `ru` andmebaasis puudub — vt "Avatud küsimused").

## Veaolukorrad

| Olukord | Status code | Response body |
|---|---|---|
| Ootamatu serveri viga | 500 Internal Server Error | Standardne `ApiError` vastus |

Teenusel pole sisendeid ega mockupi veateateid (`Veateated: —` mõlemas märkmete failis), seega muid veaolukordi pole.

## Vastuvõtu kriteeriumid

- [ ] Endpoint `GET /api/languages` on olemas ja tagastab `List<SystemLanguageDto>` struktuuriga vastuse
- [ ] Vastus sisaldab kõiki `language` tabeli kirjeid (hetkel `et` ja `en`)
- [ ] Tulemus on sorteeritud `language.id` järgi kasvavalt
- [ ] `languageName` väli ei sõltu ühestki keele/tõlke parameetrist (teenusel pole `contentLang` parameetrit)
- [ ] `is_main_language` väli vastusesse ei jõua
- [ ] Kirjeldatud veaolukord (500 ootamatu vea korral) on käsitletud
- [ ] Teenusel on automaattestid

## Avatud küsimused

1. **DTO ja abiklassid on juba olemas.** `SystemLanguageDto` (`controller/common/dto/SystemLanguageDto.java`), `LanguageMapper` ja `LanguageRepository` (`persistance/language/`) on juba loodud ja neid kasutab hetkel `LoginService` (admini sisselogimisel `LoginResponse.systemLanguages` täitmiseks). Endpointi `GET /api/languages` ennast (controller ega eraldi service-meetod) koodis veel ei ole — see task kirjeldab uut, olemasolevaid klasse taaskasutavat endpointi.
2. **Vastuse järjekord — otsustatud:** `language.id` järgi kasvavalt (lühikeste valikunimekirjade ühtne reegel). NB! `LoginService` kasutab `findAll()` ilma sorteerimiseta — selle teenuse jaoks tuleb järjekord määrata eksplitsiitselt (nt `findAll(Sort.by("id"))`).
3. **`training-form-view-state-new-training-markmed.md` ja `trainings-view-markmed.md` API märkmed langevad kokku** — sama `SystemLanguageDto` kuju, sama kirjeldus ("languageName ei ole tõlgitud"), sama `Veateated: —`. Lahknevust pole, seega täiendavat otsust polnud vaja teha.
4. **Vene keel (`ru`)** esineb mockupi wireframe'i teistes kohtades (nt `contentLang` valikuna teistel teenustel), kuid andmebaasi `language` tabelis on ainult `et` ja `en`. Kuna see teenus tagastab otse `language` tabeli sisu, ei ole `ru` vastuses — kasutaja otsusel pole seda ka näidisandmetesse lisatud.
