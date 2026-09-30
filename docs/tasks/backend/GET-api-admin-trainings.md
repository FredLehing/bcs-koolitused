# Admini koolituste tabeli päring (filtrid, sorteerimine, leheküljestus)

**Teenus:** `GET /api/admin-trainings`

**Kasutav vaade:** `AdminTrainingsView.vue` (`/admin-trainings`)

> Mockupi pilt lisatakse hiljem. Seni vt interaktiivset läbimängu `docs/mock-wireframe/loo-mock-vaade/admin-trainings-view/admin-trainings-view-labimang.html`, märkmeid `docs/mock-wireframe/markmed/admin-trainings-view-markmed.md` ja otsuseid `docs/mock-wireframe/loo-mock-vaade/admin-trainings-view/admin-trainings-view-skeemid.md`.

## Sisend

Kõik parameetrid on query parameetrid. Teenusel puudub request body.

| Parameeter | Tüüp | Kohustuslik | Kirjeldus |
|---|---|---|---|
| `contentLang` | String | jah | Nime ja kategooria keel (`et`/`en`). Kui koolitusel selles keeles tõlget pole, tagastatakse põhikeele (`language.is_main_language`) nimi ja kategooria |
| `searchText` | String | jah | Otsingutekst, `""` = kõik. Jagatakse tühikute kohalt sõnadeks, iga sõna peab esinema (contains, tõstutundetu) **ainult `title` väljas** (erinevalt `GET /api/trainings`, mis otsib ka lühikirjeldusest). `%` ja `_` otsitakse sõna-sõnalt |
| `categoryId` | Integer | jah | Kategooria filter, `0` = kõik |
| `trainingLanguageId` | Integer | jah | Õppekeele filter (`training.training_language_id`), `0` = kõik |
| `fundingTypeId` | Integer | jah | Rahastustüübi filter (`training_funding_type` kaudu), `0` = kõik |
| `status` | String | ei | `"U"` = mustand, `"P"` = publitseeritud, `"D"` = kustutatud. **Puudub = aktiivsed** (`"U"` ja `"P"`) |
| `isOrderable` | Boolean | ei | Tellitav jah/ei; puudub = kõik |
| `isPromoted` | Boolean | ei | Esile tõstetud jah/ei; puudub = kõik |
| `hasAllTranslations` | Boolean | ei | `true` = kõik tõlked olemas, `false` = mõni tõlge puudub; puudub = kõik |
| `sortBy` | String | jah | `createdAt` / `updatedAt` / `title` / `categoryName` / `trainingLanguageCode` / `status` / `hasAllTranslations`. Tundmatu väärtuse korral kasutatakse `createdAt` |
| `sortDirection` | String | jah | `asc` / `desc` |
| `page` | Integer | jah | Lehekülje number, algab 0-st |
| `limit` | Integer | jah | Ridade arv lehel (vaade kasutab `10`) |

Valikulised parameetrid: `@RequestParam(required = false)`; `null` = filtrit ei rakendata.

**Teostus:** päring tehakse uue andmebaasi view `admin_training_summary` peale (vt "Seotud andmebaasi tabelid") JPA Specificationitega, nagu `GET /api/trainings` puhul (`TrainingSummarySpecifications`). Filtreerimine, sorteerimine ja leheküljestamine toimuvad andmebaasis.

## Väljund

**Response (200 OK):** `AdminTrainingSummaryDto.java` — leheküljestatud nimekiri.

Näide vaikimisi päringust `?contentLang=et&searchText=&categoryId=0&trainingLanguageId=0&fundingTypeId=0&sortBy=createdAt&sortDirection=desc&page=0&limit=2` (`3_import.sql` andmed, kõik 8 koolitust on `status = 'P'`):

