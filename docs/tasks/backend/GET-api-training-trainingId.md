# Ühe koolituse andmete päring

**Teenus:** `GET /api/training/{trainingId}`

**Kasutav vaade:** `TrainingFormView.vue` (`/training-form?trainingId={id}&trainingTranslationId={id}` — `state: "update"`; ka `state: "new-translation"`)

![Mockup](../../mock-wireframe/pdf-images/TrainingFormView-state-update.png)

## Sisend

**Path variable:**

| Nimi | Tüüp | Kirjeldus |
|---|---|---|
| `trainingId` | Integer | Koolituse ID (`training.id`) |

Teenusel puuduvad query parameetrid ja request body.

## Väljund

**Response (200 OK):** `TrainingDto.java` — koolituse väljad ilma tõlgitud tekstideta (pealkiri, lühikirjeldus, kirjeldus tulevad eraldi teenusest `GET /api/training-translation/{trainingTranslationId}`).

```json
{
  "trainingId": 1,
  "categoryId": 1,
  "trainingLanguageId": 1,
  "locationId": 1,
  "defaultLecturerId": 1,
  "defaultLecturerName": "Rain Tüür",
  "isOrderable": true,
  "isPromoted": true,
  "status": "P",
  "fundingTypeIds": [
    1
  ]
}
```

Näidis vastab `3_import.sql` koolitusele id=1.

Väljade selgitused:

- `trainingId` — `training.id`.
- `categoryId`, `trainingLanguageId`, `locationId` — vastavad `training` tabeli veergudele (`category_id`, `training_language_id`, `location_id`); `trainingLanguageId` on koolituse **õppekeel**, mitte tõlke keel.
- `defaultLecturerId` — `training.default_lecturer_id`; võib olla `null`, kui koolitusel pole vaikimisi lektorit.
- `defaultLecturerName` — vaikimisi lektori nimi (`lecturer.full_name`), leitud `defaultLecturerId` kaudu; `null`, kui `defaultLecturerId` on `null`.
- `isOrderable`, `isPromoted` — `training.is_orderable` ja `training.is_promoted`.
- `status` — `training.status` väärtus otse: `"U"` = mustand (unpublished), `"P"` = publitseeritud. Kasutada `TrainingStatus` enumit (`UNPUBLISHED("U")`, `PUBLISHED("P")`) — **mitte** `ApiStatus`-t, kus `"D"` tähendab kustutatud.
- `fundingTypeIds` — koolitusega seotud `training_funding_type` kirjete `funding_type_id` väärtused, `funding_type_id` järgi kasvavalt; tühi list, kui koolitusel pole ühtegi rahastustüüpi. Tõlgitud rahastustüübi nimesid siin ei tagastata (need tulevad `GET /api/funding-types` teenusest).

Tõlgitavaid välju (`title`, `shortDescription`, `description`) selles vastuses ei ole — need loeb frontend eraldi teenusest `GET /api/training-translation/{trainingTranslationId}`.

## Eesmärk

Admin muudab `TrainingFormView` vaates olemasolevat koolitust. Vaade laeb selle teenusega koolituse baasandmed (kategooria, õppekeel, toimumiskoht, vaikimisi lektor, "Tellitav"/"Esile tõstetud" lülitid, rahastustüübid ja staatus), et vorm eeltäita ning kuvada staatuse märgis ("Mustand"/"Publitseeritud") koos õige nupuga ("Publitseeri" või "Liiguta mustandisse"). Teenust kutsutakse nii `state: "update"` (URL-is `trainingId` ja `trainingTranslationId`) kui `state: "new-translation"` (URL-is `trainingId` ja `languageId`) olekus, kuna mõlemal juhul on vaja koolituse baasandmeid, mitte ainult avatud tõlget. Andmed laaditakse uuesti iga `router.replace` järel (nt pärast "Salvesta", "Publitseeri"/"Liiguta mustandisse" tegevust).

## Seotud andmebaasi tabelid

Vt `docs/database/2_create.sql`.

### training

Koolituse põhikirje.

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

Näidisandmed (`3_import.sql`):

| id | category_id | training_language_id | location_id | default_lecturer_id | status | is_orderable | is_promoted |
|---|---|---|---|---|---|---|---|
| 1 | 1 | 1 | 1 | 1 | P | true | true |
| 2 | 3 | 1 | 2 | 2 | P | false | false |

