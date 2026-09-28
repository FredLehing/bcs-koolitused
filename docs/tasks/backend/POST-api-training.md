# Uue koolituse lisamine

**Teenus:** `POST /api/training`

**Kasutav vaade:** `TrainingFormView.vue` (`/training-form`, `state: "new-training"`)

![Mockup](../../mock-wireframe/pdf-images/TrainingFormView-state-new-training.png)

## Sisend

Teenusel puuduvad path variable'id ja query parameetrid. Sisend tuleb request body's.

**Request body:** `TrainingCreateRequestDto.java`

| Väli | Tüüp | Kohustuslik | Kirjeldus |
|---|---|---|---|
| `userId` | Integer | jah | Koolituse lisanud admin (`training.user_id`); frontend võtab selle localStorage'ist |
| `categoryId` | Integer | jah | Kategooria (`training.category_id`) |
| `trainingLanguageId` | Integer | jah | Koolituse **õppekeel** (`training.training_language_id`) — ei ole tõlke keel |
| `locationId` | Integer | jah | Toimumiskoht (`training.location_id`) |
| `defaultLecturerId` | Integer | ei (`null`) | Vaikimisi lektor (`training.default_lecturer_id`); "Vali lektor" modalist, võib jääda tühjaks |
| `isOrderable` | Boolean | jah | "Tellitav" switch (`training.is_orderable`) |
| `isPromoted` | Boolean | jah | "Esile tõstetud" switch (`training.is_promoted`) |
| `fundingTypeIds` | List&lt;Integer&gt; | jah (võib olla tühi `[]`) | Valitud rahastustüübid (checkboxid) → `training_funding_type` read; duplikaadid eemaldatakse enne salvestamist |
| `title` | String (max 255) | jah | Pealkiri **põhikeeles** |
| `shortDescription` | String (max 255) | jah | Lühikirjeldus põhikeeles |
| `description` | String (richtext / HTML) | jah | Kirjeldus põhikeeles |

`title`, `shortDescription` ja `description` salvestatakse **põhikeele** tõlkena (`language.is_main_language = true`, praegu `et`). Keelt request body's ei saadeta — backend leiab põhikeele ise, `language_id = 1` ei tohi koodis olla kõvasti kirjas.

`status` väärtust request body's ei ole — backend määrab uuele koolitusele alati `"U"` (unpublished / mustand).

```json
{
  "userId": 1,
  "categoryId": 1,
  "trainingLanguageId": 1,
  "locationId": 2,
  "defaultLecturerId": 1,
  "isOrderable": true,
  "isPromoted": false,
  "fundingTypeIds": [1, 2],
  "title": "Power BI edasijõudnutele",
  "shortDescription": "Andmemudelid, DAX ja interaktiivsed aruanded.",
  "description": "Kursusel ehitatakse Power BI-s andmemudel, kirjutatakse DAX-valemeid ja luuakse interaktiivseid aruandeid."
}
```

Näidise ID-d vastavad `3_import.sql` andmetele: kasutaja 1 (`admin@vali-it.ee`, roll admin), kategooria 1 "Programmeerimine", keel 1 "Eesti", asukoht 2 "Veebiõpe", lektor 1 "Mari Tamm", rahastustüübid 1 "Töötukassa" ja 2 "EL rahastus". Koolituse tekst on mockupi lk 7 põhjal uus näide.

## Väljund

**Response (200 OK):** `TrainingCreateResponseDto.java` — loodud koolituse ja selle põhikeele tõlke ID-d.

```json
{
  "trainingId": 3,
  "trainingTranslationId": 5
}
```

Näidise ID-d on need, mis tekivad esimese lisamise järel värskelt imporditud andmebaasis (`training` max id = 2, `training_translation` max id = 4).

Väljade selgitused:

- `trainingId` — uue `training` rea ID.
- `trainingTranslationId` — uue põhikeele `training_translation` rea ID.

Frontend teeb vastuse põhjal `router.replace` aadressile `/training-form?trainingId=3&trainingTranslationId=5` (vaade liigub olekusse `state: "update"`) ja laadib andmed uuesti.

## Eesmärk