```json
{
  "totalPages": 4,
  "totalElements": 8,
  "adminTrainingSummaries": [
    {
      "trainingId": 8,
      "trainingTranslationId": 15,
      "title": "SQL ja andmebaasid",
      "categoryId": 1,
      "categoryName": "Programmeerimine",
      "trainingLanguageCode": "et",
      "trainingLanguageFlagIconCode": "fi-ee",
      "status": "P",
      "isOrderable": true,
      "isPromoted": false,
      "createdAt": "2026-08-20T06:00:00Z",
      "updatedAt": "2026-08-20T06:00:00Z",
      "hasAllTranslations": true,
      "missingTranslationLanguageCodes": [],
      "fundingTypes": [
        {
          "fundingTypeId": 1,
          "fundingTypeName": "Töötukassa"
        }
      ]
    },
    {
      "trainingId": 7,
      "trainingTranslationId": 13,
      "title": "Agiilne meeskonnajuhtimine",
      "categoryId": 3,
      "categoryName": "Juhtimine",
      "trainingLanguageCode": "et",
      "trainingLanguageFlagIconCode": "fi-ee",
      "status": "P",
      "isOrderable": true,
      "isPromoted": false,
      "createdAt": "2026-08-18T06:00:00Z",
      "updatedAt": "2026-08-18T06:00:00Z",
      "hasAllTranslations": true,
      "missingTranslationLanguageCodes": [],
      "fundingTypes": [
        {
          "fundingTypeId": 2,
          "fundingTypeName": "EL rahastus"
        }
      ]
    }
  ]
}
```

Väljade selgitused:

- `totalPages`, `totalElements` — kogu filtreeritud tulemushulga kohta.
- `trainingTranslationId`, `title`, `categoryName` — `contentLang` keeles; puuduva tõlke korral põhikeele omad. Vaade kasutab `trainingTranslationId`-d "Vaata" ja "Muuda" linkides.
- `createdAt` = `training.created_at`; `updatedAt` = hiliseim `training.updated_at` ja selle koolituse tõlgete `updated_at` (`Instant`, JSON-is UTC). Frontend kuvab kuupäeva kujul `30/09/2026`.
- `hasAllTranslations` — kas koolitusel on tõlge igas keeles, mille `language.requires_translation = true`.
- `missingTranslationLanguageCodes` — puuduvate tõlgete keelekoodid (view veerg on komaga eraldatud string, nt `"en"` → DTO-s list `["en"]`); tühi list, kui kõik on olemas.
- `fundingTypes` — `contentLang` keeles, samamoodi nagu `GET /api/trainings` vastuses (`FundingTypeDto`, `controller/common/dto/`); võib olla tühi list.

**Sorteerimine:** `sortBy` + `sortDirection` järgi, lisaks alati `training_id` järgi (stabiilne järjekord võrdsete väärtuste korral). Erandid:

- `status` sorteeritakse view veeru `status_order` järgi (1 = `U`, 2 = `P`, 3 = `D`), et kasvav järjekord oleks töövoo järjekord Mustand → Publitseeritud → Kustutatud.
- `hasAllTranslations` kasvavalt = puuduvate tõlgetega koolitused eespool (`false` → `true`).

## Eesmärk

Admin näeb `AdminTrainingsView` tabelis kiiret ülevaadet kõigist koolitustest: lisamise ja muutmise kuupäev, nimi, kategooria, õppekeel, staatus, tõlgete olemasolu ja sätted. Tabelit saab otsida nime järgi, filtreerida (kaart "Otsingu filtrid"), sorteerida veeru pealkirjale klõpsates ja lehitseda. Vaikimisi kuvatakse ainult aktiivsed koolitused; kustutatud koolitused tulevad nähtavale filtriga Staatus = "Kustutatud", et neid saaks taastada.

## Seotud andmebaasi tabelid

Vt `docs/database/2_create.sql`.

### Uus view `admin_training_summary`