### lecturer

Vaikimisi lektori nime jaoks (`defaultLecturerName`). Tõlgitud sisu (`lecturer_translation.bio`) sellesse teenusesse ei puutu.

```sql
CREATE TABLE lecturer
(
    id         serial       NOT NULL,
    full_name  varchar(255) NOT NULL,
    photo      bytea        NOT NULL,
    created_at timestamp    NOT NULL,
    updated_at timestamp    NOT NULL,
    created_by int          NOT NULL,
    CONSTRAINT lecturer_pk PRIMARY KEY (id)
);
```

Näidisandmed: id=1 "Rain Tüür" (koolitus 1 vaikimisi lektor), id=2 "Merje Vaide" (koolitus 2 vaikimisi lektor).

### training_funding_type

Koolituse ja rahastustüübi many-to-many liitetabel — `fundingTypeIds` allikas.

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

Näidisandmed: `(1, 1, 1)` — koolitusel 1 on rahastustüüp 1, seega `fundingTypeIds: [1]`. Koolitusel 2 pole ühtegi `training_funding_type` kirjet, seega `fundingTypeIds: []`.

Tabel `funding_type` (ja `funding_type_translation`) sellesse teenusesse ei puutu — tagastatakse ainult ID-d, mitte tõlgitud nimesid.

## Veaolukorrad

| Olukord | Status code | Response body |
|---|---|---|
| `trainingId` ei leidu andmebaasist | 404 Not Found | `{ "message": "Ei leidnud primary keyd 'trainingId' väärtusega: 123", "errorCode": "PRIMARY_KEY_NOT_FOUND" }` |
| Ootamatu serveripoolne viga | 500 Internal Server Error | Standardne vea response body (vastavalt projekti globaalsele error handler'ile) |

Esimene rida on mockupi märkmetest (`Veateated`) ja vastab projekti mustrile: `PrimaryKeyNotFoundException` + `getValidTrainingBy(Integer trainingId)` meetod `TrainingService`-s (vt `backend/CLAUDE.md`).

## Vastuvõtu kriteeriumid

- [ ] Endpoint `GET /api/training/{trainingId}` on olemas ja tagastab `TrainingDto`
- [ ] Õnnestunud päring tagastab 200 OK ja kõik `TrainingDto` väljad (`trainingId`, `categoryId`, `trainingLanguageId`, `locationId`, `defaultLecturerId`, `defaultLecturerName`, `isOrderable`, `isPromoted`, `status`, `fundingTypeIds`) vastavalt andmebaasi andmetele
- [ ] `defaultLecturerId` ja `defaultLecturerName` on mõlemad `null`, kui koolitusel pole vaikimisi lektorit; muul juhul `defaultLecturerName` vastab `lecturer.full_name` väärtusele
- [ ] `fundingTypeIds` sisaldab koolitusega seotud `training_funding_type` kirjete `funding_type_id` väärtused `funding_type_id` järgi kasvavalt; tühi list, kui koolitusel pole ühtegi rahastustüüpi
- [ ] `status` väli kasutab `TrainingStatus` enumit ("U"/"P"), mitte `ApiStatus`-t
- [ ] Olematu `trainingId` → 404 `PRIMARY_KEY_NOT_FOUND` täpselt kirjeldatud sõnumiga
- [ ] Teenusel on automaattestid

## Avatud küsimused

1. **`fundingTypeIds` järjestus.** Ei mockupi API märkmetes ega "API teenuse lisainfos" pole järjestust otseselt kirjeldatud (näidises on ainult üks element). **Otsustatud:** `funding_type_id` järgi kasvavalt — sama järjekord nagu `GET /api/funding-types` vastuses (lühikeste valikunimekirjade ühtne reegel), nii ühtib see frontendi checkboxide järjekorraga. Kui vajalik on hoopis `funding_type_id` järgi sortimine, tuleb see täpsustada.
2. **`user_id`, `created_at`, `updated_at`.** `training` tabelis on ka `user_id`, `created_at`, `updated_at` veerud, kuid ei mockupi API märge ega frontendi `TrainingFormView.vue`/`MockDatabase.js` neid `TrainingDto`-s ei kasuta — task jätab need vastusest teadlikult välja. Kui vaade vajab neid tulevikus (nt "muudetud" kuupäeva kuvamiseks), tuleb DTO-t laiendada.