Admin lisab `TrainingFormView` vaates uue koolituse. Vormis on koolituse andmed (kategooria, õppekeel, toimumiskoht, vaikimisi lektor, rahastustüübid, "Tellitav" / "Esile tõstetud") ja koolituse tekstid põhikeeles. Nupule "Lisa" vajutades luuakse koolitus koos esimese (põhikeele) tõlkega, et koolitusel oleks algusest peale pealkiri. Uus koolitus on mustand (`status = "U"`) ja muutub avalikuks alles "Publitseeri" nupuga (eraldi teenus `PUT /api/training/{trainingId}/publish`). Teised tõlked lisatakse hiljem ükshaaval (`POST /api/training/{trainingId}/training-translation`).

## Seotud andmebaasi tabelid

Vt `docs/database/2_create.sql`.

Kõik kolm tabelit täidetakse **ühes transaktsioonis** (`@Transactional`) — kui mõni samm ebaõnnestub, ei jää andmebaasi poolikut koolitust.

### training

Koolituse põhikirje. `created_at` ja `updated_at` määrab backend (hetke aeg), `status` = `"U"`.

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
    is_promoted          boolean   NOT NULL,
    CONSTRAINT course_pk PRIMARY KEY (id)
);
```

Välisvõtmed: `user_id` → `"user"`, `category_id` → `category`, `training_language_id` → `language`, `location_id` → `location`, `default_lecturer_id` → `lecturer`.

`status` väärtused: `"U"` = mustand (unpublished), `"P"` = publitseeritud. Kasuta eraldi `TrainingStatus` enumit (`UNPUBLISHED("U")`, `PUBLISHED("P")`) — **mitte** olemasolevat `ApiStatus` enumit, kus `"D"` tähendab kustutatud.

Näidisandmed: koolitused 1 ("Java algkursus") ja 2 ("Projektijuhtimise põhitõed"), mõlemad `status = 'P'`.

### training_translation

Koolituse tõlgitud tekstid. Selle teenusega luuakse üks rida põhikeeles.

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

Näidisandmed: ID-d 1–4 (koolitustel 1 ja 2 on et ja en tõlge).

### training_funding_type

Koolituse ja rahastustüübi many-to-many liitetabel. Iga `fundingTypeIds` elemendi kohta üks rida; tühja listi korral ridu ei looda.

```sql
CREATE TABLE training_funding_type
(
    id              serial NOT NULL,
    training_id     int    NOT NULL,
    funding_type_id int    NOT NULL,
    CONSTRAINT training_funding_type_pk PRIMARY KEY (id),
    CONSTRAINT training_funding_type_uq UNIQUE (training_id, funding_type_id)
);
```

Näidisandmed: `(1, 1, 1)` — koolitusel 1 on rahastustüüp 1.

### language

Põhikeele leidmiseks: rida, kus `is_main_language = true` (unikaalne osaindeks lubab ainult ühte). Näidisandmetes `(1, 'et', 'Eesti', true)`, `(2, 'en', 'English', false)`.

Tabelid `category`, `location`, `lecturer`, `funding_type` ja `"user"` on siin ainult ID-de olemasolu kontrolliks — neid ei muudeta.

## Veaolukorrad

| Olukord | Status code | Response body |
|---|---|---|
| `categoryId` ei leidu andmebaasist | 404 Not Found | `{ "message": "Ei leidnud primary keyd 'categoryId' väärtusega: 123", "errorCode": "PRIMARY_KEY_NOT_FOUND" }` |
| `locationId` ei leidu andmebaasist | 404 Not Found | `{ "message": "Ei leidnud primary keyd 'locationId' väärtusega: 123", "errorCode": "PRIMARY_KEY_NOT_FOUND" }` |
| `trainingLanguageId` ei leidu andmebaasist | 404 Not Found | `{ "message": "Ei leidnud primary keyd 'trainingLanguageId' väärtusega: 123", "errorCode": "PRIMARY_KEY_NOT_FOUND" }` |
| `defaultLecturerId` on antud, aga ei leidu andmebaasist | 404 Not Found | `{ "message": "Ei leidnud primary keyd 'defaultLecturerId' väärtusega: 123", "errorCode": "PRIMARY_KEY_NOT_FOUND" }` |
| Mõni `fundingTypeIds` element ei leidu andmebaasist | 404 Not Found | `{ "message": "Ei leidnud primary keyd 'fundingTypeId' väärtusega: 123", "errorCode": "PRIMARY_KEY_NOT_FOUND" }` |
| `userId` ei leidu andmebaasist | 404 Not Found | `{ "message": "Ei leidnud primary keyd 'userId' väärtusega: 123", "errorCode": "PRIMARY_KEY_NOT_FOUND" }` |
| Kohustuslik väli puudub või on liiga pikk (`@Valid`) | 400 Bad Request | `{ "message": "<väli>: <valideerimise teade>", "errorCode": "INCORRECT_INPUT" }` |
| Ootamatu serveripoolne viga | 500 Internal Server Error | Standardne vea response body (vastavalt projekti globaalsele error handler'ile) |

Kaks esimest rida on mockupi märkmetest (`Veateated`). Ülejäänud 404 read tulenevad sama märkme lisainfost ("Sama PRIMARY_KEY_NOT_FOUND muster kehtib ka trainingLanguageId, defaultLecturerId ja fundingTypeIds väljadele") ja projekti mustrist: `PrimaryKeyNotFoundException` + `getValid<Entiteet>By(Integer id)` meetod vastava service klassi all (vt `backend/CLAUDE.md`). 400 rida tuleneb `RestExceptionHandler.handleMethodArgumentNotValid`-ist (`errorCode: INCORRECT_INPUT`).

## Vastuvõtu kriteeriumid

- [ ] Endpoint `POST /api/training` on olemas ja võtab vastu `TrainingCreateRequestDto`
- [ ] Õnnestunud päring tagastab 200 OK ja `TrainingCreateResponseDto` (`trainingId`, `trainingTranslationId`)
- [ ] Luuakse `training` rida: väljad request body'st, `status = "U"`, `created_at` / `updated_at` = hetke aeg
- [ ] Luuakse `training_translation` rida põhikeeles (`language.is_main_language = true`); keele ID ei ole koodis kõvasti kirjas
- [ ] Iga `fundingTypeIds` elemendi kohta luuakse `training_funding_type` rida; tühi list ei loo ridu; duplikaadid (nt `[1, 1]`) eemaldatakse, viga ei teki
- [ ] `defaultLecturerId = null` on lubatud ja salvestub `NULL`-ina
- [ ] Kõik kolm tabelit salvestatakse ühes transaktsioonis — vea korral ei jää andmebaasi poolikuid ridu
- [ ] Olematu `categoryId` / `locationId` / `trainingLanguageId` / `defaultLecturerId` / `fundingTypeId` / `userId` → 404 `PRIMARY_KEY_NOT_FOUND` õige väljanimega
- [ ] Puuduv kohustuslik väli või liiga pikk `title` / `shortDescription` → 400 `INCORRECT_INPUT`
- [ ] Selle taski käigus luuakse `TrainingStatus` enum (`UNPUBLISHED("U")`, `PUBLISHED("P")`) baaspaketti; koolituse staatus kasutab seda, mitte `ApiStatus`-t
- [ ] Teenusel on automaattestid

## Avatud küsimused

1. **Andmebaasi jadad — lahendatud.** `3_import.sql` faili lisati puuduvad `setval` read (`funding_type`, `funding_type_translation`, `training_funding_type`). Enne testimist loo andmebaas uuesti.
2. **`fundingTypeIds` kuju.** Kasutatud on lihtsat ID-de listi (märkmete ettepanek). Alternatiiv oleks struktuuridokumendi näite muster `[{ fundingTypeId, isSelected }]` — praegu pole seda vaja, sest vorm saadab ainult valitud ID-d.
3. **Duplikaadid `fundingTypeIds`-is — otsustatud:** backend eemaldab duplikaadid enne salvestamist (muidu annaks `training_funding_type_uq` 500 vea).
4. **`userId` — otsustatud:** kontrollitakse ainult kasutaja olemasolu (404 `PRIMARY_KEY_NOT_FOUND`), admin-rolli ei kontrollita. Backendil pole autentimist, kasutaja ID tuleb frontendilt (localStorage).
5. **Kas põhikeele puudumine on võimalik?** Kui ühtegi `is_main_language = true` rida pole, on see andmete viga → 500. Eraldi veakoodi pole ette nähtud.