Lisa `docs/database/2_create.sql` faili lõppu (olemasoleva `training_summary` järele). Rida tekib **iga koolituse ja iga tõlkekeele** (`language.requires_translation = true`) kohta — ka siis, kui selles keeles tõlget pole; siis võetakse nimi ja kategooria põhikeelest. Kustutatud koolitused on view's sees, staatuse filter rakendatakse päringus.

```sql
-- Admini koolituste tabel: üks rida koolituse ja tõlkekeele kohta; puuduva tõlke korral põhikeele pealkiri ja kategooria
CREATE VIEW admin_training_summary AS
SELECT row_number() OVER (ORDER BY t.id, cl.id)                    AS id,
       t.id                                                        AS training_id,
       cl.code                                                     AS content_language_code,
       COALESCE(tt.id, mtt.id)                                     AS training_translation_id,
       COALESCE(tt.title, mtt.title)                               AS title,
       t.category_id,
       COALESCE(ct.name, mct.name)                                 AS category_name,
       t.training_language_id,
       trl.code                                                    AS training_language_code,
       trl.flag_icon_code                                          AS training_language_flag_icon_code,
       t.status,
       CASE t.status WHEN 'U' THEN 1 WHEN 'P' THEN 2 ELSE 3 END   AS status_order,
       t.is_orderable,
       t.is_promoted,
       t.created_at,
       GREATEST(t.updated_at, (SELECT MAX(att.updated_at)
                               FROM training_translation att
                               WHERE att.training_id = t.id))     AS updated_at,
       mt.missing_translation_language_codes,
       mt.missing_translation_language_codes IS NULL               AS has_all_translations
FROM training t
         CROSS JOIN language cl
         JOIN language trl ON trl.id = t.training_language_id
         JOIN language ml ON ml.is_main_language
         LEFT JOIN training_translation tt ON tt.training_id = t.id AND tt.language_id = cl.id
         LEFT JOIN training_translation mtt ON mtt.training_id = t.id AND mtt.language_id = ml.id
         LEFT JOIN category_translation ct ON ct.category_id = t.category_id AND ct.language_id = cl.id
         LEFT JOIN category_translation mct ON mct.category_id = t.category_id AND mct.language_id = ml.id
         -- string_agg tühjast hulgast annab NULL, seega NULL = kõik tõlked olemas
         CROSS JOIN LATERAL (SELECT string_agg(rl.code, ',' ORDER BY rl.id) AS missing_translation_language_codes
                             FROM language rl
                             WHERE rl.requires_translation
                               AND NOT EXISTS (SELECT 1
                                               FROM training_translation rtt
                                               WHERE rtt.training_id = t.id
                                                 AND rtt.language_id = rl.id)) mt
WHERE cl.requires_translation;
```

**NB!** SQL-i pole veel andmebaasi vastu käivitatud — kontrolli esimese asjana (`1_reset` → `2_create` → `3_import`, siis `SELECT * FROM admin_training_summary WHERE content_language_code = 'et'`). `3_import.sql` andmetega peab tulema 8 rida keele kohta, kõigil `has_all_translations = true`.

| View veerg | Filter / sorteerimine / väli |
|---|---|
| `content_language_code` | alati `= contentLang` |
| `training_translation_id`, `title` | `trainingTranslationId`, `title`; otsing; `sortBy=title` |
| `category_id`, `category_name` | `categoryId` filter; `sortBy=categoryName` |
| `training_language_id`, `training_language_code`, `training_language_flag_icon_code` | `trainingLanguageId` filter; `sortBy=trainingLanguageCode`; lipp |
| `status` | `status` filter (puudub = `IN ('U', 'P')`) |
| `status_order` | `sortBy=status` |
| `is_orderable`, `is_promoted` | filtrid |
| `created_at`, `updated_at` | `sortBy=createdAt` / `updatedAt` |
| `missing_translation_language_codes` | `missingTranslationLanguageCodes` |
| `has_all_translations` | `hasAllTranslations` filter ja `sortBy=hasAllTranslations` |

