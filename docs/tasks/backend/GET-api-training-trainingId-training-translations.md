# Koolituse tõlgete nimekirja päring

**Teenus:** `GET /api/training/{trainingId}/training-translations`

**Kasutav vaade:** `TrainingFormView.vue` (`/training-form`, `state: "update"` ja `state: "new-translation"`)

![Mockup](../../mock-wireframe/pdf-images/TrainingFormView-state-update.png)

## Sisend

**Path variable:**

| Väli | Tüüp | Kirjeldus |
|---|---|---|
| `trainingId` | Integer | Koolitus, mille tõlkeid päritakse (`training.id`) |

Query parameetreid teenusel pole.

## Väljund

**Response (200 OK):** `TrainingTranslationItemDto[]` — koolituse olemasolevate tõlgete nimekiri.

```json
[
  {
    "trainingTranslationId": 1,
    "languageId": 1,
    "languageCode": "et",
    "isMainLanguage": true
  },
  {
    "trainingTranslationId": 2,
    "languageId": 2,
    "languageCode": "en",
    "isMainLanguage": false
  }
]
```

Näidisandmed `3_import.sql`-st, koolitus `trainingId = 1` ("Java algkursus"): `training_translation` read id 1 (et) ja id 2 (en).

Väljade selgitused:

- `trainingTranslationId` — `training_translation.id`.
- `languageId` — `training_translation.language_id`.
- `languageCode` — vastava `language.code`.
- `isMainLanguage` — vastava `language.is_main_language`.

Tulemus on sorteeritud `language_id` järgi kasvavalt.

Massiivis on ainult read, mille kohta on olemasolev `training_translation` kirje — puuduva tõlke jaoks rida ei tagastata (frontend tuletab puuduvad keeled, võrreldes vastust store'i `contentLanguages` massiiviga).

## Eesmärk

`TrainingFormView` vaates (roll: Admin) kuvatakse iga koolituse keele kohta lipuke (`TranslationFlags.vue`): kui keele `languageCode` esineb selle teenuse vastuses, on lipuke värviline ja klõps avab olemasoleva tõlke (`state: "update"`, laetakse `GET /api/training-translation/{trainingTranslationId}` kaudu); kui ei esine, on lipuke hall ja klõps viib uue tõlke lisamise vormi (`state: "new-translation"`). `state: "new-translation"` vaates kasutatakse sama vastuse `isMainLanguage: true` elementi, et leida põhikeele (`et`) `trainingTranslationId` ja laadida sellega vorm eeltäidetuna tõlkimiseks.

## Seotud andmebaasi tabelid

Vt `docs/database/2_create.sql`.

### training_translation

Koolituse tõlgitud tekstid. Iga koolituse-keele kombinatsiooni kohta on ülimalt üks rida (unikaalsuspiirang).

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

Näidisandmed (`3_import.sql`): id 1 (`training_id=1, language_id=1`, et), id 2 (`training_id=1, language_id=2`, en), id 3 (`training_id=2, language_id=1`, et), id 4 (`training_id=2, language_id=2`, en).

### language

Süsteemi keeled; `is_main_language` märgib põhikeele (unikaalne osaindeks `language_main_language_uq` tagab, et tõsi on ainult ühel real).

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

Näidisandmed: `(1, 'et', 'Eesti', true)`, `(2, 'en', 'English', false)`. Andmebaasis on hetkel ainult need kaks keelt — mockupi wireframe'il nähtav venekeelne (`ru`) lipuke andmebaasis puudub.

### training

Ainult `trainingId` olemasolu kontrolliks (vt Veaolukorrad) — teenus ise koolituse välju ei tagasta.

## Veaolukorrad

| Olukord | Status code | Response body |
|---|---|---|
| `trainingId` ei leidu andmebaasist | 404 Not Found | `{ "message": "Ei leidnud primary keyd 'trainingId' väärtusega: 123", "errorCode": "PRIMARY_KEY_NOT_FOUND" }` |
| Ootamatu serveripoolne viga | 500 Internal Server Error | Standardne vea response body (vastavalt projekti globaalsele error handler'ile) |

Esimene rida on mockupi märkmetest üle võetud. `trainingId` puudumise kontroll järgib projekti mustrit — `PrimaryKeyNotFoundException` + `getValidTrainingBy(Integer trainingId)` meetod (vt `backend/CLAUDE.md`, "Entiteedi otsing ID järgi"). Kuna olemasoleval koolitusel on alati vähemalt põhikeele tõlge (see luuakse koos koolitusega, vt `POST /api/training`), ei ole tühja tulemuse (`[]`) juhtu ette nähtud — kui `trainingId` on olemas, tagastatakse alati vähemalt üks element.

## Vastuvõtu kriteeriumid

- [ ] Endpoint `GET /api/training/{trainingId}/training-translations` on olemas ja tagastab `List<TrainingTranslationItemDto>`
- [ ] Vastus sisaldab kõiki antud `trainingId`-ga seotud `training_translation` ridu (mitte kõiki andmebaasi tõlkeid)
- [ ] `isMainLanguage` väärtus tuleb `language.is_main_language` veerust, mitte kõvasti kirjutatud loogikast
- [ ] Tulemus on sorteeritud `language_id` järgi kasvavalt
- [ ] Olematu `trainingId` → 404 `PRIMARY_KEY_NOT_FOUND` sõnumiga `Ei leidnud primary keyd 'trainingId' väärtusega: <id>`
- [ ] Teenusel on automaattestid

## Avatud küsimused

1. **Endpointi ega `getValidTrainingBy` meetodit koodis veel pole.** Praeguses `TrainingController`/`TrainingService`-s on ainult `GET /api/trainings` (nimekiri koos filtritega); ühegi ressursi kohta pole veel `getValid<Entiteet>By(Integer id)` mustrit implementeeritud, seega tuleb see selle taski käigus esimesena luua (`TrainingService` või uus koolituse-ID-põhine teenuseklass, vastavalt implementatsiooniplaanile).
2. **`TrainingTranslationItemDto` ja päring `training_translation` + `language` liitmiseks pole veel koodis olemas** — need tuleb luua (nt JPQL `@Query` projektsioon otse DTOsse, vastavalt `backend/CLAUDE.md` "SQL päringud" konventsioonile).
3. **Tühja tulemuse juht** — mockup ega andmebaasi mudel ei näe seda ette (koolitusel on alati vähemalt põhikeele tõlge), seega pole eraldi veakoodi ega käitumist selle jaoks kirjeldatud. Kui reaalsuses siiski esineks koolitus ilma tõlketa (andmete viga), tagastaks teenus tühja massiivi `[]`, mitte viga — see pole mockupis eraldi lahti kirjutatud.
