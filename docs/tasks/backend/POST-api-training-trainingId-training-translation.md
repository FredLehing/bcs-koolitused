# Koolituse uue keele tõlke lisamine

**Teenus:** `POST /api/training/{trainingId}/training-translation`

**Kasutav vaade:** `TrainingFormView.vue` (`/training-form?trainingId={id}&languageId={id}`, `state: "new-translation"`)

![Mockup](../../mock-wireframe/pdf-images/TrainingFormView-state-new-translation.png)

## Sisend

**Path variable:**

| Nimi | Tüüp | Kirjeldus |
|---|---|---|
| `trainingId` | Integer | Koolitus, millele tõlge lisatakse (`training.id`) |

Query parameetreid ei ole. Sisend tuleb request body's.

**Request body:** `TrainingTranslationCreateRequestDto.java`

| Väli | Tüüp | Kohustuslik | Kirjeldus |
|---|---|---|---|
| `languageId` | Integer | jah | Sihtkeel, millesse tõlge lisatakse (`training_translation.language_id`) |
| `title` | String (max 255) | jah | Pealkiri sihtkeeles |
| `shortDescription` | String (max 255) | jah | Lühikirjeldus sihtkeeles |
| `description` | String (richtext / HTML) | jah | Kirjeldus sihtkeeles |

Koolituse `trainingId` tuleb path'ist, mitte request body'st. Ühes keeles saab koolitusel olla ainult üks tõlge (`training_translation_uq` — `(training_id, language_id)`); kui sihtkeeles on tõlge juba olemas, lisamist ei toimu (vt "Veaolukorrad").

```json
{
  "languageId": 2,
  "title": "Power BI for Advanced Users",
  "shortDescription": "Data models, DAX and interactive reports.",
  "description": "The course builds a data model in Power BI, writes DAX formulas and creates interactive reports."
}
```

Näidis eeldab, et koolitus 3 ("Power BI edasijõudnutele") on juba loodud (`POST /api/training`, vt `docs/tasks/backend/POST-api-training.md`) koos põhikeele (et) tõlkega — ja sellele lisatakse nüüd inglise keele (languageId 2) tõlge. Tekst on mockupi lk 8 põhjal (sama koolituse ingliskeelne versioon). Vene keelt (mockupis kolmanda keelena mainitud) näidises ei kasutata, kuna `language` tabelis on `3_import.sql` järgi ainult "et" ja "en".

## Väljund

**Response (200 OK):** `TrainingTranslationCreateResponseDto.java` — loodud tõlke ID.

```json
{
  "trainingTranslationId": 6
}
```

`trainingTranslationId` on näitlik: värskelt imporditud andmebaasis on `training_translation` max id 4; kui enne seda on koolitus 3 loodud koos põhikeele tõlkega (id 5, vt `POST /api/training` task), on selle teenuse loodud rea id 6.

Frontend teeb vastuse põhjal `router.replace` aadressile `/training-form?trainingId={trainingId}&trainingTranslationId={trainingTranslationId}` (vaade liigub olekusse `state: "update"`) ja laadib andmed uuesti.

## Eesmärk

Admin lisab `TrainingFormView` vaates (`state: "new-translation"`) koolitusele tõlke uude keelde, mida veel pole. Koolituse põhiandmed (kategooria, õppekeel, toimumiskoht jne) on vaates kirjutuskaitstud — ainult teksti väljad (pealkiri, lühikirjeldus, kirjeldus) täidetakse sihtkeeles, vajadusel "Tee AI tõlge" abiga (`GET /api/training/{trainingId}/ai-translation`, eraldi task). Nupule "Lisa tõlge" vajutades luuakse üks uus `training_translation` rida. Koolituse muid andmeid ega staatust see teenus ei muuda.

## Seotud andmebaasi tabelid

Vt `docs/database/2_create.sql`.

### training_translation

Koolituse tõlgitud tekstid. Selle teenusega luuakse üks uus rida.

```sql
CREATE TABLE training_translation
(
    id                serial       NOT NULL,
    training_id       int          NOT NULL,
    language_id       int          NOT NULL,
    title             varchar(255) NOT NULL,
    short_description varchar(255) NOT NULL,
    description       text         NOT NULL,
    created_at        timestamp    NOT NULL,
    updated_at        timestamp    NOT NULL,
    CONSTRAINT training_translation_pk PRIMARY KEY (id),
    CONSTRAINT training_translation_uq UNIQUE (training_id, language_id)
);
```

`created_at` ja `updated_at` määrab backend (hetke aeg). `training_translation_uq` tagab, et ühel koolitusel saab ühes keeles olla ainult üks tõlge — sellel põhineb allpool kirjeldatud 403 veaolukord.

Näidisandmed: ID-d 1–4 (koolitustel 1 ja 2 on mõlemal juba et ja en tõlge, seega uut tõlget neile selle näitega lisada ei saa — näidises kasutatakse koolitust 3).

### training

Ainult ID olemasolu kontrolliks (`trainingId` path variable) — rida ise ei muutu.

```sql
CREATE TABLE training
(
    id                   serial    NOT NULL,
    user_id              int       NOT NULL,
    default_lecturer_id  int       NULL,
    category_id          int       NOT NULL,
    training_language_id int       NOT NULL,
    location_id          int       NOT NULL,
    status               varchar(1) NOT NULL,
    created_at           timestamp NOT NULL,
    updated_at           timestamp NOT NULL,
    is_orderable         boolean   NOT NULL,
    is_promoted           boolean   NOT NULL,
    CONSTRAINT course_pk PRIMARY KEY (id)
);
```

