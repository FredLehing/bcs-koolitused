# Koolituse ja selle tõlke muutmine

**Teenus:** `PUT /api/training/{trainingId}`

**Kasutav vaade:** `TrainingFormView.vue` (`/training-form?trainingId={id}&trainingTranslationId={id}`, `state: "update"`, nupp "Salvesta")

![Mockup](../../mock-wireframe/pdf-images/TrainingFormView-state-update.png)

## Sisend

**Path variable:**

| Nimi | Tüüp | Kirjeldus |
|---|---|---|
| `trainingId` | Integer | Muudetava koolituse ID (`training.id`) |

Query parameetrid puuduvad.

**Request body:** `TrainingUpdateRequestDto.java`

| Väli | Tüüp | Kohustuslik | Kirjeldus |
|---|---|---|---|
| `categoryId` | Integer | jah | Kategooria (`training.category_id`) |
| `trainingLanguageId` | Integer | jah | Koolituse **õppekeel** (`training.training_language_id`) — ei ole tõlke keel |
| `locationId` | Integer | jah | Toimumiskoht (`training.location_id`) |
| `defaultLecturerId` | Integer | ei (`null`) | Vaikimisi lektor (`training.default_lecturer_id`); `null` eemaldab lektori |
| `isOrderable` | Boolean | jah | "Tellitav" switch (`training.is_orderable`) |
| `isPromoted` | Boolean | jah | "Esile tõstetud" switch (`training.is_promoted`) |
| `fundingTypeIds` | List&lt;Integer&gt; | jah (võib olla tühi `[]`) | Valitud rahastustüübid (checkboxid); `training_funding_type` read kirjutatakse selle järgi üle; duplikaadid eemaldatakse enne salvestamist |
| `trainingTranslationId` | Integer | jah | Vormis avatud tõlke ID (`training_translation.id`) — peab kuuluma sellele koolitusele |
| `title` | String (max 255) | jah | Avatud tõlke pealkiri |
| `shortDescription` | String (max 255) | jah | Avatud tõlke lühikirjeldus |
| `description` | String (richtext / HTML) | jah | Avatud tõlke kirjeldus |

Mida request body's **ei ole** (ja mida see teenus ei muuda):

- `userId` — koolituse autorit (`training.user_id`) ei muudeta.
- `status` — staatust muudavad ainult eraldi teenused `PUT /api/training/{trainingId}/publish` ja `/unpublish`.
- tõlke keel (`language_id`) — muudetakse ainult olemasoleva tõlke tekste; tõlke keelt vahetada ei saa.

```json
{
  "categoryId": 1,
  "trainingLanguageId": 1,
  "locationId": 1,
  "defaultLecturerId": 1,
  "isOrderable": true,
  "isPromoted": true,
  "fundingTypeIds": [1, 2],
  "trainingTranslationId": 1,
  "title": "Java algkursus",
  "shortDescription": "Java programmeerimise alused algajatele.",
  "description": "Kursusel õpitakse Java süntaksit, objektorienteeritud programmeerimist ja põhilisi andmestruktuure."
}
```

Näidis põhineb mockupi märkmel ja `3_import.sql` andmetel: koolitus 1 ("Java algkursus"), selle eestikeelne tõlge 1, kategooria 1 "Programmeerimine", keel 1 "Eesti", asukoht 1 "BCS Koolitus", lektor 1 "Mari Tamm". Andmebaasis on koolitusel 1 ainult rahastustüüp 1 "Töötukassa" — näidises lisatakse juurde rahastustüüp 2 "EL rahastus" (mockupi `[1, ...]` täpsustatud), et oleks näha `training_funding_type` ridade ülekirjutamine.

Frontend koostab body meetodis `TrainingFormView.vue` → `createTrainingUpdateRequest()` (samad 11 välja) ja kutsub `TrainingService.sendPutTrainingRequest(trainingId, ...)`. Mock-teostus: `frontend/src/api-services/mock/MockDatabase.js` → `updateTraining` (kasutab samuti `new Set(fundingTypeIds)` duplikaatide eemaldamiseks).

## Väljund

**Response (200 OK):** Tühi vastus (ainult staatuskood 200 OK).

