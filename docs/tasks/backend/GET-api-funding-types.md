# Rahastustüüpide nimekirja päring

**Teenus:** `GET /api/funding-types?contentLang={contentLang}`

**Kasutav vaade:** `TrainingFormView.vue` (`/training-form`), `TrainingsView.vue` (`/trainings`)

![Mockup](../../mock-wireframe/pdf-images/TrainingFormView-state-new-training.png)

## Sisend

Query parameeter, kohustuslik (`@RequestParam` ilma `required = false`-ta):

| Parameeter | Tüüp | Kirjeldus |
|---|---|---|
| `contentLang` | String (`et`/`en`) | Määrab tagastatava `fundingTypeName` välja keele |

Teenusel puudub request body.

## Väljund

**Response (200 OK):** Kõigi süsteemis olevate rahastustüüpide nimekiri, `contentLang` keeles tõlgitud.

`FundingTypeDto.java` (olemasolev, `controller/common/dto/FundingTypeDto.java`) — sama DTO-d kasutab ka `GET /api/trainings` vastuse `fundingTypes` list:

```json
[
  {
    "fundingTypeId": 1,
    "fundingTypeName": "Töötukassa"
  },
  {
    "fundingTypeId": 2,
    "fundingTypeName": "EL rahastus"
  }
]
```

Väljade selgitused:

- `fundingTypeId` — pärineb `funding_type.id`.
- `fundingTypeName` — pärineb `funding_type_translation.name` väljalt, `contentLang` keeles.
- Järjestus: `funding_type.id` kasvavas järjekorras (sama muster nagu olemasolevas `FundingTypeTranslationRepository.findTrainingFundingTypeTranslationsBy(...)` päringus, mis on hetkel filtreeritud kindla koolituse järgi).

**Olemasolevad klassid, mida saab taaskasutada/laiendada:**

- `FundingTypeDto.java` (`controller/common/dto/`) — response DTO on juba olemas.
- `FundingTypeTranslationRepository.java` (`persistance/fundingtype/translation/`) — sisaldab hetkel ainult meetodit, mis tagastab ühe koolituse rahastustüüpide tõlked (`findTrainingFundingTypeTranslationsBy(trainingId, contentLang)`). Selle teenuse jaoks on vaja uut repositooriumi meetodit, mis tagastab **kõik** `FundingTypeTranslation` read antud `contentLang` järgi (nt `findAllFundingTypeTranslationsBy(String contentLang)`), ilma koolitusega sidumata.
- `FundingTypeTranslationMapper.java` (`persistance/fundingtype/translation/`) — `toFundingTypeDtos(List<FundingTypeTranslation>)` meetod on juba olemas ja sobib otse kasutamiseks.

## Eesmärk

`TrainingFormView` vaates (roll: Admin) kuvatakse rahastustüübid checkboxidena koolituse lisamise/muutmise vormil, kus admin valib koolitusele sobivad rahastustüübid (`fundingTypeIds` väli `POST /api/training` päringus). `TrainingsView` vaates (avalik, kõik rollid) kasutatakse sama nimekirja "Rahastus" filtri valikute kuvamiseks, mille järgi külastaja saab koolituste nimekirja filtreerida (`GET /api/trainings` `fundingTypeId` parameeter). Mõlemas vaates tehakse päring vaate avanemisel, keel määratakse `contentLang` parameetriga.

## Seotud andmebaasi tabelid

Vt `docs/database/2_create.sql`.

### funding_type

Rahastustüübi põhikirje (kood, loomise metaandmed).

```sql
CREATE TABLE funding_type
(
    id         serial      NOT NULL,
    code       varchar(50) NOT NULL,
    created_at timestamp   NOT NULL,
    updated_at timestamp   NOT NULL,
    created_by int         NOT NULL,
    CONSTRAINT funding_type_pk PRIMARY KEY (id),
    CONSTRAINT funding_type_code_uq UNIQUE (code)
);
```

Näidisandmed (`docs/database/3_import.sql`):

| id | code |
|---|---|
| 1 | JOB_CENTRE |
| 2 | EU_FUNDED |

### funding_type_translation

Rahastustüübi tõlgitud nimi (`name`) keele kaupa.

```sql
CREATE TABLE funding_type_translation
(
    id              serial       NOT NULL,
    funding_type_id int          NOT NULL,
    language_id     int          NOT NULL,
    name            varchar(255) NOT NULL,
    created_at      timestamp    NOT NULL,
    updated_at      timestamp    NOT NULL,
    CONSTRAINT funding_type_translation_pk PRIMARY KEY (id),
    CONSTRAINT funding_type_translation_uq UNIQUE (funding_type_id, language_id)
);
```