View entity (nt `persistance/view/admintrainingsummary/AdminTrainingSummary.java`) tehakse `@Immutable` klassina nagu `TrainingSummary`. `training_id` võiks olla `@ManyToOne Training` seos, et rahastustüübi filter saaks kasutada sama alampäringu mustrit nagu `TrainingSummarySpecifications.hasFundingTypeId`.

### training, training_translation, category_translation, language, training_funding_type, funding_type_translation

View põhitabelid ja rahastustüübid — struktuur `2_create.sql`-is. `training.status` väärtused: `U` = mustand, `P` = publitseeritud, `D` = kustutatud (`TrainingStatus` enum, `DELETED("D")` lisatakse `DELETE-api-training-trainingId.md` taskis).

Näidisandmed (`3_import.sql`): 8 koolitust (id 1–8), kõik `status = 'P'`, kõigil `et` ja `en` tõlge. Loodud vahemikus `2026-08-01` (id 1) kuni `2026-08-20` (id 8). Õppekeel `en` on koolitustel 4 ja 6. Rahastustüübita on koolitused 2 ja 6. Mustandi, kustutatud koolituse ja puuduva tõlke kontrollimiseks tuleb testis andmeid muuta (vt läbimängu näidised 9–14).

## Veaolukorrad

| Olukord | Status code | Response body |
|---|---|---|
| Integer/Boolean parameetrisse antakse sobimatu väärtus (nt `page=abc`) | 400 Bad Request | Standardne vea response body |
| Ootamatu serveripoolne viga | 500 Internal Server Error | Standardne vea response body (vastavalt projekti globaalsele error handler'ile) |

Mockup ärivigu ei kirjelda — tühi tulemus on normaalne (`totalElements: 0`, tühi list), mitte viga.

## Vastuvõtu kriteeriumid

- [ ] View `admin_training_summary` on `2_create.sql`-is ja andmebaas luuakse skriptidest vigadeta
- [ ] Endpoint `GET /api/admin-trainings` on olemas ja tagastab `AdminTrainingSummaryDto` (lihtväljad enne listi)
- [ ] Ilma `status` parameetrita tagastatakse ainult `U` ja `P` koolitused; `status=D` tagastab ainult kustutatud
- [ ] Iga koolitus on vastuses täpselt üks kord; puuduva `contentLang` tõlke korral on `title`, `categoryName` ja `trainingTranslationId` põhikeelest
- [ ] `searchText` otsib ainult `title`-ist, iga sõna peab esinema, tõstutundetult
- [ ] `categoryId`, `trainingLanguageId`, `fundingTypeId` (`0` = kõik) ja valikulised `isOrderable`, `isPromoted`, `hasAllTranslations` filtreerivad õigesti ja koos
- [ ] Kõik 7 `sortBy` väärtust töötavad mõlemas suunas; `status` töövoo järjekorras, `hasAllTranslations` kasvavalt puuduvad eespool; lisaks alati `trainingId` järgi
- [ ] Tundmatu `sortBy` → sorteeritakse `createdAt` järgi
- [ ] `updatedAt` arvestab ka tõlgete `updated_at`-i
- [ ] `hasAllTranslations` ja `missingTranslationLanguageCodes` vastavad `requires_translation = true` keeltele
- [ ] `fundingTypes` on `contentLang` keeles, võib olla tühi list
- [ ] Leheküljestus: `page`, `limit`, `totalPages`, `totalElements` on õiged
- [ ] Teenusel on automaattestid

## Avatud küsimused

1. **`sortDirection` sobimatu väärtus** — eeldus: kõik muu peale `asc` tähendab `desc`. Täpsusta, kui soovid 400 viga.
2. **Tõstutundetu sorteerimine eesti keeles** (`title`, `categoryName`) sõltub andmebaasi collation'ist — kontrolli, et "Š", "Ž", "Õ" jms oleksid õiges kohas; vajadusel lepi kokku, et piisab andmebaasi vaikimisi järjestusest.