Mockupi märkmes on `Response (200): NONE`. Frontend kuvab õnnestumisel teate "Muudatused on salvestatud" ja jätab vormi samasse olekusse (`state: "update"`); URL ei muutu.

## Eesmärk

Admin muudab `TrainingFormView` vaates olemasolevat koolitust (`state: "update"`). Vorm on eeltäidetud koolituse andmete (kategooria, õppekeel, toimumiskoht, vaikimisi lektor, rahastustüübid, "Tellitav" / "Esile tõstetud") ja URL-is oleva tõlke (`trainingTranslationId`) tekstidega. Nupule "Salvesta" vajutades salvestatakse koolituse väljad ja **avatud tõlke** tekstid ühe päringuga ja ühes transaktsioonis — admin võib olla avanud nii põhikeele kui mõne muu keele tõlke. Koolituse staatus (mustand / publitseeritud) jääb samaks. Teenus on `POST /api/training` "muutmise" paariline.

## Seotud andmebaasi tabelid

Vt `docs/database/2_create.sql`.

Kõiki kolme tabelit muudetakse **ühes transaktsioonis** (`@Transactional`) — kui mõni samm (nt olematu `fundingTypeId`) ebaõnnestub, ei jää andmebaasi poolikut muudatust (nt koolitus uuendatud, aga tõlge mitte).

### training

Uuendatakse väljad `category_id`, `training_language_id`, `location_id`, `default_lecturer_id`, `is_orderable`, `is_promoted` ja `updated_at` (hetke aeg). **Ei muudeta:** `id`, `user_id`, `status`, `created_at`.

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

`status` väärtused: `"U"` = mustand (unpublished), `"P"` = publitseeritud. Kui koodis on staatust vaja (nt testis kontrollimaks, et see ei muutunud), kasuta eraldi `TrainingStatus` enumit (`UNPUBLISHED("U")`, `PUBLISHED("P")`) — **mitte** olemasolevat `ApiStatus` enumit, kus `"D"` tähendab kustutatud.

Näidisandmed (`3_import.sql`):

| id | user_id | default_lecturer_id | category_id | training_language_id | location_id | status | is_orderable | is_promoted |
|---|---|---|---|---|---|---|---|---|
| 1 | 1 | 1 | 1 | 1 | 1 | P | true | true |
| 2 | 1 | 2 | 3 | 1 | 2 | P | false | false |

### training_translation

Uuendatakse `trainingTranslationId` rea väljad `title`, `short_description`, `description` ja `updated_at` (hetke aeg). **Ei muudeta:** `training_id`, `language_id`, `created_at`. Teiste keelte tõlkeid see teenus ei puuduta.

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

Näidisandmed (`3_import.sql`):

| id | training_id | language_id | title |
|---|---|---|---|
| 1 | 1 | 1 (et) | Java algkursus |
| 2 | 1 | 2 (en) | Java Basics |
| 3 | 2 | 1 (et) | Projektijuhtimise põhitõed |
| 4 | 2 | 2 (en) | Project Management Fundamentals |

Näiteks `PUT /api/training/1` koos `trainingTranslationId: 3` on viga — tõlge 3 kuulub koolitusele 2 (vt "Veaolukorrad").

### training_funding_type

Koolituse ja rahastustüübi many-to-many liitetabel. Koolituse read kirjutatakse `fundingTypeIds` järgi **üle**: pärast salvestamist on koolitusel täpselt need rahastustüübid, mis request body's (duplikaadid eemaldatud). Tühi list `[]` eemaldab kõik koolituse rahastustüübid. Lihtsaim viis: kustuta koolituse vanad read ja lisa uued.

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

Näidisandmed: `(1, 1, 1)` — koolitusel 1 on rahastustüüp 1 "Töötukassa". Rahastustüübid (`funding_type`): 1 `JOB_CENTRE` "Töötukassa", 2 `EU_FUNDED` "EL rahastus". Näidis-request'i järel on koolitusel 1 read `(training_id=1, funding_type_id=1)` ja `(training_id=1, funding_type_id=2)`.

Tähelepanu: kui vanad read kustutatakse ja samad `(training_id, funding_type_id)` paarid lisatakse samas transaktsioonis uuesti, peab kustutamine jõudma andmebaasi enne lisamist (nt `deleteAll` + `flush()` või kustutamine `@Modifying` päringuga), muidu võib `training_funding_type_uq` anda vea.