Näidisandmed (`docs/database/3_import.sql`):

| id | funding_type_id | language_id | name |
|---|---|---|---|
| 1 | 1 | 1 (et) | Töötukassa |
| 2 | 1 | 2 (en) | Job Centre |
| 3 | 2 | 1 (et) | EL rahastus |
| 4 | 2 | 2 (en) | EU Funded |

### language

`contentLang` kasutab `language.code` väärtust (`et`/`en`). Vt struktuuri `docs/tasks/backend/GET-api-trainings.md` failist — sama tabelit kasutatakse ka seal.

**Tabel `training_funding_type` ei kuulu selle teenuse skoopi** — see on koolituse ja rahastustüübi vaheline liitetabel, mida kasutab `GET /api/trainings` vastuse `fundingTypes` väli, mitte see üldine rahastustüüpide nimekirja teenus.

## Veaolukorrad

| Olukord | Status code | Response body |
|---|---|---|
| `contentLang` parameeter puudub | 400 Bad Request | Springi vaikimisi veavastus (mitte projekti `ApiError` kuju) — kuna parameeter on `@RequestParam` ilma `required = false`-ta, annab Spring ise 400, ilma projekti `errorCode`/`message` väljadeta |
| Tundmatu `contentLang` väärtus (pole `et` ega `en`) | 200 OK | Tühi list `[]` — see ei ole veaolukord, vaid tavapärane tühja tulemuse juhtum (sama muster nagu `GET /api/trainings` teenusel) |
| Ootamatu serveri viga | 500 Internal Server Error | Standardne `ApiError` vastus (vastavalt projekti globaalsele error handler'ile) |

Mockupi märkmetes (`Veateated: —`) ärivigu ei kirjeldata.

## Vastuvõtu kriteeriumid

- [ ] Endpoint `GET /api/funding-types` on olemas ja tagastab `List<FundingTypeDto>`
- [ ] `contentLang` parameeter on kohustuslik ja määrab `fundingTypeName` tõlke keele
- [ ] Vastus sisaldab kõiki `funding_type` kirjeid, sorteeritud `id` kasvavas järjekorras
- [ ] Tundmatu `contentLang` väärtuse korral tagastatakse tühi list, mitte viga
- [ ] Puuduva `contentLang` parameetri korral tagastatakse 400 (Springi vaikekäitumine)
- [ ] Teenusel on automaattestid, mis katavad õnnestunud päringu (`et`/`en`) ja tühja tulemuse juhtumi

## Avatud küsimused

1. **`ru` keele mainimine `TrainingFormView` API märkmetes.** Fail `docs/mock-wireframe/markmed/training-form-view-state-new-training-markmed.md` kirjeldab `contentLang` väärtusi kui `"et"/"en"/"ru"`, samas kui `docs/mock-wireframe/markmed/trainings-view-markmed.md` kirjeldab sama teenust ainult väärtustega `"et"/"en"`. Kuna `funding_type_translation` tabelis (`docs/database/3_import.sql`) on andmed ainult `language_id` 1 (et) ja 2 (en) kohta, on see task koostatud eeldusel, et `contentLang` toetab reaalselt ainult `et`/`en` — `ru` on mainitud ainult ühes mockupi märkmes ega vasta andmebaasi tegelikule sisule. Kui vene keel on siiski vajalik, tuleb esmalt lisada vastavad andmed ja tõlked andmebaasi.
2. **Endpoint puudub veel koodis.** `backend/src/main/java/ee/bcskoolitus/` alt ei leidu `FundingTypeController` klassi ega teenusklassi selle päringu jaoks — see task kirjeldab uut loodavat teenust. `FundingTypeDto`, `FundingTypeTranslation`, `FundingTypeTranslationMapper` ja `FundingTypeTranslationRepository` on juba olemas, kuid repositooriumis on ainult koolituse-spetsiifiline otsingumeetod — üldise nimekirja jaoks tuleb lisada uus meetod (vt "Väljund" jaotus).
3. **Puuduva `contentLang` parameetri veavastus.** Kuna `@RequestParam` on kohustuslik, tagastab Spring omal algatusel 400, mille body ei vasta projekti `ApiError` kujule (`message`/`errorCode`). Sama muster kehtib ka `GET /api/trainings` teenusel ja seda pole taskis eraldi lahendatud — kui on vaja ühtset `ApiError` vormingut ka selle juhtumi jaoks, tuleb see täpsustada ja rakendada globaalselt, mitte ainult selles teenuses.