### language

Ainult ID olemasolu kontrolliks (`languageId` request body's) — rida ise ei muutu. Näidisandmed: `(1, 'et', 'Eesti', true)`, `(2, 'en', 'English', false)`.

## Veaolukorrad

| Olukord | Status code | Response body |
|---|---|---|
| `trainingId` ei leidu andmebaasist | 404 Not Found | `{ "message": "Ei leidnud primary keyd 'trainingId' väärtusega: 123", "errorCode": "PRIMARY_KEY_NOT_FOUND" }` |
| `languageId` ei leidu andmebaasist | 404 Not Found | `{ "message": "Ei leidnud primary keyd 'languageId' väärtusega: 123", "errorCode": "PRIMARY_KEY_NOT_FOUND" }` |
| Koolitusel on `languageId` keeles tõlge juba olemas (`training_translation_uq`) | 403 Forbidden | `{ "message": "Selles keeles tõlge on juba olemas", "errorCode": "TRANSLATION_EXISTS" }` |
| Kohustuslik väli puudub või on liiga pikk (`@Valid`) | 400 Bad Request | `{ "message": "<väli>: <valideerimise teade>", "errorCode": "INCORRECT_INPUT" }` |
| Ootamatu serveripoolne viga | 500 Internal Server Error | Standardne vea response body (vastavalt projekti globaalsele error handler'ile) |

Kolm esimest rida on mockupi märkmetest (`Veateated`). `INCORRECT_INPUT` rida tuleneb projekti mustrist (`RestExceptionHandler.handleMethodArgumentNotValid`, vt `docs/tasks/backend/POST-api-training.md`).

`PRIMARY_KEY_NOT_FOUND` read vastavad mustrile `PrimaryKeyNotFoundException` + `getValid<Entiteet>By(Integer id)` (vt `backend/CLAUDE.md`, "Entiteedi otsing ID järgi").

`TRANSLATION_EXISTS` errorCode **puudub veel** `Error.java` enumis (praegu on seal ainult `INCORRECT_CREDENTIALS`) — see tuleb lisada. Erind visatakse `ForbiddenException`-ina (`errorCode`, `message` konstruktoriparameetritena) — `RestExceptionHandler.handleForbiddenException` mapib selle juba 403 vastuseks, uut handler-meetodit vaja ei ole.

## Vastuvõtu kriteeriumid

- [ ] Endpoint `POST /api/training/{trainingId}/training-translation` on olemas ja võtab vastu `TrainingTranslationCreateRequestDto`
- [ ] Õnnestunud päring tagastab 200 OK ja `TrainingTranslationCreateResponseDto` (`trainingTranslationId`)
- [ ] Luuakse üks `training_translation` rida: `training_id` path'ist, ülejäänud väljad request body'st, `created_at` / `updated_at` = hetke aeg
- [ ] Koolituse (`training`) rida ega staatust ei muudeta
- [ ] Olematu `trainingId` → 404 `PRIMARY_KEY_NOT_FOUND`
- [ ] Olematu `languageId` → 404 `PRIMARY_KEY_NOT_FOUND`
- [ ] Kui koolitusel on `languageId` keeles tõlge juba olemas → 403 `TRANSLATION_EXISTS`, sõnum "Selles keeles tõlge on juba olemas"; uut rida ei looda
- [ ] Puuduv kohustuslik väli või liiga pikk `title` / `shortDescription` → 400 `INCORRECT_INPUT`
- [ ] `Error` enumisse on lisatud `TRANSLATION_EXISTS("Selles keeles tõlge on juba olemas")`
- [ ] Teenusel on automaattestid

## Avatud küsimused

1. **Dubleeriva tõlke kontroll — kas eraldusseisev `existsBy` päring või unique constraint'i (`training_translation_uq`) rikkumise püüdmine?** Mockup ja see task eeldavad, et backend kontrollib olemasolu eksplitsiitselt enne salvestamist (nt `TrainingTranslationRepository.existsByTrainingIdAndLanguageId(...)`) ja viskab `ForbiddenException`, mitte ei lase andmebaasi unique constraint'il 500 viga anda. See on kooskõlas projekti mustriga (`training_funding_type_uq` duplikaatide eemaldamine `POST /api/training` taskis), aga täpne kontrollimeetod jäägu implementeerijale.
2. **Kas `languageId` peab erinema koolituse põhikeelest?** Mockup ja märkmed ei täpsusta eraldi keeldu põhikeelde tõlke lisamiseks selle teenuse kaudu (erinevalt `GET /api/training/{trainingId}/ai-translation` teenusest, kus on eraldi `MAIN_LANGUAGE_NOT_TRANSLATABLE` viga). Kuna põhikeele tõlge luuakse juba `POST /api/training` käigus, väldiks `training_translation_uq` duplikaadi teket niikuinii — täiendavat äriloogika piirangut pole vaja lisada, `TRANSLATION_EXISTS` katab selle olukorra piisavalt.
3. **Kas `TRANSLATION_EXISTS` kuulub `ForbiddenException` alla (403) või oleks sobivam uus 409 Conflict tüüpi erind?** Kasutaja kinnitas 403 `ForbiddenException`, mis on olemasolev muster (vt `infrastructure/exception/ForbiddenException.java`) — 409 jaoks pole projektis veel infrastruktuuri, seda pole vaja luua.