Tabelid `category`, `language`, `location`, `lecturer` ja `funding_type` on siin ainult ID-de olemasolu kontrolliks — neid ei muudeta.

## Veaolukorrad

| Olukord | Status code | Response body |
|---|---|---|
| `trainingId` (path) ei leidu andmebaasist | 404 Not Found | `{ "message": "Ei leidnud primary keyd 'trainingId' väärtusega: 123", "errorCode": "PRIMARY_KEY_NOT_FOUND" }` |
| `trainingTranslationId` ei leidu andmebaasist | 404 Not Found | `{ "message": "Ei leidnud primary keyd 'trainingTranslationId' väärtusega: 123", "errorCode": "PRIMARY_KEY_NOT_FOUND" }` |
| `trainingTranslationId` on olemas, aga kuulub teisele koolitusele | 404 Not Found | `{ "message": "Ei leidnud primary keyd 'trainingTranslationId' väärtusega: 3", "errorCode": "PRIMARY_KEY_NOT_FOUND" }` |
| `categoryId` ei leidu andmebaasist | 404 Not Found | `{ "message": "Ei leidnud primary keyd 'categoryId' väärtusega: 123", "errorCode": "PRIMARY_KEY_NOT_FOUND" }` |
| `trainingLanguageId` ei leidu andmebaasist | 404 Not Found | `{ "message": "Ei leidnud primary keyd 'trainingLanguageId' väärtusega: 123", "errorCode": "PRIMARY_KEY_NOT_FOUND" }` |
| `locationId` ei leidu andmebaasist | 404 Not Found | `{ "message": "Ei leidnud primary keyd 'locationId' väärtusega: 123", "errorCode": "PRIMARY_KEY_NOT_FOUND" }` |
| `defaultLecturerId` on antud, aga ei leidu andmebaasist | 404 Not Found | `{ "message": "Ei leidnud primary keyd 'defaultLecturerId' väärtusega: 123", "errorCode": "PRIMARY_KEY_NOT_FOUND" }` |
| Mõni `fundingTypeIds` element ei leidu andmebaasist | 404 Not Found | `{ "message": "Ei leidnud primary keyd 'fundingTypeId' väärtusega: 123", "errorCode": "PRIMARY_KEY_NOT_FOUND" }` |
| Kohustuslik väli puudub või on liiga pikk (`@Valid`) | 400 Bad Request | `{ "message": "<väli>: <valideerimise teade>", "errorCode": "INCORRECT_INPUT" }` |
| Ootamatu serveripoolne viga | 500 Internal Server Error | Standardne vea response body (vastavalt projekti globaalsele error handler'ile) |

Allikad:

- **Mockupi märkmetest** (`Veateated`): kaks esimest rida — `trainingId` ja `trainingTranslationId` 404.
- **Projekti mustrist** (`PrimaryKeyNotFoundException` + `getValid<Entiteet>By(Integer id)` meetod vastava service klassi all, vt `backend/CLAUDE.md`; sama nagu `POST-api-training.md` taskis): `categoryId`, `trainingLanguageId`, `locationId`, `defaultLecturerId`, `fundingTypeId` 404 read.
- **Otsus selle taski jaoks** (vt "Avatud küsimused" p 1): teisele koolitusele kuuluv `trainingTranslationId` → sama 404 `PRIMARY_KEY_NOT_FOUND` `'trainingTranslationId'` sõnumiga.
- 400 rida tuleneb `RestExceptionHandler.handleMethodArgumentNotValid`-ist (`errorCode: INCORRECT_INPUT`, sõnum on esimese vigase välja nimi + valideerimise teade).

## Vastuvõtu kriteeriumid

- [ ] Endpoint `PUT /api/training/{trainingId}` on olemas ja võtab vastu `TrainingUpdateRequestDto`
- [ ] Õnnestunud päring tagastab 200 OK tühja vastusega
- [ ] `training` real uuenevad `category_id`, `training_language_id`, `location_id`, `default_lecturer_id`, `is_orderable`, `is_promoted` ja `updated_at` (hetke aeg)
- [ ] `training.user_id`, `training.status` ja `training.created_at` ei muutu (nt mustand jääb mustandiks, publitseeritud jääb publitseerituks)
- [ ] `trainingTranslationId` real uuenevad `title`, `short_description`, `description` ja `updated_at`; `language_id`, `training_id` ja `created_at` ei muutu
- [ ] Koolituse teiste keelte tõlked ei muutu
- [ ] Pärast salvestamist on koolitusel täpselt need `training_funding_type` read, mis `fundingTypeIds`-is; tühi list eemaldab kõik; duplikaadid (nt `[1, 1]`) eemaldatakse, viga ei teki
- [ ] Samade rahastustüüpide uuesti saatmine (nt `[1]` koolitusele, millel juba on 1) ei anna `training_funding_type_uq` viga
- [ ] `defaultLecturerId = null` on lubatud ja salvestub `NULL`-ina
- [ ] Kõik muudatused tehakse ühes transaktsioonis — vea korral (nt olematu `fundingTypeId`) ei muutu andmebaasis midagi
- [ ] Olematu `trainingId` / `trainingTranslationId` / `categoryId` / `trainingLanguageId` / `locationId` / `defaultLecturerId` / `fundingTypeId` → 404 `PRIMARY_KEY_NOT_FOUND` õige väljanimega
- [ ] Teisele koolitusele kuuluv `trainingTranslationId` (nt `PUT /api/training/1` + `trainingTranslationId: 3`) → 404 `PRIMARY_KEY_NOT_FOUND` `'trainingTranslationId'`; midagi ei salvestata
- [ ] Puuduv kohustuslik väli või liiga pikk `title` / `shortDescription` → 400 `INCORRECT_INPUT`
- [ ] Koolituse staatust puudutav kood kasutab `TrainingStatus` enumit, mitte `ApiStatus`-t
- [ ] Teenusel on automaattestid

## Avatud küsimused

1. **Teisele koolitusele kuuluv `trainingTranslationId` — valitud 404 `PRIMARY_KEY_NOT_FOUND`.** Mockup seda juhtumit eraldi ei kirjelda. Valitud on 404 sama sõnumiga nagu olematu ID puhul (`Ei leidnud primary keyd 'trainingTranslationId' väärtusega: 3`), sest selle koolituse kontekstis sellist tõlget ei ole; see kasutab juba olemasolevat `PrimaryKeyNotFoundException`-it ja mockupis juba kirjeldatud veateadet ning uut `errorCode`-i ega `Error` enumi kirjet pole vaja. Tõlke saab leida nt `findByIdAndTrainingId(...)` abil. Alternatiiv oleks `ForbiddenException` (403) eraldi koodiga. **Otsustatud: 404 `PRIMARY_KEY_NOT_FOUND`.**
2. **Kontrollide järjekord.** Kui mitu ID-d on korraga vigased, tagastatakse esimene leitud viga. Soovituslik järjekord: `trainingId` → `trainingTranslationId` → `categoryId` → `trainingLanguageId` → `locationId` → `defaultLecturerId` → `fundingTypeIds`. Testid ei tohiks sõltuda muust kui ühe vea korraga.
3. **Samaaegne muutmine.** Optimistlikku lukustamist (versioon / `updated_at` võrdlus) ei ole — kui kaks admini salvestavad sama koolitust, jääb kehtima viimane. Praegu seda ei nõuta.
4. **`updated_at` muutmata andmete korral.** Kui request body sisu on sama mis andmebaasis, uuendatakse `updated_at` siiski (lihtsam loogika). Kinnita, et see sobib.
5. **`training_funding_type` ülekirjutamise viis — otsustatud:** kustuta kõik koolituse read + lisa uued (lihtsaim). Kustutamise järel tee `flush()` enne lisamist, muidu võib `training_funding_type_uq` anda vea.
6. **Tühi `title` / `shortDescription` / `description` — lahendatud.** Tühi string ei ole lubatud (`@NotBlank`), nagu `POST /api/training` puhul. `description` on HTML: `@HtmlNotBlank` annab 400 `INCORRECT_INPUT` ka `<p></p>` korral ja sisu puhastatakse enne salvestamist `HtmlSanitizer`-iga (vt `training-description-html-sanitize.md`).
7. **`Error` enum.** Valitud lahenduse (p 1: 404) korral uusi äriveateateid pole vaja.
